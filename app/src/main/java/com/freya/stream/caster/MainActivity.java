package com.freya.stream.caster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.freya.stream.caster.navigationtabstrip.NavigationTabStrip;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    private ViewPager mViewPager;

    private NavigationTabStrip mAddressNavigationTabStrip;
    private NavigationTabStrip mOrientationNavigationTabStrip;
    private NavigationTabStrip mPushmodeNavigationTabStrip;
    private NavigationTabStrip mCameraNavigationTabStrip;
    private NavigationTabStrip mVideorateNavigationTabStrip;
    private NavigationTabStrip mFpsNavigationTabStrip;
    private NavigationTabStrip mSoundrateNavigationTabStrip;
    private NavigationTabStrip mSoundhzNavigationTabStrip;
    private NavigationTabStrip mEncodemodeNavigationTabStrip;
    private NavigationTabStrip mLevelNavigationTabStrip;

    private Button ready_button;
    private String[] resolution_front_final;
    private String[] resolution_back_final;
    private String[] resolution_common;
    String resolution_common_s;
    private int have_front =0;
    private int have_back  =0;

    private String mRtmp_Url = "rtmp://192.168.1.17/myapp/";    //定义RTMP推流地址
    private int mOrientation_Degree = 0;                          //定义屏幕方向   0代表竖屏,90代表横屏
    private int mCamera_Mode = 1;                                  //定义第一次预览使用的摄像头   0代表后置摄像头,1代表前置摄像头
    private int mEnable_Video = 1;                                 //定义是否推视频   0代表不推,1代表推
    private int mEnable_Sound = 1;                                 //定义是否推音频   0代表不推,1代表推
    private int mVideo_Rate = 1200;                                //定义视频码率   单位(KBPS)
    private int mFps = 20;                                          //定义视频帧率   单位(FPS)
    private int mSound_Rate = 32;                                  //定义音频码率   单位(KBPS)
    private int mSound_Hz = 44100;                                 //定义音频采样率   单位(HZ)   常用频率11025HZ 22050HZ 44100HZ  其中44100HZ兼容性最好能支持绝大多数移动设备
    private int mSound_Channels = 2;                              //定义音频声道数   1代表单声道,2代表立体声

    //在界面中自定义H264编码PROFILE,H264编码模式,AAC编码PROFILE的格式->"H264编码PROFILE代码:H264编码模式代码:AAC编码PROFILE代码"   例子1->"B:SUPERFAST:L"   例子2->"M:SUPERFAST:H"

    private String mH264_Profile = "baseline";                   //定义H264编码PROFILE   可选值baseline main high high10 high422 high444  CPU性能较弱的移动设备建议选择baseline,CPU性能较好的移动设备可选择main,不推荐使用high系列的PROFILE
    //在界面中自定义时对应代码baseline->B  main->M  high->H  high10->H10  high422->H422  high444->H444

    private String mH264_Encode_Mode = "ultrafast";             //定义H264编码模式   可选值ultrafast superfast veryfast faster fast medium slow slower veryslow placebo 自左向右编码速度从快到慢,编码质量从低到高  CPU性能较弱的移动设备建议选择较快的编码模式,以提升编码速度  CPU性能较好的移动设备可选择较慢的编码模式,以提升编码质量
    //在界面中自定义时对应代码与设置值相同

    private String mAac_Profile = "aac_lc";                      //定义AAC编码PROFILE   aac_lc aac_he aac_he_v2 兼容性最好的是aac_lc,而选择aac_he,aac_he_v2可以在同编码质量下节省带宽
    //在界面中自定义时对应代码aac_lc->L  aac_he->H  aac_he_v2->H2

    private int mVideo_Width_Front = 1280;                       //定义前置摄像头视频分辨率宽度
    private int mVideo_Height_Front = 720;                       //定义前置摄像头视频分辨率高度
    private int mVideo_Width_Back = 1280;                        //定义后置摄像头视频分辨率宽度
    private int mVideo_Height_Back = 720;                        //定义后置摄像头视频分辨率高度

    public boolean isNumeric(String str){
     Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }


    private static boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }
    public static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_FRONT = 1;
        return checkCameraFacing(CAMERA_FACING_FRONT);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasFrontFacingCamera()){
            have_front = 1;
        }
        if (hasBackFacingCamera()){
            have_back = 1;
        }

