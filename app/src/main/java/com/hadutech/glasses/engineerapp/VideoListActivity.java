package com.hadutech.glasses.engineerapp;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoListActivity extends AppCompatActivity {

    private static final String TAG="VideoListActivity";
    private static final int MSG_TYPE_VIDEO_LIST=1;

    /**
     * 用户名
     */
    private String user;
    private String tellphone;
    private String email;
    private String phone;
    private String ID;
    private String engineerName;
    private String msgName;
    private ListView list_content;
    private VideoAdapter adapter;
    private List<RemoteVideo> videoList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        /**
         * 1.UI初始化
         */
        list_content=(ListView)findViewById(R.id.list_content);
        adapter=new VideoAdapter(VideoListActivity.this,R.layout.list_item,videoList);

        //在视图中设立的标题
        new TitleBuilder(this).setTitleText("远程视频链接").setIv_right(R.drawable.ic_me);
        Log.e(TAG,"onCreate execute");

        /**
         * 2.获取员工信息
         */
        getMessage();

        /**
         * 3.调用2.9接口，获取留言问题记录
         */
        getGuidanceIssue();
        return;

        //4.把列表展示到ListView中（放到接口的回调方法里）


    }

    //获取员工信息
    private void getMessage(){
        SharedPreferences preferences=getSharedPreferences(ConfigData.SHARE_PREFERENCES_PREFIX,MODE_PRIVATE);
        String user=preferences.getString("user","");
        String tellphone=preferences.getString("tellphone","");
        String email=preferences.getString("email","");
        String phone=preferences.getString("phone","");
        String ID=preferences.getString("ID","");
        String engineerName=preferences.getString("engineerName","");
        String name=preferences.getString("name","");
        Boolean status=preferences.getBoolean("status",false);
    }


    //调用问题留言的接口
    private void getGuidanceIssue(){
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/list/time?start_time=2018-03-01 00:00:00&end_time=2018-06-07 23:59:59", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String issueMsg=response.body().string();
                try {
                    JSONObject issueObj=new JSONObject(issueMsg);
                    Log.d(TAG, "======="+issueObj);
                    Boolean msgStatus=issueObj.optBoolean("status");
                    JSONArray jsonArray=issueObj.getJSONArray("result");
                    List<RemoteVideo> videoList=new ArrayList<>();
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject result=jsonArray.getJSONObject(i);
                            String name=result.optString("name");
                            Log.d(TAG, "============="+name+"===============");
                            Boolean status=result.optBoolean("status");
                            String code=result.optString("code");
                            String time=result.optString("time");
                            RemoteVideo remoteVideo=new RemoteVideo();
                            remoteVideo.setTime(time);
                            remoteVideo.setName(name);
                            remoteVideo.setStatus(status);
                            remoteVideo.setId(code);
                            videoList.add(remoteVideo);
                        }
                        //利用Handler机制把信息回传给UI主线程
                        Message msg=new Message();
                        msg.what=MSG_TYPE_VIDEO_LIST;
                        msg.obj=videoList;
                        handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    final Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_TYPE_VIDEO_LIST:
                    List<RemoteVideo>list=(List<RemoteVideo>)msg.obj;
                    //4.把列表展示到ListView
//                    VideoAdapter adapter=new VideoAdapter();
//                    list_content.setAdapter(adapter);
                case 2:
                    update();
                    break;
            }
            super.handleMessage(msg);
        }
        void update(){

        }
    };

    //用于时间的刷新
    Timer timer=new Timer();
    TimerTask task=new TimerTask() {
        @Override
        public void run() {
            Message message=new Message();
            message.what=2;
            handler.sendMessage(message);
        }
    };
}
