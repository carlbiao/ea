package com.hadutech.glasses.engineerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dyhdyh.widget.loading.bar.LoadingBar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;

import com.hadutech.glasses.engineerapp.events.ScreenShotEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class RTCActivity extends Activity implements View.OnClickListener,View.OnTouchListener {
    boolean isVisible = false;
    private static final String TAG = "RTCActivity";
    private static final int SEEKBAR_VOLUME_MIN = 1;
    private static final int SEEKBAR_ZOOM_MIN = 1;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    private RemoteVideo remoteVideo = null;
    private GLSurfaceView remoteVideoView = null;
    private AudioManager audiomanage = null;
    private Toast toast = null;
    private SeekBar volumeSeekBar = null;
    private SeekBar zoomSeekbar = null;
    private Boolean isScreenShots = false;
//    private FrameLayout maskLayout = null;
    private ScreenShotsView screenShotsView = null;
    private RelativeLayout screenShotsContainer = null;
    private Boolean isZoom = false;
    private int zoomScale = 1;
    private Point screenSize = new Point();
    private FrameLayout parent = null;
    private int actionDownStartX;
    private int actionDownStartY;
    private MediaStream remoteMediaStream;
    private MediaProjectionManager mMediaProjectionManager = null;
    private Handler screenshotHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            closeScreenshot();
            LoadingBar.cancel(parent);
        }
    };

    /**
     * 处理所有逻辑的Handler
     */
    @SuppressLint("HandlerLeak")
    private Handler rtcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == RtcClient.RTC_MESSAGE_TYPE_JOIN_COMPLETE) {
                //信令服务器连接成功回调
                toast.setText("连接信令服务器成功");
                toast.show();
                //Toast.makeText(RTCActivity.this, "", Toast.LENGTH_SHORT).show();
            } else if (msg.what == RtcClient.RTC_MESSAGE_TYPE_ONLINE_ENGINEER_LIST) {
                //获取工程师（们）在线状态回调
                JSONObject jsonObject = (JSONObject) msg.obj;
                Log.e("MainActivity", jsonObject.toString());

            }else if(msg.what == RtcClient.RTC_MESSAGE_TYPE_ICECONNECTIONCHANGE){
                //视频通话状态变更回调
                onIceStatusChanged(String.valueOf(msg.obj));
            }else if(msg.what == RtcClient.RTC_MESSAGE_TYPE_RECEIVE_MESSAGE){
                String content = String.valueOf(msg.obj);
                JsonObject msgJson = new Gson().fromJson(content, JsonObject.class);
                String messageType = msgJson.get("type").getAsString();
                String message = msgJson.get("content").getAsString();
                if(messageType.equals("base64")){

                    Log.e(TAG,message);
                }else{
                    //收到文本信息
                    Log.e(TAG,message);
                }
            }else if(msg.what == RtcClient.RTC_MESSAGE_TYPE_RECEIVE_REMOTE_VIDEO){
//                maskLayout.setVisibility(View.GONE);
                remoteMediaStream = (MediaStream) msg.obj;

                VideoRenderer.Callbacks remoteRender = VideoRendererGuiCustom.create(0, 0, 100, 100, VideoRendererGuiCustom.ScalingType.SCALE_ASPECT_FILL, false);
                remoteMediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
            }else if(msg.what == RtcClient.RTC_MESSAGE_TYPE_CALL){
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(RTCActivity.class.getName(),"onCreate");
        //全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //显示屏常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rtc);
        initView();
        EventBus.getDefault().register(this);
        //检查权限
        if(!checkPermissions()){
//            new AlertDialog.Builder(this)
//                    .setTitle("权限不够")
//                    .setMessage("请检查网络、摄像头和麦克风权限！")
//                    .create().show();
            ActivityCompat.requestPermissions(RTCActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSION_REQUEST_CODE);
            return;
        }


        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        remoteVideo = RemoteVideo.toRemoteVideo(bd);


        bindEvent();
        RtcClient.getInstance().setRtcHandler(rtcHandler);
        //开始应答工程师端
        RtcClient.getInstance().startCamera(RTCActivity.this,null,false,true,1280,720);
        RtcClient.getInstance().startAnswer(remoteVideo);


    }

   // private ScreenRecorder mRecorder = null;

    //录屏核心代码
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        // video size
        final int width = 1280;
        final int height = 720;
        File file = new File(Environment.getExternalStorageDirectory(),
                "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
        final int bitrate = 6000000;
        mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
        mRecorder.start();
        mButton.setText("Stop Recorder");
        Toast.makeText(this, "Screen recorder is running...", Toast.LENGTH_SHORT).show();
        moveTaskToBack(true);**/
    }

    /**
     * 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            //检查权限
            if(!checkPermissions()){
//            new AlertDialog.Builder(this)
//                    .setTitle("权限不够")
//                    .setMessage("请检查网络、摄像头和麦克风权限！")
//                    .create().show();
                ActivityCompat.requestPermissions(RTCActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSION_REQUEST_CODE);
                return;
            }
            Intent intent = getIntent();
            Bundle bd = intent.getExtras();
            remoteVideo = RemoteVideo.toRemoteVideo(bd);


            bindEvent();
            RtcClient.getInstance().setRtcHandler(rtcHandler);
            //开始应答工程师端
            RtcClient.getInstance().startCamera(RTCActivity.this,null,false,true,1280,720);
            RtcClient.getInstance().startAnswer(remoteVideo);
        }
    }

    /**
     * 初始化组件相关
     */
    private void initView(){
        if(audiomanage == null){
            audiomanage = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        toast = Toast.makeText(RTCActivity.this,"",Toast.LENGTH_SHORT);
        volumeSeekBar = findViewById(R.id.sb_rtc_volume);
        zoomSeekbar = findViewById(R.id.sb_rtc_zoom);
        remoteVideoView = (GLSurfaceView) findViewById(R.id.glsv_rtc);
        VideoRendererGuiCustom.setView(remoteVideoView, null);
//        maskLayout = findViewById(R.id.fm_rtc_mask);
        screenShotsView = findViewById(R.id.ssv_rtc_screenshots);
        screenShotsContainer = findViewById(R.id.rl_rtc_imagecut);

        //TODO 实际使用免提true
        audiomanage.setSpeakerphoneOn(false);//使用免提

//        Log.e(TAG,"audio Mode:" + audiomanage.getMode());
//        Log.e(TAG,"isSpeakerphoneOn:" + audiomanage.isSpeakerphoneOn());
        //Log.e(TAG,"STREAM_ACCESSIBILITY:" + audiomanage.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY));
//        Log.e(TAG,"STREAM_ALARM:" + audiomanage.getStreamVolume(AudioManager.STREAM_ALARM));
//        Log.e(TAG,"STREAM_DTMF:" + audiomanage.getStreamVolume(AudioManager.STREAM_DTMF));
//        Log.e(TAG,"STREAM_MUSIC:" + audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC));
//        Log.e(TAG,"STREAM_VOICE_CALL:" + audiomanage.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
//        Log.e(TAG,"STREAM_VOICE_CALL_MAX:" + audiomanage.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        volumeSeekBar.setMax(audiomanage.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        volumeSeekBar.setProgress(audiomanage.getStreamVolume(AudioManager.STREAM_VOICE_CALL));

        getWindowManager().getDefaultDisplay().getSize(screenSize);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenSize.x,screenSize.y);
        remoteVideoView.setLayoutParams(layoutParams);
        parent = findViewById(R.id.fl_rtc_container);
    }

    /**
     * 绑定事件
     */
    private void bindEvent(){
        Button button = (Button) findViewById(R.id.btn_rtc_detail);
        button.setOnClickListener(this);

        ImageView screenShotImageView = findViewById(R.id.img_rtc_screenshorts);
        screenShotImageView.setOnClickListener(this);

        ImageView hangupImageView = findViewById(R.id.btn_rtc_hangup);
        hangupImageView.setOnClickListener(this);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e(TAG,"onProgressChanged:" + progress);
                if(progress == 0){
                    audiomanage.setStreamVolume(AudioManager.STREAM_VOICE_CALL,1,AudioManager.FLAG_PLAY_SOUND);
                    volumeSeekBar.setProgress(1);
                }else{
                    //audiomanage.setStreamMute(AudioManager.STREAM_VOICE_CALL,false);
                    audiomanage.setStreamVolume(AudioManager.STREAM_VOICE_CALL,progress,AudioManager.FLAG_PLAY_SOUND);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        zoomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e(TAG,"onProgressChanged" + String.valueOf(progress));

                isZoom = (progress != 0);
                zoomScale = progress + 1;
//                remoteVideoView.setScaleX(zoomScale);
//                remoteVideoView.setScaleY(zoomScale);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) remoteVideoView.getLayoutParams();
                params.gravity = Gravity.LEFT | Gravity.TOP;
                params.height = zoomScale * screenSize.y;
                params.width = zoomScale * screenSize.x;

                int dTop = (zoomScale * screenSize.y - screenSize.y)/2;
                int dLeft = (zoomScale * screenSize.x - screenSize.x)/2;
                params.leftMargin = dLeft*(-1);
                params.topMargin = dTop*(-1);



                remoteVideoView.setLayoutParams(params);

                //remoteVideoView.layout(dLeft*(-1), dTop*(-1), dLeft*(-1) + params.width, dTop*(-1) + params.height);




            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        remoteVideoView.setOnTouchListener(this);

        ImageView sendImage = findViewById(R.id.img_rtc_send);
        sendImage.setOnClickListener(this);

        ImageView cancleImage = findViewById(R.id.img_rtc_cancle);
        cancleImage.setOnClickListener(this);

    }




    @Override
    protected void onStart(){
        super.onStart();
        Log.e(RTCActivity.class.getName(),"onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        RtcClient.getInstance().onResume();
        isVisible = true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        RtcClient.getInstance().onPause();
        isVisible = false;
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.e(TAG,"onStop");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        //RtcClient.getInstance().onDestroy();
        if(remoteMediaStream != null){
            remoteMediaStream.removeTrack(remoteMediaStream.audioTracks.get(0));
            remoteMediaStream.removeTrack(remoteMediaStream.videoTracks.get(0));
        }

        EventBus.getDefault().unregister(this);
    }

    private void showLoading(int type){
        ExLoadingFactory factory = new ExLoadingFactory(type);
        LoadingBar.make(parent,factory).show();
    }



    /**
     * 检查权限
     */
    private boolean checkPermissions(){
        return isAudioAvailable() && isWifiConnected();

    }


    /**
     * 声音检测
     * @return
     */
    private boolean isAudioAvailable(){
        TextView videoStatusView = null;
        TextView audioStatusView = null;
        audioStatusView = (TextView) findViewById(R.id.tv_rtc_audio_status);
        videoStatusView = (TextView) findViewById(R.id.tv_rtc_video_status);
        boolean available = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if(available){
            videoStatusView.setBackgroundResource(R.drawable.rtc_status_tv_ok);
            audioStatusView.setBackgroundResource(R.drawable.rtc_status_tv_ok);
        }else{
            videoStatusView.setBackgroundResource(R.drawable.rtc_status_tv_false);
            audioStatusView.setBackgroundResource(R.drawable.rtc_status_tv_false);
        }
        return available;
    }

    /**
     * 判断wifi是否可用
     * @return
     */
    private boolean isWifiConnected() {
        TextView wifiStatusView = null;
        wifiStatusView = (TextView) findViewById(R.id.tv_rtc_wifi_status);
        boolean available = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED){
            wifiStatusView.setBackgroundResource(R.drawable.rtc_status_tv_false);
            return false;
        }

        ConnectivityManager mConnectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            available = mWiFiNetworkInfo.isAvailable();
        }
        if(available){
            wifiStatusView.setBackgroundResource(R.drawable.rtc_status_tv_ok);
        }else{
            wifiStatusView.setBackgroundResource(R.drawable.rtc_status_tv_false);
        }

        return available;
    }

    public void onIceStatusChanged(String newStatus) {
        if (newStatus.equals("CONNECTED")) {
            Log.e(TAG,"通话连接成功");
        } else if (newStatus.equals("FAILED")) {
            //连接失败
            Log.e(TAG,"通话连接失败");
        } else if (newStatus.equals("CLOSED")) {
            //Log.e(TAG,"通话结束");
            Toast.makeText(RTCActivity.this,"视频已中断",Toast.LENGTH_SHORT).show();
            RtcClient.getInstance().hungup();
            finish();
        } else if (newStatus.equals("CHECKING")) {
            //互相检查媒体信息，说明通话连接握手成功

        }
    }


    private void startScreenShort(){
        showLoading(ExLoadingFactory.TYPE_SCREEN_SHOT);
        //Log.e(TAG,"top:" + remoteVideoView.getTop() + "   left:" + remoteVideoView.getLeft());
//        ;
        int[] location = new int[2];
        int[] location2 = new int[2];
        remoteVideoView.getLocationOnScreen(location);
        remoteVideoView.getLocationInWindow(location2);
        Rect rect = new Rect();
        remoteVideoView.getLocalVisibleRect(rect);
        //VideoRendererGuiCustom.takePic(rect.left,rect.top*(-1));
        //TODO 需要解决缩放以后的截图问题
        VideoRendererGuiCustom.takePic(0,0);
        //VideoRendererGuiCustom.takePic(rect.left,remoteVideoView.getHeight() - rect.bottom);
        //Log.e(TAG,"isisj");

    }

    //接收截图事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userEventBus(ScreenShotEvent event){
        LoadingBar.cancel(parent);
        //启动接听列表Activity
        Bitmap bitmap = event.getBitmap();
        Log.e(TAG,"Get ScreenShot");

        isScreenShots = true;
        remoteVideoView.setVisibility(View.GONE);
        screenShotsView.setBitmap(bitmap);
        screenShotsView.reset();
        screenShotsContainer.setVisibility(View.VISIBLE);