//分辨率等级排序
        if (have_front == 1) {
            Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            camera.release();
            camera = null;

            resolution_front_final = new String[supportedPreviewSizes.size()];
            String[] resolution_front = new String[supportedPreviewSizes.size()];
            int i_count = 0;
            for (Camera.Size size : supportedPreviewSizes) {
                resolution_front[i_count] = size.width + "*" + size.height;
                i_count++;
            }


            boolean backforward_f = false;
            int num_f = 0;
            for (int i_cyc = 0; i_cyc < resolution_front.length; i_cyc++) {

                if (backforward_f == true) {
                    i_cyc = i_cyc - 1;
                }

                if (resolution_front[i_cyc].equalsIgnoreCase("")) {
                    continue;
                }

                String[] temps = null;
                temps = resolution_front[i_cyc].split("\\*");
                int max_v = Integer.valueOf(temps[0]) * Integer.valueOf(temps[1]);
                int get_index = i_cyc;

                for (int i_cyc_v2 = 0; i_cyc_v2 < resolution_front.length; i_cyc_v2++) {

                    if (resolution_front[i_cyc_v2].equalsIgnoreCase("")) {
                        continue;
                    }

                    String[] temps_v2 = null;
                    temps_v2 = resolution_front[i_cyc_v2].split("\\*");

                    if (max_v <= Integer.valueOf(temps_v2[0]) * Integer.valueOf(temps_v2[1])) {
                        max_v = Integer.valueOf(temps_v2[0]) * Integer.valueOf(temps_v2[1]);
                        get_index = i_cyc_v2;
                    }
                }

                resolution_front_final[num_f] = resolution_front[get_index];
                resolution_front[get_index] = "";
                if (get_index != i_cyc) {
                    backforward_f = true;
                } else {
                    backforward_f = false;
                }
                num_f++;
            }
        }

        if (have_back == 1) {
            Camera camera_b = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            Camera.Parameters parameters_b = camera_b.getParameters();
            List<Camera.Size> supportedPreviewSizes_b = parameters_b.getSupportedPreviewSizes();
            camera_b.release();
            camera_b = null;

            resolution_back_final = new String[supportedPreviewSizes_b.size()];
            String[] resolution_back = new String[supportedPreviewSizes_b.size()];
            int i_count_b = 0;
            for (Camera.Size size_b : supportedPreviewSizes_b) {
                resolution_back[i_count_b] = size_b.width + "*" + size_b.height;
                i_count_b++;
            }


            boolean backforward_b = false;
            int num_b = 0;
            for (int i_cyc_b = 0; i_cyc_b < resolution_back.length; i_cyc_b++) {

                if (backforward_b == true) {
                    i_cyc_b = i_cyc_b - 1;
                }

                if (resolution_back[i_cyc_b].equalsIgnoreCase("")) {
                    continue;
                }

                String[] temps_b = null;
                temps_b = resolution_back[i_cyc_b].split("\\*");
                int max_v_b = Integer.valueOf(temps_b[0]) * Integer.valueOf(temps_b[1]);
                int get_index_b = i_cyc_b;

                for (int i_cyc_v2_b = 0; i_cyc_v2_b < resolution_back.length; i_cyc_v2_b++) {

                    if (resolution_back[i_cyc_v2_b].equalsIgnoreCase("")) {
                        continue;
                    }

                    String[] temps_v2_b = null;
                    temps_v2_b = resolution_back[i_cyc_v2_b].split("\\*");

                    if (max_v_b <= Integer.valueOf(temps_v2_b[0]) * Integer.valueOf(temps_v2_b[1])) {
                        max_v_b = Integer.valueOf(temps_v2_b[0]) * Integer.valueOf(temps_v2_b[1]);
                        get_index_b = i_cyc_v2_b;
                    }
                }

                resolution_back_final[num_b] = resolution_back[get_index_b];
                resolution_back[get_index_b] = "";
                if (get_index_b != i_cyc_b) {
                    backforward_b = true;
                } else {
                    backforward_b = false;
                }
                num_b++;
            }
        }
