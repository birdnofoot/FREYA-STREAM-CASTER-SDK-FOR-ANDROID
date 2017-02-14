package com.freya.stream.caster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.freya.stream.caster.sdk.FreyaStreamProcessorCallBack;
import com.freya.stream.caster.sdk.FreyaStreamProcessor;

public class PushActivity extends Activity implements Callback {

    private FreyaStreamProcessor mStreamProcessor = null;

    private Button mPreviewBtn = null;

    private Button mLiveCtrlBtn = null;
    private Button mChangeCameraBtn = null;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;

    private int isPreviewCamera = 0;
    private int isFlashCamera = 0;

    private int m_enableAudio = 1;
    private int m_enableVideo = 1;

    private String mRtmpUrl = null;

    private int mOrientation = 0;

    private int mCameraMode = 1;

    private int mVideoRate = 1200;

    private int mVideoFps = 2;

    private int mSoundRate = 32;

    private int mSoundHz = 44100;

    private int mSoundChannels = 2;

    private String mH264Profile = null;

    private String mH264EncodeMode = null;

    private String mAacProfile = null;

    private int mVideoWidthFront = 1280;

    private int mVideoHeightFront = 720;

    private int mVideoWidthBack = 1280;

    private int mVideoHeightBack = 720;

    private String mResolutionCommonS = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();


        String stringUrl = intent.getStringExtra("rtmpurl");
        mOrientation = intent.getIntExtra("orientation",0);
        mCameraMode = intent.getIntExtra("cameramode",1);
        int stringEnableVideo = intent.getIntExtra("enablevideo",1);
        int stringEnableAudio = intent.getIntExtra("enableaudio",1);
        mVideoRate = intent.getIntExtra("videorate",1200);
        mVideoFps = intent.getIntExtra("videofps",2);
        mSoundRate = intent.getIntExtra("soundrate",32);
        mSoundHz = intent.getIntExtra("soundhz",44100);
        mSoundChannels = intent.getIntExtra("soundchannels",2);
        mH264Profile = intent.getStringExtra("h264profile");
        mH264EncodeMode = intent.getStringExtra("h264encodemode");
        mAacProfile = intent.getStringExtra("aacprofile");
        mVideoWidthFront = intent.getIntExtra("videowidthfront",1280);
        mVideoHeightFront = intent.getIntExtra("videoheightfront",720);
        mVideoWidthBack = intent.getIntExtra("videowidthback",1280);
        mVideoHeightBack = intent.getIntExtra("videoheightback",720);
        mResolutionCommonS = intent.getStringExtra("resolutioncommons");


        setContentView(R.layout.activity_push);

        if (mOrientation==90) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mPreviewBtn = (Button) findViewById(R.id.flashlight_camera);
        mPreviewBtn.setOnClickListener(OnClickChangeBtn);

        mLiveCtrlBtn = (Button) findViewById(R.id.live_control);
        mLiveCtrlBtn.setOnClickListener(OnClickChangeBtn);

        mChangeCameraBtn = (Button) findViewById(R.id.camera_change);
        mChangeCameraBtn.setOnClickListener(OnClickChangeBtn);

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surface);



        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



        if (stringEnableAudio==1) {
            m_enableAudio = 1;
        }
        else{
            m_enableAudio = 0;
        }

        if (stringEnableVideo==1) {
            m_enableVideo = 1;
        }
        else{
            m_enableVideo = 0;
        }

        if (stringUrl.length() > 7) {
            mRtmpUrl = stringUrl;
        }

        mStreamProcessor = new FreyaStreamProcessor();

        mStreamProcessor.InitMeidaLiveHelper(new FreyaStreamProcessorCallBack() {

            @Override
            public void onDisconnect() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onConnecting() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onConnected() {
                // TODO Auto-generated method stub
            }
            public void onConnectError(int err){
                // TODO Auto-generated method stub
            }
        });

        if (mCameraMode == 1) {
            mStreamProcessor.SetCameraView(mSurfaceHolder, mVideoWidthFront, mVideoHeightFront, mOrientation, m_enableVideo);
        }
        else{
            mStreamProcessor.SetCameraView(mSurfaceHolder, mVideoWidthBack, mVideoHeightBack, mOrientation, m_enableVideo);
        }

    }

    OnClickListener OnClickChangeBtn = new OnClickListener() {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.camera_change) {
                if (mVideoWidthFront == mVideoWidthBack && mVideoHeightFront == mVideoHeightBack) {
                    mStreamProcessor.SwitchCamera();
                }
                else{
                    if (mVideoWidthFront == 0 || mVideoHeightFront == 0 || mVideoWidthBack == 0 || mVideoHeightBack == 0) {
                        Toast.makeText(getApplicationContext(), "要切换的摄像头不存在", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (mCameraMode == 1) {
                            if (mResolutionCommonS.indexOf(mVideoWidthFront+"*"+mVideoHeightFront)!=-1){
                                mStreamProcessor.SwitchCamera();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "要切换的摄像头不支持当前分辨率", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            if (mResolutionCommonS.indexOf(mVideoWidthBack+"*"+mVideoHeightBack)!=-1){
                                mStreamProcessor.SwitchCamera();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "要切换的摄像头不支持当前分辨率", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            } else if (v.getId() == R.id.live_control) {

                RunPublisherHelper();
            } else if (v.getId() == R.id.flashlight_camera) {
                if (isFlashCamera == 0) {
                    mStreamProcessor.SwitchFlash(1);
                    isFlashCamera = 1;

                } else {
                    mStreamProcessor.SwitchFlash(0);
                    isFlashCamera = 0;

                }
            }
        }
    };


    @Override
    public void onBackPressed() {
        mStreamProcessor.SwitchFlash(0);
        mStreamProcessor.Stop();
        super.onBackPressed();
    }

    private void RunPublisherHelper() {
        int ret = -1;

        if (mStreamProcessor.GetLiveStatus() == 0) {

            mStreamProcessor.SetServerUrl(mRtmpUrl);
            if (mCameraMode == 1) {
                mStreamProcessor.SetVideoOption(mVideoWidthFront, mVideoHeightFront, mOrientation, m_enableVideo, mVideoRate, mVideoFps, mH264Profile, mH264EncodeMode, false);
            }
            else{
                mStreamProcessor.SetVideoOption(mVideoWidthBack, mVideoHeightBack, mOrientation, m_enableVideo, mVideoRate, mVideoFps, mH264Profile, mH264EncodeMode, false);
            }
            mStreamProcessor.SetAudioOption(mSoundHz, mSoundChannels, mSoundRate*1000, mAacProfile);

            ret = mStreamProcessor.StartLive(m_enableVideo, m_enableAudio);

            if (ret < 0) {
                mLiveCtrlBtn.setText("错误");
                mStreamProcessor.Stop();
            } else {
                mLiveCtrlBtn.setText("停止");
            }
        } else {
            mLiveCtrlBtn.setText("推流");
            mStreamProcessor.Stop();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mStreamProcessor.DeinitMeidaLiveHelper();

        super.onDestroy();
    }

    @SuppressLint("NewApi") @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (mCameraMode==1){
            mStreamProcessor.StartPreviewCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        else {
            mStreamProcessor.StartPreviewCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        isPreviewCamera = 1;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    }
}