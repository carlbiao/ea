package com.hadutech.glasses.engineerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.IOException;

import com.hadutech.glasses.engineerapp.R;
import okhttp3.Call;
import okhttp3.Response;


public class RTCActivity extends Activity {
    boolean isVisible = false;
    private static final String TAG = "RTCActivity";

    private RemoteVideo remoteVideo = null;
    private GLSurfaceView remoteVideoView = null;

    /**
     * 处理所有逻辑的Handler
     */
    @SuppressLint("HandlerLeak")
    private Handler rtcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == RtcClient.RTC_MESSAGE_TYPE_JOIN_COMPLETE) {
                //信令服务器连接成功回调
                Toast.makeText(RTCActivity.this, "连接服务器成功", Toast.LENGTH_SHORT).show();
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
                    //TODO 收到图片的Base64字符串，请处理到图片控件上
                    //收到图片
                    Log.e(TAG,message);
                }else{
                    //收到文本信息
                    Log.e(TAG,message);
                }
            }else if(msg.what == RtcClient.RTC_MESSAGE_TYPE_RECEIVE_REMOTE_VIDEO){
                MediaStream mediaStream = (MediaStream) msg.obj;
                VideoRenderer.Callbacks remoteRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
                //localVideoTrack.addRenderer(new VideoRenderer(localRender));
                mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
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

        //检查权限
        if(!checkPermissions()){
            new AlertDialog.Builder(this)
                    .setTitle("权限不够")
                    .setMessage("请检查网络、摄像头和麦克风权限！")
                    .create().show();
            return;
        }

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();

        remoteVideo = RemoteVideo.toRemoteVideo(bd);

        bindEvent();

        initRtcView();
        //开始应答工程师端
        RtcClient.getInstance().startCamera(RTCActivity.this,null,true,1280,720);
        RtcClient.getInstance().connect(rtcHandler, "彪彪","12345678", RtcClient.RTC_CLIENT_TYPE_EMPLOYEE);
        RtcClient.getInstance().startAnswer(remoteVideo);
    }

    /**
     * 初始化RTC视频播放相关
     */
    private void initRtcView(){
        remoteVideoView = (GLSurfaceView) findViewById(R.id.videoView);
        VideoRendererGui.setView(remoteVideoView, null);
    }

    /**
     * 绑定事件
     */
    private void bindEvent(){
        Button button = (Button) findViewById(R.id.detailButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(RTCActivity.this,SecondActivity.class));
            }
        });

        button = (Button) findViewById(R.id.voiceButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RTCActivity.this,RTCActivity.class));
            }
        });

        button = (Button) findViewById(R.id.cutButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RTCActivity.this,RemoteVideoListActivity.class));
            }
        });
    }


    @Override
    protected void onStart(){
        super.onStart();
        Log.e(RTCActivity.class.getName(),"onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(RTCActivity.class.getName(),"onResume");
        isVisible = true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.e(RTCActivity.class.getName(),"onPause");
        isVisible = false;
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.e(RTCActivity.class.getName(),"onStop");
    }

//    @Override
//    protected void onDestory(){
//        super.onDestroy();
//        Log.e(RTCActivity.class.getName(),"onDestroy");
//    }



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
        TextView txtView = null;
        TextView videoStatusView = null;
        txtView = (TextView) findViewById(R.id.audioStatus);
        videoStatusView = (TextView) findViewById(R.id.videoStatus);
        boolean available = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if(available){
            txtView.setTextColor(Color.parseColor("#17771D"));
            txtView.setText("良好");
            videoStatusView.setTextColor(Color.parseColor("#17771D"));
            videoStatusView.setText("良好");
        }else{
            txtView.setTextColor(Color.parseColor("#FF0000"));
            txtView.setText("未授权");
            videoStatusView.setTextColor(Color.parseColor("#FF0000"));
            videoStatusView.setText("未授权");
        }
        return available;
    }

    /**
     * 判断wifi是否可用
     * @return
     */
    private boolean isWifiConnected() {
        TextView txtView = null;
        txtView = (TextView) findViewById(R.id.wifiStatus);
        boolean available = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED){
            txtView.setTextColor(Color.parseColor("#FF0000"));
            txtView.setText("未授权");
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
            txtView.setTextColor(Color.parseColor("#17771D"));
            txtView.setText("良好");
        }else{
            txtView.setTextColor(Color.parseColor("#FF0000"));
            txtView.setText("不可用");
        }

        return available;
    }

    public void onIceStatusChanged(String newStatus) {
        if (newStatus.equals("COMPLETED")) {
            Log.e(TAG,"通话连接成功");
        } else if (newStatus.equals("FAILED")) {
            //连接失败
            Log.e(TAG,"通话连接失败");
        } else if (newStatus.equals("CLOSED")) {
            Log.e(TAG,"通话结束");
        } else if (newStatus.equals("CHECKING")) {
            //互相检查媒体信息，说明通话连接握手成功

        }
    }



}
