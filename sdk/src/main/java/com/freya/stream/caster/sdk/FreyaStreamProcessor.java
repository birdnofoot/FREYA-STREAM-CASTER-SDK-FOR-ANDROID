package com.freya.stream.caster.sdk;

import android.hardware.Camera;
import android.view.SurfaceHolder;

public class FreyaStreamProcessor {

    private FreyaStreamSDK mPublishProcessor = null;
    private FreyaCameraVideoProcessor mCameraVideoProcessor    = null;
    private FreyaAudioProcessor mAudioProcessor    = null;
    private SurfaceHolder 	  mSurfaceHolder   = null;

    private int mEnableAudio = 0;
    private int mMaxAudioReadbytes = 0;

    public int mMeidaLiveStatus    = 0;

    private static int mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public void InitMeidaLiveHelper(FreyaStreamProcessorCallBack callback) {

        mPublishProcessor = new FreyaStreamSDK();
        mPublishProcessor.InitMediaPublisher(callback);

        mCameraVideoProcessor = new FreyaCameraVideoProcessor();
        mCameraVideoProcessor.setCameraDataCallBack(mPublishProcessor);

        mAudioProcessor = new FreyaAudioProcessor();
        mAudioProcessor.setAudioDataCallBack(mPublishProcessor);


    }

    public void SetServerUrl(String url) {
        if (mPublishProcessor != null) {
            mPublishProcessor.SetupRtmpUrl(url);
        }
    }

    public void SetVideoOption(int width, int height , int orientation, int enablevideo, int videorate, int videofps, String h264profile, String h264encodemode, boolean enableHWCodec){
        if (mCameraVideoProcessor != null) {
            mCameraVideoProcessor.setCameraFormat(width, height,orientation,enablevideo);
        }
        if (mPublishProcessor != null) {
            if (orientation==90) {
                mPublishProcessor.SetVideoEncoder(width, height, videofps, videorate * 1000, h264profile, h264encodemode, enableHWCodec);
            }
            else{
                mPublishProcessor.SetVideoEncoder(height, width, videofps, videorate * 1000, h264profile, h264encodemode, enableHWCodec);
            }
        }
    }

    public void SetAudioOption(int sampleRate, int channels, int soundrate, String aacprofile) {
        if (sampleRate <= 0 || channels <= 0) {
        }

        if (mPublishProcessor != null) {
            mPublishProcessor.SetupAudioOptions(sampleRate, channels, soundrate, aacprofile);
            mMaxAudioReadbytes = 2048*channels;
            mAudioProcessor.setAudioOption(sampleRate,channels, mMaxAudioReadbytes);
        }
    }

    public void SetCameraView(SurfaceHolder sufaceHolder,int width, int height ,int orientation ,int enablevideo) {
        if (mCameraVideoProcessor != null) {
            this.mSurfaceHolder = sufaceHolder;
            mCameraVideoProcessor.setCameraViewPosition(sufaceHolder);
            mCameraVideoProcessor.setCameraFormat(width, height, orientation,enablevideo);
        }
    }

    public int GetLiveStatus() {
        return mMeidaLiveStatus;
    }

    public void StartPreviewCamera(int current_face) {

        mCameraFace = current_face;

        if (mCameraVideoProcessor != null) {
            mCameraVideoProcessor.openCamera(mCameraFace);
        }
    }

    public void StopPreviewCamera() {
        if (mCameraVideoProcessor != null) {
            mCameraVideoProcessor.closeCamera();
        }
    }

    public int StartLive(int enableVideo, int enableAudio) {
        int ret = -1;

        mEnableAudio = enableAudio;

        if (mPublishProcessor != null) {
            ret = mPublishProcessor.StartMediaLive(enableVideo, enableAudio);
            if (ret < 0) {
                return ret;
            }
            mMeidaLiveStatus = 1;
        }


        if (mEnableAudio == 1) {
            mAudioProcessor.OpenAudio(mPublishProcessor, mMaxAudioReadbytes);
        }

        return ret;
    }

    public void Stop() {
        if (mPublishProcessor != null){
            mPublishProcessor.StopMediaLive();
        }
        if (mEnableAudio == 1){
            mAudioProcessor.closeAudio();
        }
        mMeidaLiveStatus = 0;
    }

    public void SwitchFlash(int mode) {
        if (mCameraVideoProcessor != null){
            mCameraVideoProcessor.switchFlash(mode);
        }
    }

    public void SwitchCamera() {
        if (mCameraVideoProcessor != null) {
            mCameraVideoProcessor.closeCamera();
            if(mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                mCameraVideoProcessor.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            else
            {
                mCameraVideoProcessor.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                mCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
    }

    public void DeinitMeidaLiveHelper() {
        mAudioProcessor.closeAudio();
        mCameraVideoProcessor.closeCamera();
        mPublishProcessor.StopPush();
        mPublishProcessor.DeinitMediaPublisher();
        mCameraFace = 1;
    }
}
