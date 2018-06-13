package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IssueCodeActivity extends AppCompatActivity {

    private static final String TAG="IssueCodeActivity";

    private Button play_music;
    //设置一个用户id，用于获取用户信息接口
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_code);

        //设立标题
        new LogoActivity(this).setIb_left(R.drawable.ic_back).setLogoText("远程视频列表");

        //获取语音留言问题的方法
        getIssueCode();

        //之前设置的跳转到音频文件的点击事件
        Button play_music=(Button)findViewById(R.id.play_music);
        play_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(IssueCodeActivity.this,PlayAudioTest.class);
                startActivity(intent);
            }
        });
    }

    //调用接口2.10获取语音留言问题详细信息
    private void getIssueCode(){
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/code/get?code=1002395451112030208", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String issueMsg=response.body().string();
                Log.d(TAG, "onResponse:"+issueMsg);
                try {
                    //解析json
                    JSONObject issueObj=new JSONObject(issueMsg);
                    JSONObject resMsg=issueObj.optJSONObject("result");
                    String code=resMsg.optString("code");
                    String project_no=resMsg.optString("project_no");
                    String veh_no=resMsg.optString("veh_no");
                    String part_no=resMsg.optString("part_no");
                    String station_no=resMsg.optString("station_no");
                    String voice=resMsg.optString("voice");
                    String problems=resMsg.optString("problems");
                    Boolean read_status=resMsg.optBoolean("read_status");
                    user_id=resMsg.optString("user_id");
                    String from_user_id=resMsg.optString("from_user_id");
                    String project_name=resMsg.optString("project_name");
                    String station_name=resMsg.optString("station_name");
                    //将解析出的数据赋值给控件
                    TextView textProject=(TextView)findViewById(R.id.tv_project);
                    textProject.setText(project_name);
                    TextView vehNum=(TextView)findViewById(R.id.tv_veh_num);
                    vehNum.setText(veh_no);
                    TextView partNum=(TextView)findViewById(R.id.tv_part_num);
                    partNum.setText(part_no);
                    TextView stationName=(TextView)findViewById(R.id.tv_station_num);
                    stationName.setText(station_name);
                    TextView userId=(TextView)findViewById(R.id.tv_user_id);
                    userId.setText(user_id);
                    TextView problem=(TextView)findViewById(R.id.tv_problems);
                    problem.setText(problems);

                    //获取员工基本信息的方法
                    getUserId();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    //调用接口1.5获取员工基本信息
    private void getUserId(){
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/user/userid?user_id="+user_id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String userMsg=response.body().string();
                try {
                    //解析json
                    JSONObject userObj=new JSONObject(userMsg);
                    JSONObject resMsg=userObj.optJSONObject("result");
                    String name=resMsg.optString("name");
                    String duty_name=resMsg.optString("duty_name");
                    String org_name=resMsg.optString("org_name");
                    //将解析出的数据赋值给控件
                    TextView userName=(TextView)findViewById(R.id.tv_user_name);
                    userName.setText(name);
                    TextView dutyName=(TextView)findViewById(R.id.tv_duty_name);
                    dutyName.setText(duty_name);
                    TextView orgName=(TextView)findViewById(R.id.tv_org_name);
                    orgName.setText(org_name);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }




















    private View.OnClickListener leftReturnListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(IssueCodeActivity.this,VideoListActivity.class);
            startActivity(intent);
        }
    };
}
