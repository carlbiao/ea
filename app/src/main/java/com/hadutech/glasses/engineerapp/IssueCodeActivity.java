package com.hadutech.glasses.engineerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dyhdyh.widget.loading.bar.LoadingBar;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IssueCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "IssueCodeActivity";
    private MediaPlayer mediaPlayer = null;

    //设置一个用户id，用于获取用户信息接口
    private String user_id;
    private Button playButton;
    private Button stopButton;
    private String detailType = "";
    private LinearLayout parent = null;
    private Boolean showLoadingDialog = false;

    private String code = "";
    private String voiceUrl = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //接收接口传来的数据
                    user_id = msg.getData().getString("user_id");
                    //将解析出的数据赋值给控件
                    TextView textProject = findViewById(R.id.tv_project);
                    textProject.setText(msg.getData().getString("project_name", ""));
                    TextView vehNum = findViewById(R.id.tv_veh_num);
                    vehNum.setText(msg.getData().getString("veh_no", ""));
                    TextView partNum = findViewById(R.id.tv_part_num);
                    partNum.setText(msg.getData().getString("part_no", ""));
                    TextView stationName = findViewById(R.id.tv_station_num);
                    stationName.setText(msg.getData().getString("station_name", ""));

                    break;
                case 2:
                    //接收接口传来的数据
                    user_id = msg.getData().getString("user_id");
                    String problems = msg.getData().getString("problems");
                    String voice = msg.getData().getString("voice");
                    //将解析出的数据赋值给控件
                    textProject = findViewById(R.id.tv_project);
                    textProject.setText(msg.getData().getString("project_name", ""));
                    vehNum = findViewById(R.id.tv_veh_num);
                    vehNum.setText(msg.getData().getString("veh_no", ""));
                    partNum = findViewById(R.id.tv_part_num);
                    partNum.setText(msg.getData().getString("part_no", ""));
                    stationName = findViewById(R.id.tv_station_num);
                    stationName.setText(msg.getData().getString("station_name", ""));
                    if (StringUtils.isNotEmpty(problems)) {
                        com.alibaba.fastjson.JSONObject proJson = com.alibaba.fastjson.JSONObject.parseObject(problems);
                        TextView problem = findViewById(R.id.tv_problems);
                        problem.setText(proJson.getString("menu") + "\n" + proJson.getString("detail"));
                    }

                    Button playMusicButton = findViewById(R.id.btn_play_music);
                    if (StringUtils.isEmpty(voice)) {
                        playMusicButton.setBackgroundResource(R.drawable.btn_play_music_shape_disable);
                        playMusicButton.setEnabled(false);
                    } else {
                        playMusicButton.setBackgroundResource(R.drawable.btn_play_music_shape);
                        playMusicButton.setEnabled(true);
                    }

                    break;
                case 3:
                    //将解析出的数据赋值给控件
                    TextView userName = findViewById(R.id.tv_user_name);
                    userName.setText(msg.getData().getString("name", ""));
                    TextView dutyName = findViewById(R.id.tv_duty_name);
                    dutyName.setText(msg.getData().getString("duty_name", ""));
                    TextView orgName = findViewById(R.id.tv_org_name);
                    orgName.setText(msg.getData().getString("org_name", ""));
                    ((TextView) findViewById(R.id.tv_user_id)).setText(msg.getData().getString("login_name"));
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
        boolean readStatus = intent.getBooleanExtra("readStatus", true);
        detailType = intent.getStringExtra("detailType");

        parent = findViewById(R.id.ll_issue_parent);

        playButton = findViewById(R.id.btn_play_music);
        playButton.setOnClickListener(this);

        stopButton = findViewById(R.id.btn_stop_music);
        stopButton.setOnClickListener(this);

        //设立标题
        LogoActivity logoActivity = new LogoActivity(this).setIb_left(R.mipmap.ic_back).setIb_left(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (StringUtils.equals("calling", detailType)) {
            logoActivity.setLogoText("远程视频列表");
            findViewById(R.id.rl_issue_voice).setVisibility(View.GONE);
            findViewById(R.id.rl_issue_voice_split_line).setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // 获取视频通话信息
            HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/im/code/get?code=" + code, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String issueMsg = response.body().string();
                    try {
                        //解析json
                        JSONObject issueObj = new JSONObject(issueMsg);
                        JSONObject resMsg = issueObj.optJSONObject("result");
                        resMsg = resMsg.optJSONObject("data");
                        String veh_no = resMsg.optString("vehNo");
                        String part_no = resMsg.optString("partNo");
                        user_id = resMsg.optString("fromUserId");
                        String from_login_name = resMsg.optString("fromLoginName");
                        String project_name = resMsg.optString("projectName");
                        String station_name = resMsg.optString("stationName");
                        //利用handler将数据传出去
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("project_name", project_name);
                        bundle.putString("veh_no", veh_no);
                        bundle.putString("part_no", part_no);
                        bundle.putString("station_name", station_name);
                        bundle.putString("fromUserId", user_id);
                        bundle.putString("from_login_name", from_login_name);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        getUserId();
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            });
        } else {
            logoActivity.setLogoText("留言详情");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // 如果是语音留言且未读状态则更新读取状态
            if (!readStatus) {
                //调用2.4接口，更新工程师读取留言问题记录状态
                HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/status/update?code=" + code + "&status=true", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String statusMsg = response.body().string();
                        Log.i(TAG, "Update guidance issue read status complete, result = {}" + statusMsg);
                    }
                });
            }

            // 获取语音留言详情信息
            HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/code/get?code=" + code, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String issueMsg = response.body().string();
                    try {
                        //解析json
                        JSONObject issueObj = new JSONObject(issueMsg);
                        JSONObject resMsg = issueObj.optJSONObject("result");
                        String veh_no = resMsg.optString("veh_no");
                        String part_no = resMsg.optString("part_no");
                        voiceUrl = resMsg.optString("voice");
                        String problems = resMsg.optString("problems");
                        user_id = resMsg.optString("user_id");
                        String project_name = resMsg.optString("project_name");
                        String station_name = resMsg.optString("station_name");
                        //利用handler将数据传出去
                        Message msg = new Message();
                        msg.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putString("project_name", project_name);
                        bundle.putString("veh_no", veh_no);
                        bundle.putString("part_no", part_no);
                        bundle.putString("station_name", station_name);
                        bundle.putString("user_id", user_id);
                        bundle.putString("problems", problems);
                        bundle.putString("voice", voiceUrl);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        getUserId();
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            });
        }
    }

    //调用接口1.5获取员工基本信息
    private void getUserId() {
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/user/userid?user_id=" + user_id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "", e);
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
                    String login_name = resMsg.optString("login_name");
                    //利用handler将数据传出去
                    Message msg = new Message();
                    msg.what = 3;
                    Bundle bundle = new Bundle();
                    bundle.putString("name", name);
                    bundle.putString("duty_name", duty_name);
                    bundle.putString("org_name", org_name);
                    bundle.putString("login_name", login_name);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
                return;
            }
        });
    }

    //播放音频的方法
    private void initMediaPlay() {
        try {
            mediaPlayer.setDataSource(this, Uri.parse(voiceUrl));//指定音频文件的路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMediaPlay();
            } else {
                Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                finish();
            }
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

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
            //mp.seekTo(0);
        }
    };

    private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            playButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            mp.start();
        }
    };

    //播放音频的点击事件
    @Override
    public void onClick(View v) {
        if (showLoadingDialog) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_play_music) {
            if (ContextCompat.checkSelfPermission(IssueCodeActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(IssueCodeActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                if (mediaPlayer == null) {
                    showLoading(ExLoadingFactory.TYPE_GET_VOICE);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(onPreparedListener);
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                    mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
                    try {
                        mediaPlayer.setDataSource(this, Uri.parse(voiceUrl));//指定音频文件的路径
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.prepareAsync();
                } else {
                    playButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);
//                    mediaPlayer.
                    mediaPlayer.seekTo(0);
                    //mediaPlayer.start();
                }
            }
        } else if (id == R.id.btn_stop_music) {
            mediaPlayer.pause();
            //mediaPlayer.seekTo(0);
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
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
