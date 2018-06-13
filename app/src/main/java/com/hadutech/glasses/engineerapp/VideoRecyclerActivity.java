package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoRecyclerActivity extends AppCompatActivity implements VideoRecyclerAdapter.OnItemClickListener {

    private static final String TAG = "VideoRecyclerActivity";
    private static final int MSG_TYPE_VIDEO_LIST = 1;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private VideoRecyclerAdapter adapter;
    private List<RemoteVideo> list = null;


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
        new TitleBuilder(this).setTitleText("远程视频列表").setIv_right(R.drawable.ic_me);


        //2、2.9 获取留言问题记录
        getGuidanceIssue();

    }

    //调用问题留言的接口
    private void getGuidanceIssue() {
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/list/time?start_time=2018-03-01 00:00:00&end_time=2018-06-07 23:59:59", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String issueMsg = response.body().string();
                try {
                    JSONObject issueObj = new JSONObject(issueMsg);
                    Log.d(TAG, "=======" + issueObj);
                    Boolean msgStatus = issueObj.optBoolean("status");
                    JSONArray jsonArray = issueObj.getJSONArray("result");
                    Log.d(TAG, "+++++++" + jsonArray);
                    List<RemoteVideo> videoList = new ArrayList<>();
                    for (int i = jsonArray.length() - 1; i > 0; i--) {
                        JSONObject result = jsonArray.getJSONObject(i);
                        String name = result.optString("name");
                        boolean status = result.optBoolean("status");
                        String code = result.optString("code");
                        String time = result.optString("time");
                        RemoteVideo remoteVideo = new RemoteVideo();
                        remoteVideo.setTime(time);
                        remoteVideo.setName(name);
                        remoteVideo.setStatus(status);
                        remoteVideo.setPersonId(code);
                        remoteVideo.setId(code);
                        remoteVideo.setType(RemoteVideo.TYPE_VOICE);
                        videoList.add(remoteVideo);
                    }
                    //利用Handler机制把信息回传给UI主线程
                    Message msg = new Message();
                    msg.what = MSG_TYPE_VIDEO_LIST;
                    msg.obj = videoList;
                    handler.sendMessage(msg);
                    // }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TYPE_VIDEO_LIST:
                    List<RemoteVideo> list = (List<RemoteVideo>) msg.obj;
                    for (RemoteVideo remoteVideo : list) {
                        adapter.addItem(remoteVideo);
                    }
                    break;
                case 2:
                    //update();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public void onViewItemClick(RemoteVideo item) {
        Toast.makeText(VideoRecyclerActivity.this, "查看", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(VideoRecyclerActivity.this,IssueCodeActivity.class);
        intent.putExtra("code",item.getId());
        startActivity(intent);
    }

    @Override
    public void onAnswerClick(RemoteVideo item) {

    }

    @Override
    public void onHangupClick(RemoteVideo item) {

    }

    //用于时间的刷新
//    Timer timer=new Timer();
//    TimerTask task=new TimerTask() {
//        @Override
//        public void run() {
//            Message message=new Message();
//            message.what=2;
//            handler.sendMessage(message);
//        }
//    };
}
