#include "udpClient.h"
#include "lib.h"
#include <sys/system_properties.h>
#include <android/log.h>

udpClient::udpClient(std::string server_ip, int server_port, std::string device, JNIEnv *env, jobject cm)
        : running(true) {
    this->jvm = nullptr;
    env->GetJavaVM(&this->jvm);
    this->cm = cm;

    // Create lambda to call setUiConnectionState on the ConnectionManager instance
    jclass cls = env->GetObjectClass(this->cm);
    jmethodID mid = env->GetMethodID(cls, "setUiConnectionState", "(ILjava/lang/String;)V");
    this->setUiConnectionState = [=](int state) {
        JNIEnv *threadEnv = nullptr;
        jint status = this->jvm->GetEnv((void **) &threadEnv, JNI_VERSION_1_6);

        // Each thread needs a different JNIEnv so we can't use the same one as lib.cpp
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "      getting JNIEnv for thread...");
        this->jvm->AttachCurrentThread(&threadEnv, nullptr);

        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "      converting to Java types...");
        jint jstate = (jint) state;
        jstring jhost = threadEnv->NewStringUTF(this->host.c_str());

        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "      calling method...");
        threadEnv->CallVoidMethod(this->cm, mid, jstate, jhost);

        if (status == JNI_EDETACHED) { // Only detach if we weren't attached before
            __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "      detaching thread...");
            jvm->DetachCurrentThread();
        }

        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "      UI state changed");
    };

    this->connected = false;
    this->host = server_ip;

    this->server.sin_family = AF_INET;
    this->server.sin_port = htons(server_port);
    const char *ip_char = const_cast<char *>(server_ip.c_str());
    inet_pton(AF_INET, ip_char, &(this->server.sin_addr));

    this->sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (this->sock < 0) {
        //TODO
    }

    this->conn_thread = std::thread(&udpClient::maintainConnection, this);
    this->listen_thread = std::thread(&udpClient::listen, this);
    this->timeout_thread = std::thread(&udpClient::checkTimeouts, this);

//    // Get device model from system properties
//    char man[PROP_VALUE_MAX + 1], mod[PROP_VALUE_MAX + 1];
//    int lman = __system_property_get("ro.product.manufacturer", man);
//    int lmod = __system_property_get("ro.product.model", mod);
//    int len = lman + lmod;
//    if (len > 0) {
//        char *buf = static_cast<char *>(malloc((len + 2) * sizeof(char)));
//        snprintf(buf, len + 2, "%s/%s", lman > 0 ? man : "", lmod > 0 ? mod : "");
//        this->device_name = std::string(buf);
//        free(buf);
//    } else {
//        this->device_name = "N/A";
//    }

    this->device_name = device;

    // Sent messages awaiting replies
    this->sent_messages = std::vector<sent>();

    this->ping_counter = 0;
    this->failed_pings = 0;
    this->failed_connections = 0;

    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "udpClient initialized");
}

void udpClient::stop() {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   shutdown socket...");
    shutdown(this->sock, SHUT_RDWR);
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   joining listen thread...");
    this->running = false;
    if (this->listen_thread.joinable()) {
        this->listen_thread.join();
    }
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   joining conn thread...");
    if (this->conn_thread.joinable()) {
        this->conn_thread.join();
    }
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   joining timeout thread...");
    if (this->timeout_thread.joinable()) {
        this->timeout_thread.join();
    }
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   closing socket...");
    close(this->sock);
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   doing stuff...");
    this->connected = false;
    this->sent_messages.clear();
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "   changing connection state...");
    this->setUiConnectionState(0);
}

void udpClient::maintainConnection() {
    int interval_ms = 1000 / PING_RATE;

    while (this->running) {
        if (!this->connected) {
            this->connect();
        } else {
            std::thread(&udpClient::ping,
                        this).detach(); // Otherwise stop() will try to join its own thread
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(interval_ms));
    }
}

