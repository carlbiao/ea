package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IssueCodeActivity extends AppCompatActivity {

    private static final String TAG="IssueCodeActivity";

    private Button play_music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_code);

        //设立标题
        new LogoActivity(this).setIb_left(R.drawable.ic_back).setLogoText("远程视频列表");

        getIssueCode();

        Button play_music=(Button)findViewById(R.id.play_music);
        play_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(IssueCodeActivity.this,PlayAudioTest.class);
                startActivity(intent);
            }
        });
    }

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
                    JSONObject issueObj=new JSONObject(issueMsg);
                    JSONObject resMsg=issueObj.optJSONObject("result");
                    String code=resMsg.optString("code");
                    String project_no=resMsg.optString("project_no");
                    String veh_no=resMsg.optString("veh_no");
                    String part_no=resMsg.optString("part_no");
                    String station_no=resMsg.optString("station_no");
                    String voice=resMsg.optString("voice");
                    String problems=resMsg.optString("problems");
                    Log.d(TAG, "onResponse:"+problems);
                    String read_status=resMsg.optString("read_status");
                    String user_id=resMsg.optString("user_id");
                    String from_user_id=resMsg.optString("from_user_id");
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
