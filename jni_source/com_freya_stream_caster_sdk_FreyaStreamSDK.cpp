#include <string.h>
#include <time.h>
#include "com_freya_stream_caster_sdk_FreyaStreamSDK.h"
#include "FreyaStreamCore.h"

FreyaStreamCore *freya_streamer = NULL;

JavaVM *g_jvm = NULL; 
jobject g_obj = NULL; 

void OnConnectCallBack(int err)
{
	 JNIEnv *env; 

     if( g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) 
     { 
         return; 
     } 

	jclass cls = env->GetObjectClass(g_obj);
	jmethodID onConnetingCB = env->GetMethodID(cls,"onNativeConnecting","()V");
	jmethodID onConnetErrCB = env->GetMethodID(cls,"onNativeConnectError","(I)V");
	jmethodID onConnetedCB = env->GetMethodID(cls,"onNativeConnected","()V");
	
	if (err<0)
	{
	   env->CallVoidMethod(g_obj, onConnetErrCB, err);
	}
    else
	{
	   env->CallVoidMethod(g_obj, onConnetedCB);
	}
 
     if(g_jvm->DetachCurrentThread() != JNI_OK) 
     { 
     } 
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_InitPusher
  (JNIEnv *env, jobject obj){

	freya_streamer = new FreyaStreamCore();
	freya_streamer->Initialize();

	env->GetJavaVM(&g_jvm); 
    g_obj=env->NewGlobalRef(obj); 

	return 0;
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_DeinitPusher
  (JNIEnv *env, jobject obj){

	if (freya_streamer != NULL)
	{
		freya_streamer->Destroy();
		delete freya_streamer;
		freya_streamer = NULL;
	}

	env->DeleteGlobalRef(g_obj);

	return 0;
}


JNIEXPORT void JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupRtmpUrl
 (JNIEnv *env, jobject obj, jstring url)
{
	const char *server_url = env->GetStringUTFChars(url, 0);
	freya_streamer->setupRtmpUrl((const char *)server_url);
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupVideoOptions
  (JNIEnv *env, jobject obj, jint width, jint height, jint fps, jint bitrate, jstring h264profile, jstring h264encodemode){

  	const char *h264profile_got = env->GetStringUTFChars(h264profile, 0);
	const char *h264encodemode_got = env->GetStringUTFChars(h264encodemode, 0);
	return freya_streamer->setupVideoParameter( width,  height,  bitrate,  fps,  (const char *)h264profile_got,  (const char *)h264encodemode_got);
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_SetupAudioOptions
  (JNIEnv *env, jobject obj, jint sample_rate, jint channels, jint soundrate, jstring aacprofile){
	  
	const char *aacprofile_got = env->GetStringUTFChars(aacprofile, 0);
	return freya_streamer->setupAudioParameter( sample_rate, channels, soundrate, (const char *)aacprofile_got);
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_StartPush
  (JNIEnv *env, jobject obj){
	return freya_streamer->startSession();
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_StopPush
   (JNIEnv *env, jobject obj){
	return freya_streamer->stopSession();
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_PushVideoData
  (JNIEnv *env, jobject thiz, jbyteArray vdata, jint in_width, jint in_height, jlong ts, jint is_front, jint orientation){
	
	jint dataLength = env->GetArrayLength(vdata);
	jbyte* jBuffer = (jbyte*)malloc(dataLength * sizeof(jbyte));
	env->GetByteArrayRegion(vdata, 0, dataLength, jBuffer);
	
	int ret = freya_streamer->VideoPusher((unsigned char *) jBuffer,dataLength, in_width, in_height, ts, is_front, orientation);

	free(jBuffer);
	return ret;
}


JNIEXPORT jint JNICALL Java_com_freya_stream_caster_sdk_FreyaStreamSDK_PushAudioData
  (JNIEnv *env, jobject thiz, jbyteArray adata, jlong ts){

	jint dataLength = env->GetArrayLength( adata);
	jbyte* jBuffer = (jbyte*)malloc(dataLength * sizeof(jbyte));
	env->GetByteArrayRegion(adata, 0, dataLength, jBuffer);

	int ret = freya_streamer->AudioPusher((unsigned char *) jBuffer,dataLength, ts);
	
	free(jBuffer);
	return ret;
}
