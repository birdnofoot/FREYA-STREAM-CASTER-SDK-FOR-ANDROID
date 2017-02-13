package com.freya.stream.caster.sdk;


public class FreyaStreamSDK {

    static {
        try {
            System.loadLibrary("FreyaStreamSDK");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Load library failed!");
        }
    }


    private int mMediaLiveStatus = 0;

    private FreyaStreamProcessorCallBack mStreamPusherCallBack = null;

    public native int InitPusher();

    public native int DeinitPusher();

    public native void SetupRtmpUrl(String url);

    public native int SetupVideoOptions(int width, int height, int fps, int bitrate, String h264profile, String h264encodemode);

    public native int SetupAudioOptions(int sample_rate, int channels, int soundrate, String aacprofile);

    public native int StartPush();

    public native int StopPush();

    public native int PushAudioData(byte[] data, long ts);

    private native int PushVideoData(byte[] data, int width, int height, long ts ,int isFront, int orientation);


    public int InitMediaPublisher(FreyaStreamProcessorCallBack callback) {
        this.mStreamPusherCallBack = callback;
        return InitPusher();
    }

    public int DeinitMediaPublisher() {
        return DeinitPusher();
    }

    public int StartMediaLive(int enableVideo, int enableAudio) {
        int ret = StartPush();
        if (ret < 0)
            return ret;

        mMediaLiveStatus = 1;

        return 0;
    }

    public int StopMediaLive() {
        mMediaLiveStatus = 0;
        return StopPush();
    }

    public int GetMediaLiveStatus() {
        return mMediaLiveStatus;
    }


    public int SetVideoEncoder(int width, int height, int fps, int bitrate, String h264profile, String h264encodemode, boolean enable_hw) {
        return SetupVideoOptions(width, height, fps, bitrate, h264profile, h264encodemode);
    }

    public int OnCaptureVideoFrame(byte[] data, int width, int height, long ts, int isFront, int orientation)
    {
        if(mMediaLiveStatus <= 0)
            return 0;

        PushVideoData(data, width, height, ts, isFront, orientation);
        return 0;
    }

    public void onNativeConnecting() {
        this.mStreamPusherCallBack.onConnecting();
    }

    public void onNativeConnected() {
        this.mStreamPusherCallBack.onConnected();
    }

    public void onNativeDisconnect() {
        this.mStreamPusherCallBack.onDisconnect();
    }

    public void onNativeConnectError(int err) {
        this.mStreamPusherCallBack.onConnectError( err);
    }

}