#include <jni.h>
#include <string>
#include <android/sensor.h>
#include "udpClient.h"
#include "lib.h"
#include <sstream>
#include <android/log.h>

udpClient* client = nullptr;

extern "C" JNIEXPORT void JNICALL Java_com_bmc_app_ConnectionManager_connectToHost(
        JNIEnv* env,
        jobject cm,
        jstring address,
        jstring device) {
    __android_log_print(ANDROID_LOG_DEBUG, "BMC-App", "BMC library initializing...");

    if (client) delete client;

    const char* addr_chars = env->GetStringUTFChars(address, nullptr);
    std::string addr(addr_chars);
    env->ReleaseStringUTFChars(address, addr_chars);

    const char* dev_chars = env->GetStringUTFChars(device, nullptr);
    std::string dev(dev_chars);
    env->ReleaseStringUTFChars(address, dev_chars);

    size_t colon = addr.find(':');

    std::string ip = addr.substr(0, colon);
    int port = std::stoi(addr.substr(colon + 1));

    client = new udpClient(ip, port, dev, env, (*env).NewGlobalRef(cm));
}

extern "C" JNIEXPORT void JNICALL Java_com_bmc_app_ConnectionManager_disconnect(
        JNIEnv* env,
        jobject cm) {
    disconnect();
}

void disconnect() {
    if (client) {
        client->stop();
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