//分辨率等级排序


//获取共有分辨率
        if (have_front == 1 && have_back == 1) {
            int num_count_common = 0;
            for (int cts = 0; cts < resolution_front_final.length; cts++) {
                for (int cts_b = 0; cts_b < resolution_back_final.length; cts_b++) {
                    if (resolution_front_final[cts].equalsIgnoreCase(resolution_back_final[cts_b])) {
                        num_count_common++;
                    }
                }
            }

            resolution_common_s = "";
            resolution_common = new String[num_count_common];
            int common_num = 0;
            for (int cts = 0; cts < resolution_front_final.length; cts++) {
                for (int cts_b = 0; cts_b < resolution_back_final.length; cts_b++) {
                    if (resolution_front_final[cts].equalsIgnoreCase(resolution_back_final[cts_b])) {
                        resolution_common[common_num] = resolution_front_final[cts];
                        resolution_common_s = resolution_common_s+resolution_front_final[cts]+"|";
                        common_num++;
                    }
                }
            }
        }
//获取共有分辨率

//初始化分辨率等级
        if (have_front == 1 && have_back == 1) {
            if (resolution_common.length > 0) {
                String[] tempres = null;
                tempres = resolution_common[0].split("\\*");
                mVideo_Width_Front = Integer.valueOf(tempres[0]);
                mVideo_Height_Front = Integer.valueOf(tempres[1]);
                mVideo_Width_Back = Integer.valueOf(tempres[0]);
                mVideo_Height_Back = Integer.valueOf(tempres[1]);
            } else {
                Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
            }
        }


        if (have_front == 1 && have_back == 0) {
            if (resolution_front_final.length > 0) {
                String[] tempres = null;
                tempres = resolution_front_final[0].split("\\*");
                mVideo_Width_Front = Integer.valueOf(tempres[0]);
                mVideo_Height_Front = Integer.valueOf(tempres[1]);
                mVideo_Width_Back = 0;
                mVideo_Height_Back = 0;
            } else {
                Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
            }
        }

        if (have_front == 0 && have_back == 1) {
            if (resolution_back_final.length > 0) {
                String[] tempres = null;
                tempres = resolution_back_final[0].split("\\*");
                mVideo_Width_Front = 0;
                mVideo_Height_Front = 0;
                mVideo_Width_Back = Integer.valueOf(tempres[0]);
                mVideo_Height_Back = Integer.valueOf(tempres[1]);
            } else {
                Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
            }
        }
