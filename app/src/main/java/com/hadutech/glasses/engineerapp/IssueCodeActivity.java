package com.hadutech.glasses.engineerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dyhdyh.widget.loading.bar.LoadingBar;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IssueCodeActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "IssueCodeActivity";
    private MediaPlayer mediaPlayer = null;

    //设置一个用户id，用于获取用户信息接口
    private String user_id;
    private String project_name;
    private String veh_no;
    private String part_no;
    private String station_name;
    private String problems;
    private String name;
    private String duty_name;
    private String org_name;
    private String voice;
    private Button playButton;
    private Button stopButton;
    private String detailType = "";
    private LinearLayout parent = null;
    private Boolean showLoadingDialog = false;

    private String code = "";
    private Boolean status;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    //接收接口传来的数据
                    project_name = msg.getData().getString("project_name");
                    Log.d(TAG, "handleMessage:"+project_name);
                    veh_no = msg.getData().getString("veh_no");
                    part_no = msg.getData().getString("part_no");
                    station_name = msg.getData().getString("station_name");
                    user_id = msg.getData().getString("user_id");
                    problems = msg.getData().getString("problems");
                    voice = msg.getData().getString("voice");
                    //将解析出的数据赋值给控件
                    TextView textProject = (TextView)findViewById(R.id.tv_project);
                    textProject.setText(project_name);
                    TextView vehNum = (TextView)findViewById(R.id.tv_veh_num);
                    vehNum.setText(veh_no);
                    TextView partNum = (TextView)findViewById(R.id.tv_part_num);
                    partNum.setText(part_no);
                    TextView stationName = (TextView)findViewById(R.id.tv_station_num);
                    stationName.setText(station_name);
                    TextView userId = (TextView)findViewById(R.id.tv_user_id);
                    userId.setText(user_id);
                    TextView problem = (TextView)findViewById(R.id.tv_problems);
                    problem.setText(problems);
                    Button playMusicButton = findViewById(R.id.btn_play_music);
                    if(voice.equals("")){
                        playMusicButton.setBackgroundResource(R.drawable.btn_play_music_shape_disable);
                        playMusicButton.setEnabled(false);
                    }else{
                        playMusicButton.setBackgroundResource(R.drawable.btn_play_music_shape);
                        playMusicButton.setEnabled(true);
                    }

                    break;
                case 2:
                    name = msg.getData().getString("name");
                    duty_name = msg.getData().getString("duty_name");
                    org_name = msg.getData().getString("org_name");
                    //将解析出的数据赋值给控件
                    TextView userName = (TextView)findViewById(R.id.tv_user_name);
                    userName.setText(name);
                    TextView dutyName = (TextView)findViewById(R.id.tv_duty_name);
                    dutyName.setText(duty_name);
                    TextView orgName = (TextView)findViewById(R.id.tv_org_name);
                    orgName.setText(org_name);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_code);
        Intent intent = getIntent();
        code = intent.getStringExtra("code");
        boolean readStatus = intent.getBooleanExtra("readStatus",false);
        detailType = intent.getStringExtra("detailType");
        parent = findViewById(R.id.ll_issue_parent);
        if(detailType.equals("issue") == false){
            findViewById(R.id.rl_issue_voice).setVisibility(View.GONE);
            findViewById(R.id.rl_issue_voice_split_line).setVisibility(View.GONE);

        }
        playButton = (Button)findViewById(R.id.btn_play_music);
        playButton.setOnClickListener(this);

        stopButton = (Button)findViewById(R.id.btn_stop_music);
        stopButton.setOnClickListener(this);
        if(readStatus == false){
            //调用2.4接口，更新工程师读取留言问题记录状态
            HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/status/update?code="+code+"&status=true", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String statusMsg = response.body().string();
                    try {
                        //解析json
                        JSONObject statusObj = new JSONObject(statusMsg);
                        JSONObject resMsg=statusObj.optJSONObject("result");
                        status=resMsg.optBoolean("status");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

        }
        //设立标题
        new LogoActivity(this).setIb_left(R.drawable.ic_back).setIb_left(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }).setLogoText("远程视频列表");
        //获取语音留言问题的方法
        getIssueCode();
    }

    //调用接口2.10获取语音留言问题详细信息
    private void getIssueCode(){
        if(detailType.equals("issue")){
            HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/code/get?code=" + code, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String issueMsg = response.body().string();
                    Log.d(TAG, "onResponse:"+issueMsg);
                    try {
                        //解析json
                        JSONObject issueObj = new JSONObject(issueMsg);
                        JSONObject resMsg = issueObj.optJSONObject("result");
                        String veh_no = resMsg.optString("veh_no");
                        String part_no = resMsg.optString("part_no");
                        String voice = resMsg.optString("voice");
                        String problems = resMsg.optString("problems");
                        user_id = resMsg.optString("user_id");
                        String project_name = resMsg.optString("project_name");
                        String station_name = resMsg.optString("station_name");
                        //利用handler将数据传出去
                        Message msg = new Message();
                        msg.what=1;
                        Bundle bundle = new Bundle();
                        bundle.putString("project_name",project_name);
                        bundle.putString("veh_no",veh_no);
                        bundle.putString("part_no",part_no);
                        bundle.putString("station_name",station_name);
                        bundle.putString("user_id",user_id);
                        bundle.putString("problems",problems);
                        bundle.putString("voice",voice);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        getUserId();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }else{
            HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/im/code/get?code=" + code, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String issueMsg = response.body().string();
                    Log.d(TAG, "onResponse:"+issueMsg);
                    try {
                        //解析json
                        JSONObject issueObj = new JSONObject(issueMsg);
                        JSONObject resMsg = issueObj.optJSONObject("result");
                        resMsg = resMsg.optJSONObject("data");
                        String veh_no = resMsg.optString("vehNo");
                        String part_no = resMsg.optString("partNo");
                        user_id = resMsg.optString("fromUserId");
                        String project_name = resMsg.optString("projectName");
                        String station_name = resMsg.optString("stationName");
                        //利用handler将数据传出去
                        Message msg = new Message();
                        msg.what=1;
                        Bundle bundle = new Bundle();
                        bundle.putString("project_name",project_name);
                        bundle.putString("veh_no",veh_no);
                        bundle.putString("part_no",part_no);
                        bundle.putString("station_name",station_name);
                        bundle.putString("fromUserId",user_id);
                        bundle.putString("problems",problems);
                        bundle.putString("voice",voice);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        getUserId();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }


    }

    //调用接口1.5获取员工基本信息
    private void getUserId(){
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/user/userid?user_id="+user_id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String userMsg = response.body().string();
                try {
                    //解析json
                    JSONObject userObj = new JSONObject(userMsg);
                    JSONObject resMsg = userObj.optJSONObject("result");
                    String name = resMsg.optString("name");
                    String duty_name = resMsg.optString("duty_name");
                    String org_name = resMsg.optString("org_name");
                    //利用handler将数据传出去
                    Message msg = new Message();
                    msg.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("name",name);
                    bundle.putString("duty_name",duty_name);
                    bundle.putString("org_name",org_name);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return;
            }
        });
    }

    //播放音频的方法
    private void initMediaPlay(){
        try {
            Long start = System.currentTimeMillis();
            mediaPlayer.setDataSource(this,Uri.parse(voice));//指定音频文件的路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
            Long end = System.currentTimeMillis();
            Log.e(TAG,"播放：" + (end-start) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlay();
                }else {
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();
            hideLoading();
            playButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer mp) {
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
            //mp.seekTo(0);
        }
    };

    private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener(){
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            playButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            mp.start();
        }
    };

    //播放音频的点击事件
    @Override
    public void onClick(View v){
        if(showLoadingDialog){
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_play_music) {
            if (ContextCompat.checkSelfPermission(IssueCodeActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(IssueCodeActivity.this,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
                }else {
                if(mediaPlayer == null){
                    showLoading(ExLoadingFactory.TYPE_GET_VOICE);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(onPreparedListener);
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                    mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
                    try {
                        mediaPlayer.setDataSource(this,Uri.parse(voice));//指定音频文件的路径
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.prepareAsync();
                }else{
                    playButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);
//                    mediaPlayer.
                    mediaPlayer.seekTo(0);
                    //mediaPlayer.start();
                }
            }
        }else if (id == R.id.btn_stop_music) {
            mediaPlayer.pause();
            //mediaPlayer.seekTo(0);
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void hideLoading() {
        showLoadingDialog = false;
        LoadingBar.cancel(parent);
    }

    private void showLoading(int type) {
        showLoadingDialog = true;
        ExLoadingFactory factory = new ExLoadingFactory(type);
        LoadingBar.make(parent, factory).show();
    }






}
