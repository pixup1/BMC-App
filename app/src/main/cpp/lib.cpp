#include <jni.h>
#include <string>
#include <android/sensor.h>
#include "udpClient.h"
#include "lib.h"
#include <sstream>
#include <android/log.h>

udpClient* client = nullptr;

extern "C" JNIEXPORT void JNICALL Java_com_bmc_app_ConnectionManager_connect(
        JNIEnv* env,
        jobject cm,
        jstring address) {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "BMC library initializing...");

    if (client) delete client;
    const char* addr_chars = env->GetStringUTFChars(address, nullptr);
    std::string addr(addr_chars);
    env->ReleaseStringUTFChars(address, addr_chars);

    size_t colon = addr.find(':');

    std::string ip = addr.substr(0, colon);
    int port = std::stoi(addr.substr(colon + 1));

    client = new udpClient(ip, port, env, (*env).NewGlobalRef(cm));
}

extern "C" JNIEXPORT void JNICALL Java_com_bmc_app_ConnectionManager_disconnect(
        JNIEnv* env,
        jobject cm) {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "calling disconnect()...");
    disconnect();
}

void disconnect() {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "disconnect() started");
    if (client) {
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "client found, stopping...");
        client->stop();
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "deleting client...");
        delete client;
        client = nullptr;
        __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "disconnect done");
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_bmc_app_ConnectionManager_sendData(
        JNIEnv* env,
        jobject cm,
        jstring data) {
    if (client) {
        const char* data_chars = env->GetStringUTFChars(data, nullptr);
        std::string msg(data_chars);
        env->ReleaseStringUTFChars(data, data_chars);

        client->sendData(msg);
    }
}