//初始化分辨率等级


        ready_button = (Button)findViewById(R.id.startRtmpButton);
        ready_button.setOnClickListener(OnClickChangeBtn);

        initUI();
        setUI();
    }



    View.OnClickListener OnClickChangeBtn = new View.OnClickListener(){
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            if (have_front != 0 || have_back != 0) {
                if (v.getId() == R.id.startRtmpButton) {
                    Intent intent = new Intent(MainActivity.this, PushActivity.class);
                    intent.putExtra("rtmpurl", mRtmp_Url);
                    intent.putExtra("orientation", mOrientation_Degree);
                    intent.putExtra("cameramode", mCamera_Mode);
                    intent.putExtra("enablevideo", mEnable_Video);
                    intent.putExtra("enableaudio", mEnable_Sound);
                    intent.putExtra("videorate", mVideo_Rate);
                    intent.putExtra("videofps", mFps);
                    intent.putExtra("soundrate", mSound_Rate);
                    intent.putExtra("soundhz", mSound_Hz);
                    intent.putExtra("soundchannels", mSound_Channels);
                    intent.putExtra("h264profile", mH264_Profile);
                    intent.putExtra("h264encodemode", mH264_Encode_Mode);
                    intent.putExtra("aacprofile", mAac_Profile);
                    intent.putExtra("videowidthfront", mVideo_Width_Front);
                    intent.putExtra("videoheightfront", mVideo_Height_Front);
                    intent.putExtra("videowidthback", mVideo_Width_Back);
                    intent.putExtra("videoheightback", mVideo_Height_Back);
                    intent.putExtra("resolutioncommons", resolution_common_s);
                    startActivity(intent);
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "当前设备无摄像头", Toast.LENGTH_SHORT).show();
            }
        }
    };




    private void initUI() {
        mViewPager = (ViewPager) findViewById(R.id.vp);
        mAddressNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.address);
        mOrientationNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.orientation);
        mPushmodeNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.pushmode);
        mCameraNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.camera);
        mVideorateNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.videorate);

        mFpsNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.fps);
        mSoundrateNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.soundrate);
        mSoundhzNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.soundhz);
        mEncodemodeNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.encodemode);
        mLevelNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.level);
    }

    private void setUI() {
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                final View view = new View(getBaseContext());
                container.addView(view);
                return view;
            }
        });

        mAddressNavigationTabStrip.setTabIndex(0, true);
        mOrientationNavigationTabStrip.setTabIndex(1, true);
        mPushmodeNavigationTabStrip.setTabIndex(0, true);

        if (have_front == 1 && have_back == 1) {
            mCameraNavigationTabStrip.setTabIndex(0, true);
            mCamera_Mode = 1;
        }
        if (have_front == 1 && have_back == 0) {
            mCameraNavigationTabStrip.setTabIndex(0, true);
            mCamera_Mode = 1;
        }
        if (have_front == 0 && have_back == 1) {
            mCameraNavigationTabStrip.setTabIndex(1, true);
            mCamera_Mode = 0;
        }
        if (have_front == 0 && have_back == 0) {
            mCameraNavigationTabStrip.setTabIndex(0, true);
            mCamera_Mode = 1;
        }

        mVideorateNavigationTabStrip.setTabIndex(1, true);

        mFpsNavigationTabStrip.setTabIndex(0, true);
        mSoundrateNavigationTabStrip.setTabIndex(1, true);
        mSoundhzNavigationTabStrip.setTabIndex(1, true);
        mEncodemodeNavigationTabStrip.setTabIndex(0, true);
        mLevelNavigationTabStrip.setTabIndex(3, true);


//设置RTMP推流地址开始
        mAddressNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if(index==0){
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("推流地址信息")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("推流地址: "+mRtmp_Url)
                            .setNegativeButton("确定", null)
                            .show();
                }
                if (index==1){
                    final EditText medit_text = new EditText(MainActivity.this);
                    medit_text.setTextColor(0xffffffff);
                    medit_text.setText(mRtmp_Url);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入推流地址")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();
                                    if (input.equals("rtmp://")|| input.equals("")) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的推流地址" , Toast.LENGTH_LONG).show();
                                        mRtmp_Url = "rtmp://";
                                        mAddressNavigationTabStrip.setTabIndex(0, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "推流地址已应用", Toast.LENGTH_SHORT).show();
                                        mRtmp_Url = input;
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置RTMP推流地址结束

//设置屏幕方向开始
        mOrientationNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if(index==0){
                    mOrientation_Degree = 90;  //横屏
                }
                if (index==1){
                    mOrientation_Degree = 0;  //竖屏
                }
            }
        });
//设置屏幕方向结束

//设置摄像头开始
        mCameraNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if(index==0){
                    if (have_front == 0 && have_back == 1) {
                        Toast.makeText(getApplicationContext(), "无前置摄像头" , Toast.LENGTH_LONG).show();
                        mCameraNavigationTabStrip.setTabIndex(1, true);
                        mCamera_Mode = 0;
                    }
                    else {
                        mCamera_Mode = 1;
                    }
                }

                if (index==1){
                    if (have_front == 1 && have_back == 0) {
                        Toast.makeText(getApplicationContext(), "无后置摄像头" , Toast.LENGTH_LONG).show();
                        mCameraNavigationTabStrip.setTabIndex(0, true);
                        mCamera_Mode = 1;
                    }
                    else {
                        mCamera_Mode = 0;
                    }

                }
            }
        });
