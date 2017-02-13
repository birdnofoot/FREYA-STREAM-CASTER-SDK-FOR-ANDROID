package com.freya.stream.caster.sdk;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.view.SurfaceHolder;

public class FreyaCameraVideoProcessor implements Runnable, Camera.PreviewCallback {

    private Context mContext;
    private SurfaceHolder mSurfaceHolder = null;
    AsyncTask<Void, Void, Void> mPreviewTask = null;
    private FreyaStreamSDK mStreamPushProcessor = null;

    private Camera mCamera = null;
    private boolean mPreviewRunning = false;

    private int mVideoWidth = 1280;
    private int mVideoHeight = 720;

    private int mCameraOrientation = 0;

    private int mInputCameraFace = 0;
    private int mDeviceOrientation = 0;

    private int mCurrentCameraId = 0;
    private int mBufSize;
    private int mEnablevideo = 1;

    private Thread mThread;

    public static ArrayList<String> sRotateModel = new ArrayList<String>();

    static {
        sRotateModel.add("Nexus 6");
    }


    public void setContext(Context ctx) {
        mContext = ctx;
    }


    public void setCameraFormat(int width, int height, int orientation,int enablevideo) {
        mVideoWidth = width;
        mVideoHeight = height;
        mCameraOrientation = orientation;
        mEnablevideo = enablevideo;
    }


    public void setCameraDataCallBack(FreyaStreamSDK obj) {
        mStreamPushProcessor = obj;
    }


    public void setCameraViewPosition(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }


    public int openCamera(int cameraFace)  {
        if (mStreamPushProcessor == null) {
        }
        if (mSurfaceHolder == null) {
        }
        mInputCameraFace = cameraFace;

        this.mThread = new Thread(this, "camera");
        this.mThread.start();
        return 0;
    }

    public void run() {
        openCamera();
    }

    public int openCamera() {
        try {
            if (mPreviewRunning) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            selectVideoCapture(mInputCameraFace);
            mSurfaceHolder.setKeepScreenOn(true);
            initCamera(mSurfaceHolder, 0);

        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }

        return 0;
    }


    public void closeCamera() {
        if (null != mCamera) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                mPreviewRunning = false;
                mCamera.release();
                mCamera = null;
            } catch (Exception ex) {
                mCamera = null;
                mPreviewRunning = false;
            }
        }
    }


    public int getCameraFace() {
        return mInputCameraFace;
    }

    @SuppressLint("NewApi")
    private void initCamera(SurfaceHolder holder, int flash_mode) {
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            if (numberOfCameras > 0) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == mInputCameraFace) {
                        mCamera = Camera.open(i);
                        mCurrentCameraId = i;
                    }
                }
            } else {
                mCamera = Camera.open();
            }

            cameraAutoFocus();

            mDeviceOrientation = getDisplayOrientation(mCameraOrientation, mCurrentCameraId);
            mCamera.setDisplayOrientation(mDeviceOrientation);

            Camera.Parameters parameters = mCamera.getParameters();

            List<String> focusModesList = parameters.getSupportedFocusModes();
            for(int i=0;i<focusModesList.size();i++)
            {
            }
            if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }


            List previewSizes = this.mCamera.getParameters().getSupportedPreviewSizes();
            for (int i = 0; i < previewSizes.size(); i++) {
                Camera.Size s = (Camera.Size)previewSizes.get(i);

                if ((s.width == this.mVideoWidth) && (s.height == this.mVideoHeight)) {
                    this.mVideoWidth = s.width;
                    this.mVideoHeight = s.height;
                    parameters.setPreviewSize(s.width, s.height);
                    break;
                }
            }

            if (flash_mode == 1) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }

            List<int[]> fpsRange = parameters.getSupportedPreviewFpsRange();
            for (int i = 0; i < fpsRange.size(); i++) {
                int[] r = fpsRange.get(i);
                if (r[0] >= 25000 && r[1] >= 25000) {
                    parameters.setPreviewFpsRange(r[0], r[1]);
                    break;
                }
            }

            parameters.setPreviewFormat(ImageFormat.NV21);

            try {
                mCamera.setParameters(parameters);
            } catch (Exception e) {
            }

            Camera.Size captureSize = mCamera.getParameters().getPreviewSize();

            mBufSize = captureSize.width * captureSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
            for (int i = 0; i < 3; i++) {
                byte[] buffer = new byte[mBufSize];
                mCamera.addCallbackBuffer(buffer);
            }

            mCamera.setPreviewCallbackWithBuffer(this);
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (Exception ex) {
            }

            mCamera.startPreview();
            mPreviewRunning = true;

            if (sRotateModel.contains(Build.MODEL)) {
                mInputCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
            }

        } catch (Exception e) {
        }
    }


    public void cameraAutoFocus() {
        if (mCamera == null || !mPreviewRunning)
            return;
        try {
            mCamera.autoFocus(null);
        } catch (Exception ex) {
        }
    }


    public void switchFlash(int mode) {
        if (mPreviewRunning == false) {
            return;
        }

        if (mode == 1){
            if (mCamera == null) {
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }
            List<String> flashModes = parameters.getSupportedFlashModes();

            if (flashModes == null) {
                return;
            }
            String flashMode = parameters.getFlashMode();
            if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                } else {
                }
            }
        }
        else{
            if (mCamera == null) {
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }
            List<String> flashModes = parameters.getSupportedFlashModes();
            String flashMode = parameters.getFlashMode();
            if (flashModes == null) {
                return;
            }
            if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                } else {
                }
            }
        }

    }

    public void switchCamera() {
        if (mPreviewRunning == false) {
            return;
        }
        try {
            if (Camera.getNumberOfCameras() == 1 || mSurfaceHolder == null)
                return;
            mCurrentCameraId = (mCurrentCameraId == 0) ? 1 : 0;
            if (null != mCamera) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                mPreviewRunning = false;
                mCamera.release();
                mCamera = null;
            }

            initCamera(mSurfaceHolder, 0);
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }


    private void selectVideoCapture(int facing) {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facing) {
                mCurrentCameraId = i;
                break;
            }
        }
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        // TODO Auto-generated method stub
        if (data == null) {
            mBufSize += mBufSize / 20;
            camera.addCallbackBuffer(new byte[mBufSize]);
        } else {
            camera.addCallbackBuffer(data);
            if (mStreamPushProcessor.GetMediaLiveStatus() == 1 && mStreamPushProcessor != null) {

                if (mEnablevideo==1) {
                    mStreamPushProcessor.OnCaptureVideoFrame(data, this.mVideoWidth, this.mVideoHeight, 0L, getCameraFace(), mCameraOrientation);
                }
            }
        }
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}