void udpClient::listen() {
    while (this->running) {
        std::string msg = this->receive();
        if (msg == "[RECEPTION_ERROR]") {
            continue;
        }

        size_t pos = msg.find(' ');
        std::string type, rest;

        if (pos == std::string::npos) {
            type = msg;      // no space found
            rest = "";
        } else {
            type = msg.substr(0, pos);
            rest = msg.substr(pos + 1);
        }

        if (type == "CONNECT") {
            this->handleConnectReply(rest);
        } else if (type == "PONG") {
            this->handlePingReply(rest);
        } else if (type == "ERR") {
            if (rest == "Not connected" and this->connected) {
                this->connected = false;
                this->setUiConnectionState(1);
                this->sent_messages.clear();
            }
        } else {
            // idk
        }
    }
}

void udpClient::checkTimeouts() {
    int interval_ms = 1000 / TIMEOUT_CHECK_RATE;

    while (this->running) {
        int64_t now = time(nullptr);

        this->sent_messages.erase( // TODO: optimize?
                std::remove_if(this->sent_messages.begin(), this->sent_messages.end(),
                               [&](const sent &s) {
                                   bool expired = (now - s.timestamp) >= TIMEOUT;
                                   if (expired) {
                                       if (s.type == "PING") {
                                           this->failed_pings++;
                                       } else if (s.type == "CONNECT") {
                                           this->failed_connections++;
                                       }
                                   }
                                   return expired;
                               }),
                this->sent_messages.end()
        );

        if (this->failed_connections >= CONNECTION_TRIES) {
            std::thread([this]() { disconnect(); }).detach();
        } else if (this->failed_pings >= FAILED_PINGS_DISCONNECT and this->connected) {
            this->connected = false;
            this->setUiConnectionState(1);
            this->sent_messages.clear();
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(interval_ms));
    }
}

void udpClient::send(std::string type, std::string msg, bool await_reply) {
    std::string full_msg = type + " " + msg;
    const char *char_msg = full_msg.c_str();
    ssize_t n = sendto(sock, char_msg, strlen(char_msg), 0, (sockaddr *) &server, sizeof(server));
    if (n < 0) {
        //TODO
    }
    if (await_reply) {
        sent s{type, msg, static_cast<int64_t >(time(nullptr))};
        this->sent_messages.push_back(s);
    }

    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "message sent: %s %s", type.c_str(),
                        msg.c_str());
}

std::string udpClient::receive() {
    char reply_buf[BUF_LENGTH];
    socklen_t len = sizeof(this->server);
    ssize_t n = recvfrom(this->sock, reply_buf, BUF_LENGTH - 1, 0, (sockaddr *) &this->server,
                         &len);
    if (n < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "huh?");
        return "[RECEPTION_ERROR]";
    }

    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "reply received: %s", std::string(reply_buf, n).c_str());
    return std::string(reply_buf, n);
}

void udpClient::sendData(std::string data) {
    if (this->connected) this->send("DATA", data, false);
}

void udpClient::connect() {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "udpClient connecting...");

    this->setUiConnectionState(1);

    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "UI connection state set");

    std::string msg = std::string(this->device_name);

    this->send("CONNECT", msg, true);
}

void udpClient::handleConnectReply(std::string msg) { //TODO: check failed connections
    if (msg == "Connected") {
        this->connected = true;
        this->failed_connections = 0;
        this->setUiConnectionState(2);

        // Delete any pending CONNECT messages
        this->sent_messages.erase(
                std::remove_if(this->sent_messages.begin(), this->sent_messages.end(),
                               [&](const sent &s) {
                                   return s.type == "CONNECT";
                               }),
                this->sent_messages.end()
        );
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "udpClient connected");
    }
}

void udpClient::ping() {
    std::string msg = std::to_string(this->ping_counter++);

    this->send("PING", msg, true);
}

void udpClient::handlePingReply(std::string id) {
    auto it = std::find_if(this->sent_messages.begin(), this->sent_messages.end(),
                           [id](const sent &s) { return id == s.msg; });
    if (it != this->sent_messages.end()) {
        this->sent_messages.erase(it);
        this->failed_pings = 0;
    }
}