//设置摄像头结束

//设置推流模式开始
        mPushmodeNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if(index==0){
                    mEnable_Video = 1;
                    mEnable_Sound = 1;
                }
                if (index==1){
                    mEnable_Video = 1;
                    mEnable_Sound = 0;
                }
                if (index==2){
                    mEnable_Video = 0;
                    mEnable_Sound = 1;
                }
            }
        });
//设置推流模式结束

//设置视频码率开始
        mVideorateNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    mVideo_Rate = 800;
                }
                if (index==1){
                    mVideo_Rate = 1200;
                }
                if (index==2){
                    final EditText medit_text = new EditText(MainActivity.this);
                    medit_text.setText(Integer.toString(mVideo_Rate));
                    medit_text.setTextColor(0xffffffff);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入视频码率参数(KBPS)")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();
                                    if (input.equals("") || !isNumeric(input)) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的视频码率参数" , Toast.LENGTH_LONG).show();
                                        mVideo_Rate = 1200;
                                        mVideorateNavigationTabStrip.setTabIndex(1, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "视频码率参数已应用", Toast.LENGTH_SHORT).show();
                                        mVideo_Rate = Integer.valueOf(input).intValue();
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置视频码率结束

//设置视频帧率开始
        mFpsNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    mFps = 20;
                }
                if (index==1){
                    mFps = 24;
                }
                if (index==2){
                    final EditText medit_text = new EditText(MainActivity.this);
                    medit_text.setText(Integer.toString(mFps));
                    medit_text.setTextColor(0xffffffff);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入视频帧率参数(FPS)")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();
                                    if (input.equals("") || !isNumeric(input)) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的视频帧率参数" , Toast.LENGTH_LONG).show();
                                        mFps = 20;
                                        mFpsNavigationTabStrip.setTabIndex(0, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "视频帧率参数已应用", Toast.LENGTH_SHORT).show();
                                        mFps = Integer.valueOf(input).intValue();
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置视频帧率结束

//设置音频码率开始
        mSoundrateNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    mSound_Rate = 24;
                }
                if (index==1){
                    mSound_Rate = 32;
                }
                if (index==2){
                    final EditText medit_text = new EditText(MainActivity.this);
                    medit_text.setText(Integer.toString(mSound_Rate));
                    medit_text.setTextColor(0xffffffff);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入音频码率参数(KBPS)")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();
                                    if (input.equals("") || !isNumeric(input)) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的音频码率参数" , Toast.LENGTH_LONG).show();
                                        mSound_Rate = 32;
                                        mSoundrateNavigationTabStrip.setTabIndex(1, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "音频码率参数已应用", Toast.LENGTH_SHORT).show();
                                        mSound_Rate = Integer.valueOf(input).intValue();
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置音频码率结束

//设置音频采样率开始
        mSoundhzNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    mSound_Hz = 22050;
                }
                if (index==1){
                    mSound_Hz = 44100;
                }
                if (index==2){
                    final EditText medit_text = new EditText(MainActivity.this);
                    medit_text.setText(Integer.toString(mSound_Hz));
                    medit_text.setTextColor(0xffffffff);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入音频采样率参数(HZ)")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();
                                    if (input.equals("") || !isNumeric(input)) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的音频采样率参数" , Toast.LENGTH_LONG).show();
                                        mSound_Hz = 44100;
                                        mSoundhzNavigationTabStrip.setTabIndex(1, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "音频采样率参数已应用", Toast.LENGTH_SHORT).show();
                                        mSound_Hz = Integer.valueOf(input).intValue();
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置音频采样率结束


//设置编码模式开始
        mEncodemodeNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    mH264_Profile = "baseline";
                    mH264_Encode_Mode = "ultrafast";
                    mAac_Profile = "aac_lc";
                }
                if (index==1){
                    mH264_Profile = "main";
                    mH264_Encode_Mode = "superfast";
                    mAac_Profile = "aac_he";
                }
                if (index==2){
                    final EditText medit_text = new EditText(MainActivity.this);

                    String setmode = "";
                    if (mH264_Profile.equalsIgnoreCase("baseline")){
                        setmode = setmode+"B:";
                    }
                    if (mH264_Profile.equalsIgnoreCase("main")){
                        setmode = setmode+"M:";
                    }
                    if (mH264_Profile.equalsIgnoreCase("high")){
                        setmode = setmode+"H:";
                    }
                    if (mH264_Profile.equalsIgnoreCase("high10")){
                        setmode = setmode+"H10:";
                    }
                    if (mH264_Profile.equalsIgnoreCase("high422")){
                        setmode = setmode+"H422:";
                    }
                    if (mH264_Profile.equalsIgnoreCase("high444")){
                        setmode = setmode+"H444:";
                    }

                    setmode = setmode+mH264_Encode_Mode.toUpperCase()+":";

                    if (mAac_Profile.equalsIgnoreCase("aac_lc")){
                        setmode = setmode+"L";
                    }
                    if (mAac_Profile.equalsIgnoreCase("aac_he")){
                        setmode = setmode+"H";
                    }
                    if (mAac_Profile.equalsIgnoreCase("aac_he_v2")){
                        setmode = setmode+"H2";
                    }

                    medit_text.setText(setmode);
                    medit_text.setTextColor(0xffffffff);
                    new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("请输入编码模式参数")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(medit_text)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = medit_text.getText().toString();

                                    String [] temp = null;
                                    temp = input.split(":");

                                    if (input.equals("") || temp.length!=3) {
                                        Toast.makeText(getApplicationContext(), "请输入正确的编码模式参数" , Toast.LENGTH_LONG).show();
                                        mH264_Profile = "baseline";
                                        mH264_Encode_Mode = "ultrafast";
                                        mAac_Profile = "aac_lc";
                                        mEncodemodeNavigationTabStrip.setTabIndex(0, true);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "编码模式参数已应用", Toast.LENGTH_SHORT).show();

                                        if (temp[0].equalsIgnoreCase("B")){
                                            mH264_Profile = "baseline";
                                        }
                                        if (temp[0].equalsIgnoreCase("M")){
                                            mH264_Profile = "main";
                                        }
                                        if (temp[0].equalsIgnoreCase("H")){
                                            mH264_Profile = "high";
                                        }
                                        if (temp[0].equalsIgnoreCase("H10")){
                                            mH264_Profile = "high10";
                                        }
                                        if (temp[0].equalsIgnoreCase("H422")){
                                            mH264_Profile = "high422";
                                        }
                                        if (temp[0].equalsIgnoreCase("H444")){
                                            mH264_Profile = "high444";
                                        }

                                        if(temp[1].equalsIgnoreCase("ULTRAFAST") || temp[1].equalsIgnoreCase("SUPERFAST")|| temp[1].equalsIgnoreCase("VERYFAST")|| temp[1].equalsIgnoreCase("FASTER")|| temp[1].equalsIgnoreCase("FAST")|| temp[1].equalsIgnoreCase("MEDIUM")|| temp[1].equalsIgnoreCase("SLOW")|| temp[1].equalsIgnoreCase("SLOWER")|| temp[1].equalsIgnoreCase("VERYSLOW")|| temp[1].equalsIgnoreCase("PLACEBO")) {
                                            mH264_Encode_Mode = temp[1].toLowerCase();
                                        }

                                        if (temp[2].equalsIgnoreCase("L")){
                                            mAac_Profile = "aac_lc";
                                        }
                                        if (temp[2].equalsIgnoreCase("H")){
                                            mAac_Profile = "aac_he";
                                        }
                                        if (temp[2].equalsIgnoreCase("H2")){
                                            mAac_Profile = "aac_he_v2";
                                        }

                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
//设置编码模式结束


//设置分辨率等级开始
        mLevelNavigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
            }
            @Override
            public void onEndTabSelected(String title, int index) {
                if (index==0){
                    if (have_front == 1 && have_back == 1) {
                        if (resolution_common.length > 3) {
                            String[] tempres = null;
                            tempres = resolution_common[3].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_common[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }


                    if (have_front == 1 && have_back == 0) {
                        if (resolution_front_final.length > 3) {
                            String[] tempres = null;
                            tempres = resolution_front_final[3].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        } else {
                            String[] tempres = null;
                            tempres = resolution_front_final[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        }
                    }

                    if (have_front == 0 && have_back == 1) {
                        if (resolution_back_final.length > 3) {
                            String[] tempres = null;
                            tempres = resolution_back_final[3].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_back_final[0].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }
                }
                if (index==1){
                    if (have_front == 1 && have_back == 1) {
                        if (resolution_common.length > 2) {
                            String[] tempres = null;
                            tempres = resolution_common[2].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_common[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }


                    if (have_front == 1 && have_back == 0) {
                        if (resolution_front_final.length > 2) {
                            String[] tempres = null;
                            tempres = resolution_front_final[2].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        } else {
                            String[] tempres = null;
                            tempres = resolution_front_final[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        }
                    }

                    if (have_front == 0 && have_back == 1) {
                        if (resolution_back_final.length > 2) {
                            String[] tempres = null;
                            tempres = resolution_back_final[2].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_back_final[0].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }
                }
                if (index==2){
                    if (have_front == 1 && have_back == 1) {
                        if (resolution_common.length > 1) {
                            String[] tempres = null;
                            tempres = resolution_common[1].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_common[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }


                    if (have_front == 1 && have_back == 0) {
                        if (resolution_front_final.length > 1) {
                            String[] tempres = null;
                            tempres = resolution_front_final[1].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        } else {
                            String[] tempres = null;
                            tempres = resolution_front_final[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        }
                    }

                    if (have_front == 0 && have_back == 1) {
                        if (resolution_back_final.length > 1) {
                            String[] tempres = null;
                            tempres = resolution_back_final[1].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            String[] tempres = null;
                            tempres = resolution_back_final[0].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        }
                    }
                }
                if (index==3){
                    if (have_front == 1 && have_back == 1) {
                        if (resolution_common.length > 0) {
                            String[] tempres = null;
                            tempres = resolution_common[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
                        }
                    }


                    if (have_front == 1 && have_back == 0) {
                        if (resolution_front_final.length > 0) {
                            String[] tempres = null;
                            tempres = resolution_front_final[0].split("\\*");
                            mVideo_Width_Front = Integer.valueOf(tempres[0]);
                            mVideo_Height_Front = Integer.valueOf(tempres[1]);
                            mVideo_Width_Back = 0;
                            mVideo_Height_Back = 0;
                        } else {
                            Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (have_front == 0 && have_back == 1) {
                        if (resolution_back_final.length > 0) {
                            String[] tempres = null;
                            tempres = resolution_back_final[0].split("\\*");
                            mVideo_Width_Front = 0;
                            mVideo_Height_Front = 0;
                            mVideo_Width_Back = Integer.valueOf(tempres[0]);
                            mVideo_Height_Back = Integer.valueOf(tempres[1]);
                        } else {
                            Toast.makeText(getApplicationContext(), "视频分辨率参数获取错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (index==4){

                    String[] resolution;

                    if (have_front == 1 && have_back == 1) {
                        if (mCamera_Mode == 1) {
                            resolution = resolution_front_final;
                        }
                        else{
                            resolution = resolution_back_final;
                        }
                    }

                    else if (have_front == 1 && have_back == 0) {
                        resolution = resolution_front_final;
                    }

                    else if (have_front == 0 && have_back == 1) {
                        resolution = resolution_back_final;
                    }
                    else{
                        resolution =  null;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    builder.setTitle("请选择视频分辨率参数");
                    if (have_front == 0 && have_back == 0) {
                        builder.setCancelable(true);
                    }
                    else {
                        builder.setCancelable(false);
                    }
                    builder.setItems(resolution, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Toast.makeText(MainActivity.this, "视频分辨率参数" + resolution[which]+"已应用", Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "视频分辨率参数已应用", Toast.LENGTH_SHORT).show();

                            if (have_front == 1 && have_back == 1) {
                                if (mCamera_Mode == 1) {
                                    int cc = which;
                                    if (which > resolution_front_final.length-1 ){
                                        cc = resolution_front_final.length-1;
                                    }
                                    String [] tempres_f = null;
                                    tempres_f = resolution_front_final[cc].split("\\*");
                                    mVideo_Width_Front = Integer.valueOf(tempres_f[0]);
                                    mVideo_Height_Front = Integer.valueOf(tempres_f[1]);

                                    String  got_close = "";
                                    int min_val = 2147483640;

                                    for (int cts = 0; cts < resolution_back_final.length; cts++) {
                                        String [] tempres_f2 = null;
                                        tempres_f2 = resolution_back_final[cts].split("\\*");
                                         if (min_val >= Math.abs(mVideo_Width_Front*mVideo_Height_Front-Integer.valueOf(tempres_f2[0])*Integer.valueOf(tempres_f2[1]))){
                                             min_val = Math.abs(mVideo_Width_Front*mVideo_Height_Front-Integer.valueOf(tempres_f2[0])*Integer.valueOf(tempres_f2[1]));
                                             got_close = resolution_back_final[cts];
                                         }
                                    }

                                    String [] tempres_f3 = null;
                                    tempres_f3 = got_close.split("\\*");
                                    mVideo_Width_Back = Integer.valueOf(tempres_f3[0]);
                                    mVideo_Height_Back = Integer.valueOf(tempres_f3[1]);

                                }
                                else{
                                    int cc = which;
                                    if (which > resolution_back_final.length-1 ){
                                        cc = resolution_back_final.length-1;
                                    }
                                    String [] tempres_f = null;
                                    tempres_f = resolution_back_final[cc].split("\\*");
                                    mVideo_Width_Back = Integer.valueOf(tempres_f[0]);
                                    mVideo_Height_Back = Integer.valueOf(tempres_f[1]);

                                    String  got_close = "";
                                    int min_val = 2147483640;

                                    for (int cts = 0; cts < resolution_front_final.length; cts++) {
                                        String [] tempres_f2 = null;
                                        tempres_f2 = resolution_front_final[cts].split("\\*");
                                        if (min_val >= Math.abs(mVideo_Width_Back*mVideo_Height_Back-Integer.valueOf(tempres_f2[0])*Integer.valueOf(tempres_f2[1]))){
                                            min_val = Math.abs(mVideo_Width_Back*mVideo_Height_Back-Integer.valueOf(tempres_f2[0])*Integer.valueOf(tempres_f2[1]));
                                            got_close = resolution_front_final[cts];
                                        }
                                    }

                                    String [] tempres_f3 = null;
                                    tempres_f3 = got_close.split("\\*");
                                    mVideo_Width_Front = Integer.valueOf(tempres_f3[0]);
                                    mVideo_Height_Front = Integer.valueOf(tempres_f3[1]);
                                }
                            }

                            else if (have_front == 1 && have_back == 0) {
                                int cc = which;
                                if (which > resolution_front_final.length-1 ){
                                    cc = resolution_front_final.length-1;
                                }
                                String [] tempres_f = null;
                                tempres_f = resolution_front_final[cc].split("\\*");
                                mVideo_Width_Front = Integer.valueOf(tempres_f[0]);
                                mVideo_Height_Front = Integer.valueOf(tempres_f[1]);
                                mVideo_Width_Back = 0;
                                mVideo_Height_Back = 0;
                            }

                            else if (have_front == 0 && have_back == 1) {
                                int cc = which;
                                if (which > resolution_back_final.length-1 ){
                                    cc = resolution_back_final.length-1;
                                }
                                String [] tempres_f = null;
                                tempres_f = resolution_back_final[cc].split("\\*");
                                mVideo_Width_Front = 0;
                                mVideo_Height_Front = 0;
                                mVideo_Width_Back = Integer.valueOf(tempres_f[0]);
                                mVideo_Height_Back = Integer.valueOf(tempres_f[1]);
                            }

                        }
                    });
                    builder.show();
                }
            }
        });
//设置分辨率等级结束


    }
}
