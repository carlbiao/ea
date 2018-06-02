package com.hadutech.glasses.engineerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import com.hadutech.glasses.engineerapp.R;

public class RemoteVideoListActivity extends AppCompatActivity {

    private int showType = 1; //1：有工程师call    2：查看历史
    private final LinkedList<RemoteVideo> list = new LinkedList<>();

    private MediaPlayer mediaPlayer = null;
    private AlertDialog.Builder alertBuilder = null;
    private RemoteVideoAdapter adapter = null;
    Handler _handler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_remote_vedio_list);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null){
            //需要call
            appendCall(bd.getString("personId"),bd.getString("name"),bd.getString("remoteSocketId"),null);
        }
        initView();
        initRtcSocket();
    }

    /**
     * 初始化界面
     */
    private void initView(){
        adapter = new RemoteVideoAdapter(list,RemoteVideoListActivity.this);
        ListView listView = (ListView) findViewById(R.id.listview);
        adapter.setOnInnerItemOnClickListener(new RemoteVideoAdapter.InnerItemOnclickListener() {
            @Override
            public void itemClick(View v) {
                RemoteVideo remoteVideo = (RemoteVideo) v.getTag();
                if(v.getId() == R.id.button_answer){
                    answerCall(remoteVideo);
                }else if(v.getId() == R.id.button_hangup){
                    Toast.makeText(getBaseContext(),"挂断",Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setAdapter(adapter);
    }

    private void answerCall(RemoteVideo remoteVideo){

//        Toast.makeText(getBaseContext(),"接听，来自：" + remoteVideo.getName(),Toast.LENGTH_SHORT).show();
        stopAlarm();
        //打开视频窗口
        Intent intent = new Intent(RemoteVideoListActivity.this,RTCActivity.class);
        Bundle bundle = remoteVideo.toBundle();
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 响铃
     */
    private void startAlarm() {
        //TODO 处理IllegalStateException异常
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());
            mediaPlayer.setLooping(true);
        }
        try {
            mediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void stopAlarm(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        Log.e(RTCActivity.class.getName(),"onPause");
    }

    final Handler addItemHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            RemoteVideo item = new RemoteVideo();
            item.setId("18373918700");
            item.setName("燕青");
            item.setTime((String) DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()));
            adapter.add(item);
        }
    };

    /**
     * 初始化socket
     */
    private void initRtcSocket(){
        if(_handler == null) {
            _handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == RtcClient.RTC_MESSAGE_TYPE_JOIN_COMPLETE) {
                        Toast.makeText(RemoteVideoListActivity.this, "连接服务器成功", Toast.LENGTH_SHORT).show();
                    } else if (msg.what == RtcClient.RTC_MESSAGE_TYPE_CALL) {
                        JSONObject streamJson = (JSONObject) msg.obj;
                        //启动呼叫列表
                        try {
                            appendCall(streamJson.getString("personId"),streamJson.getString("name"),streamJson.getString("streamId"),null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
        //TODO 设置工程师名字
        RtcClient.getInstance().connect(_handler,"彪彪","12345678",RtcClient.RTC_CLIENT_TYPE_ENGINEER);
    }

    private void appendCall(String personId,String name,String streamId,String dateString){
        RemoteVideo item = new RemoteVideo();
        item.setId(personId);
        item.setPersonId(personId);
        item.setName(name);
        if(dateString == null){
            item.setTime((String) DateFormat.format("yyyy-MM-dd HH:mm:ss",new Date()));
        }else{
            item.setTime(dateString);
        }
        item.setType(RemoteVideo.TYPE_RTC);
        item.setRemoteSocketId(streamId);
        adapter.add(0,item);
        startAlarm();
    }

}

