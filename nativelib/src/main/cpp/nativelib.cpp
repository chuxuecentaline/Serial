#include <jni.h>
#include <string>
#include "crc.h"
#include <stdio.h>
#include <stdlib.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_yaxiu_nativelib_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT int JNICALL
Java_com_yaxiu_nativelib_NativeLib_cRC16(JNIEnv *env, jobject thiz, jstring puch_msg,
                                         jint us_data_len) {
    const char *str = env->GetStringUTFChars(puch_msg, JNI_FALSE);
    int len = us_data_len;

    printf("c-int: %d - %s", len, str);
    return CRC16((unsigned char *) str, len);

}