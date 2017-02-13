#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_InitPusher
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_DeinitPusher
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupRtmpUrl
  (JNIEnv *, jobject, jstring);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupVideoOptions
  (JNIEnv *, jobject, jint, jint, jint, jint, jstring, jstring);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupAudioOptions
  (JNIEnv *, jobject, jint, jint, jint, jstring);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_StartPush
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_StopPush
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_PushAudioData
  (JNIEnv *, jobject, jbyteArray, jlong);

JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_PushVideoData
  (JNIEnv *, jobject, jbyteArray, jint, jint, jlong, jint, jint);


#ifdef __cplusplus
}
#endif