//        screenShotsView.invalidate();
    }

    private void closeScreenshot(){
        remoteVideoView.setVisibility(View.VISIBLE);
        screenShotsContainer.setVisibility(View.GONE);
        isScreenShots = false;
    }

    @Override
    public void onBackPressed() {
        if(isScreenShots){
            closeScreenshot();
        }else{
            RtcClient.getInstance().hungup();
            super.onBackPressed();
        }
        // super.onBackPressed();//注释掉这行,back键不退出activity


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_rtc_hangup:
                //Log.e(TAG,"挂断");
                RtcClient.getInstance().hungup();
                finish();
                break;
            case R.id.btn_rtc_detail:
                //Log.e(TAG,"详情");
                Log.e(TAG,"详情：" + remoteVideo.getId());
                Intent intent = new Intent(RTCActivity.this,IssueCodeActivity.class);
                intent.putExtra("code",remoteVideo.getId());
                intent.putExtra("detailType","calling");
                intent.putExtra("readStatus",true);
                startActivity(intent);
                break;
            case R.id.img_rtc_screenshorts:
                Log.e(TAG,"截图");
                startScreenShort();
                break;
            case R.id.img_rtc_cancle:
                closeScreenshot();
                break;
            case R.id.img_rtc_send:
                showLoading(ExLoadingFactory.TYPE_SEND_MESSAGE);
                sendImage();
                break;
        }
    }

    /**
     * 发送图片
     */
    private void sendImage() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String imageContent = "data:image/png;base64," + screenShotsView.getBase64ImageContent();
                        RtcClient.getInstance().sendImageToPeer(imageContent);
                        screenshotHandler.sendEmptyMessage(0);
                        //TODO 调用http接口保存该条记录，目前接口提示文件过大
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("ref_code", remoteVideo.getId());
                        params.put("message", imageContent);
                        params.put("message_type", 2);
                        HttpUtil.doPost(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/im/save",params,new Callback() {


                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e(TAG,"invoke 2.12 远程指导即时通信发送消息失败");

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String statusMsg = response.body().string();
                                try {
                                    //解析json
                                    JSONObject statusObj = new JSONObject(statusMsg);
                                    JSONObject resMsg=statusObj.optJSONObject("result");
                                    boolean status=resMsg.optBoolean("status");
                                    if((Boolean) statusObj.get("ret") && (Boolean) resMsg.get("status")){
                                        Log.e(TAG,"save image message ok!");
                                    }
                                    else{
                                        Log.e(TAG,resMsg.getString("msg"));
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!isZoom){
            return true;
        }
        if(v.getId() == R.id.glsv_rtc){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:// 获取手指第一次接触屏幕
                    actionDownStartX = (int) event.getRawX();
                    actionDownStartY = (int) event.getRawY();


//                    int dTop = (scale*remoteVideoView.getHeight() - remoteVideoView.getHeight())/2;
//                    int dLeft = (scale*remoteVideoView.getWidth() - remoteVideoView.getWidth())/2;
//                    remoteVideoView.layout(dLeft, dTop, r + dLeft, b + dTop);
//                    ViewGroup.LayoutParams layoutParams = remoteVideoView.getLayoutParams();
//                    layoutParams.height = orignHeight*scale;
//                    layoutParams.width = orignWidth*scale;
//                    remoteVideoView.setLayoutParams(layoutParams);

//
                    Log.e(TAG,"startX,startY: " + String.valueOf(actionDownStartX)+" , "+String.valueOf(actionDownStartY));
//                    Log.e(TAG,"left,right,top,bottom: " + String.valueOf(l)+" , "+String.valueOf(r) + " , " + String.valueOf(t) + " , " + String.valueOf(b));
                    break;
                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动对应的事件
                    int x = (int) event.getRawX();
                    int y = (int) event.getRawY();
                    // 获取手指移动的距离
                    int dx = x - actionDownStartX;
                    int dy = y - actionDownStartY;

                    //Log.e(TAG,"moveX,moveY: " + String.valueOf(x)+" , "+String.valueOf(y));
                    //Log.e(TAG,"dx,dy: " + String.valueOf(dx)+" , "+String.valueOf(dy));

                    // 得到imageView最开始的各顶点的坐标
                    int l = remoteVideoView.getLeft();
                    //remoteVideoView.get
                    int r = remoteVideoView.getRight();
                    int t = remoteVideoView.getTop();
                    int b = remoteVideoView.getBottom();
                    //Log.e(TAG,"left,right,top,bottom: " + String.valueOf(l)+" , "+String.valueOf(r) + " , " + String.valueOf(t) + " , " + String.valueOf(b));

                    //checkOffset(dx,dy);
                    // 更改imageView在窗体的位置
                    remoteVideoView.layout(l + dx, t + dy, r + dx, b + dy);
                    // 获取移动后的位置
                    actionDownStartX = (int) event.getRawX();
                    actionDownStartY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:// 手指离开屏幕对应事件
                    // 记录最后图片在窗体的位置
//                    int lasty = iv_dv_view.getTop();
//                    int lastx = iv_dv_view.getLeft();
//                    iv_dv_view.setImageResource(R.drawable.next);
//                    SharedPreferences.Editor editor = sp.edit();
//                    editor.putInt("lasty", lasty);
//                    editor.putInt("lastx", lastx);
//                    editor.commit();
                    checkOffset();
                    break;
            }
        }
        return true;
    }

    private void checkOffset(){
        // 得到imageView最开始的各顶点的坐标
        int l = remoteVideoView.getLeft();
        int r = remoteVideoView.getRight();
        int t = remoteVideoView.getTop();
        int b = remoteVideoView.getBottom();
        Log.e(TAG,"left,right,top,bottom: " + String.valueOf(l)+" , "+String.valueOf(r) + " , " + String.valueOf(t) + " , " + String.valueOf(b));

        int dTop = (zoomScale * remoteVideoView.getHeight() - remoteVideoView.getHeight())/2;
        int dLeft = (zoomScale * remoteVideoView.getWidth() - remoteVideoView.getWidth())/2;



        boolean needReLayout = false;//是否越界了需要修正
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) remoteVideoView.getLayoutParams();

        if(t > 0){
            needReLayout = true;
            Log.e(TAG,"上面越界！");
            layoutParams.topMargin = 0;
            //layoutParams.

            //Log.e(TAG,"dTop,dLeft: " + String.valueOf(dTop)+" , "+String.valueOf(dLeft));
            //Log.e(TAG,"left,right,top,bottom: " + String.valueOf(l)+" , "+String.valueOf(r) + " , " + String.valueOf(t) + " , " + String.valueOf(b));
        }
        if(b < screenSize.y){
            needReLayout = true;
            Log.e(TAG,"下面越界！");
            layoutParams.topMargin = (remoteVideoView.getHeight() - screenSize.y)*(-1);
            //Log.e(TAG,"dTop,dLeft: " + String.valueOf(dTop)+" , "+String.valueOf(dLeft));
            //Log.e(TAG,"left,right,top,bottom: " + String.valueOf(l)+" , "+String.valueOf(r) + " , " + String.valueOf(t) + " , " + String.valueOf(b));
        }
        if(l > 0){
            Log.e(TAG,"左面越界！");
            needReLayout = true;
            layoutParams.leftMargin = 0;
//            l = dLeft;
//            r = r-(l-dLeft);
        }
        if((l*(-1)) > (remoteVideoView.getWidth()-screenSize.x)){
            needReLayout = true;
            Log.e(TAG,"右面越界！");
            layoutParams.leftMargin = (remoteVideoView.getWidth()-screenSize.x)*(-1);
        }
        if(needReLayout){
            remoteVideoView.setLayoutParams(layoutParams);
        }
    }
}
