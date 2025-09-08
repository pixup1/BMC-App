#ifndef BLENDER_MOTION_CONTROL_UDPCLIENT_H
#define BLENDER_MOTION_CONTROL_UDPCLIENT_H

#include <arpa/inet.h>
#include <thread>
#include <jni.h>

#define BUF_LENGTH 1024
#define PING_RATE 2 // hertz
#define FAILED_PINGS_DISCONNECT 4 // Number of failed pings after which the client disconnects
#define TIMEOUT_CHECK_RATE 3 // hertz
#define TIMEOUT 1 // seconds
#define CONNECTION_TRIES 3 // Number of tries to connect before giving up

struct sent {
    std::string type;
    std::string msg;
    int64_t timestamp;
};

class udpClient {
public:
    /**
     * @param server_ip IP of the server to connect to
     * @param server_port Port of the server to connect
     * @param env JNI environment
     * @param cm Reference to the ConnectionManager caller Kotlin instance
     */
    udpClient(std::string server_ip, int server_port, JNIEnv* env, jobject cm);
    void stop();
    void maintainConnection();
    void listen();
    void checkTimeouts();
    void send(std::string type, std::string msg, bool await_reply);
    std::string receive();
    void sendData(std::string data);
    void connect();
    void handleConnectReply(std::string msg);
    void ping();
    void handlePingReply(std::string msg);
private:
    JavaVM* jvm;
    jobject cm;
    std::function<void(int)> setUiConnectionState;
    int sock;
    std::string host;
    sockaddr_in server{};
    std::atomic<bool> running;
    std::thread conn_thread;
    std::thread listen_thread;
    std::thread timeout_thread;
    bool connected;
    std::string device_name;
    std::vector<sent> sent_messages;
    int ping_counter;
    int failed_pings;
    int failed_connections;
};

#endif //BLENDER_MOTION_CONTROL_UDPCLIENT_H
