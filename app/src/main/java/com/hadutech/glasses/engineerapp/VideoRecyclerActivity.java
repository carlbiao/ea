package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.hadutech.glasses.engineerapp.events.AppEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoRecyclerActivity extends AppCompatActivity implements VideoRecyclerAdapter.OnItemClickListener {

    private static final String TAG = VideoRecyclerActivity.class.getName();
    private static final int MSG_TYPE_VIDEO_LIST = 1;
    private static final int MSG_TYPE_ANSWER_TIMEOUT = 2;
    private static final int MSG_TYPE_REQUEST_VIDEO_LIST = 3;

    private static final int REQUEST_CODE_ASK_WRITE_SETTINGS = 10;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private VideoRecyclerAdapter adapter;
    private List<RemoteVideo> list = null;
    private MediaPlayer mediaPlayer = null;
    private Timer answernTimeout = null;
    private AudioManager audiomanage = null;
    private Timer timer = null;
    TimerTask timerTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recycler);
        //1、UI初始化
        recyclerView = (RecyclerView) findViewById(R.id.rv_video_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        list = new LinkedList<>();
        adapter = new VideoRecyclerAdapter(list);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        //在视图中设立的标题
        new TitleBuilder(this).setTitleText("远程视频列表").setIv_right(R.mipmap.icon_end).setRightIcoListening(new AppEvent.Logout(this));

        //2、2.9 获取留言问题记录
        getGuidanceIssue();
        startRequestLoop();

/*        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
            } else {
                Log.i(TAG, "我有权限！！！！");
            }
        }*/
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            Log.i(TAG, "OK");
        }
    }*/

    private void startRequestLoop() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MSG_TYPE_REQUEST_VIDEO_LIST;
                handler.sendMessage(message);
            }
        };
        timer.schedule(timerTask, 3000, 60 * 1000);
    }

    private void stopRequestLoop() {
        Log.w(TAG, "Stop GuidanceIssue request loop task!!!");
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }

        timer = null;
        timerTask = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String name = intent.getStringExtra("name");
        String personId = intent.getStringExtra("personId");
        final String remoteSocketId = intent.getStringExtra("remoteSocketId");
        String code = intent.getStringExtra("code");


        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(personId) && !TextUtils.isEmpty(remoteSocketId)) {
            appendCall(personId, name, remoteSocketId, code, null);
            //超过指定时间未应答则主动挂断
            answernTimeout = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = MSG_TYPE_ANSWER_TIMEOUT;
                    msg.obj = remoteSocketId;
                    handler.sendMessage(msg);
                }
            };
            answernTimeout.schedule(task, ConfigData.ANSWER_TIMEOUT);
        }
    }

    //调用问题留言的接口
    private void getGuidanceIssue() {
        String endDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd") + " 00:00:00";
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/list/time?start_time=2018-03-01 00:00:00&end_time=" + endDateStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getGuidanceIssue failed!!!", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String issueMsg = response.body().string();
                Log.i(this.getClass().getName(), "Http request to get issue list complete, result = " + issueMsg);
                try {
                    if (StringUtils.isNotEmpty(issueMsg)) {
                        JSONObject issueObj = new JSONObject(issueMsg);
                        if (issueObj.getBoolean("ret")) {
                            JSONArray jsonArray = issueObj.getJSONArray("result");
                            List<RemoteVideo> videoList = new ArrayList<>();
                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                JSONObject result = jsonArray.getJSONObject(i);
                                String name = result.optString("name");
                                boolean status = result.optBoolean("status");
                                String code = result.optString("code");
                                String time = result.optString("time");
                                String empCode = result.optString("empCode");
                                RemoteVideo remoteVideo = new RemoteVideo();
                                remoteVideo.setTime(time);
                                remoteVideo.setName(name);
                                remoteVideo.setStatus(status);
                                remoteVideo.setPersonId(empCode);
                                remoteVideo.setId(code);
                                remoteVideo.setEmpCode(empCode);
                                remoteVideo.setType(RemoteVideo.TYPE_VOICE);
                                videoList.add(remoteVideo);
                            }
                            //利用Handler机制把信息回传给UI主线程
                            Message msg = new Message();
                            msg.what = MSG_TYPE_VIDEO_LIST;
                            msg.obj = videoList;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TYPE_VIDEO_LIST:
                    List<RemoteVideo> tmplist = (List<RemoteVideo>) msg.obj;
                    for (RemoteVideo remoteVideo : tmplist) {
//                        if(list.contains(remoteVideo)){
//                            continue;
//                        }
                        adapter.addItem(remoteVideo);
                    }

                    break;
                case MSG_TYPE_ANSWER_TIMEOUT:
                    stopAlarm();
                    RtcClient.getInstance().refuse(String.valueOf(msg.obj));
                    adapter.removeItemBySocketId(String.valueOf(msg.obj));
                    //update();
                    break;
                case MSG_TYPE_REQUEST_VIDEO_LIST:
                    getGuidanceIssue();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public void onViewItemClick(RemoteVideo item) {
        Intent intent = new Intent(VideoRecyclerActivity.this, IssueCodeActivity.class);
        intent.putExtra("code", item.getId());
        intent.putExtra("readStatus", item.isStatus());
        intent.putExtra("detailType", "issue");

        startActivity(intent);
        int index = list.indexOf(item);
        item.setStatus(true);
        adapter.updateItem(index, item);
    }

    @Override
    public void onAnswerClick(RemoteVideo item) {
        stopAlarm();
        answernTimeout.cancel();
        answernTimeout = null;
        //打开视频窗口
        Intent intent = new Intent(VideoRecyclerActivity.this, RTCActivity.class);
        Bundle bundle = item.toBundle();
        intent.putExtras(bundle);
        startActivity(intent);
        adapter.removeItemBySocketId(item.getRemoteSocketId());
    }

    @Override
    public void onHangupClick(RemoteVideo item) {
        stopAlarm();
        RtcClient.getInstance().refuse(item.getRemoteSocketId());

        adapter.removeItemBySocketId(item.getRemoteSocketId());
    }

    //点击标题栏图片返回到登录界面
/*    private View.OnClickListener rightReturnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new AppEvent(AppEvent.EVENT_TYPE_LOGOUT));
            finish();
        }
    };*/

    private void appendCall(String personId, String name, String streamId, String code, String dateString) {
        RemoteVideo item = new RemoteVideo();
        //item.setId(personId);
        item.setPersonId(personId);
        item.setName(name);
        item.setId(code);
        if (dateString == null) {
            item.setTime((String) DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()));
        } else {
            item.setTime(dateString);
        }
        item.setType(RemoteVideo.TYPE_RTC);
        item.setRemoteSocketId(streamId);
        adapter.addItem(0, item);
        recyclerView.scrollToPosition(0);
        startAlarm();
    }

    /**
     * 响铃
     */
    private void startAlarm() {
        if (audiomanage == null) {
            audiomanage = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        audiomanage.setSpeakerphoneOn(true);
        //audiomanage.setSpeakerphoneOn(false);

        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());
            mediaPlayer = MediaPlayer.create(this, R.raw.ring);
            mediaPlayer.setLooping(true);
        }

        mediaPlayer.start();
    }

    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = null;
    }

    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRequestLoop();
    }

}
