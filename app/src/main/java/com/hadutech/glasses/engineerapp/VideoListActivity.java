package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoListActivity extends AppCompatActivity{

    private static final String TAG="VideoListActivity";



    private static final int Type_Data=0;
    private static final int Type_Listen=1;
    private ListView list_content;
    private ListView add_list;
    private ArrayList<Object> mData=null;
    private AdapterLayout myAdapter=null;

    private Button btn_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        //在视图中设立的标题
        new TitleBuilder(this).setTitleText("远程视频链接").setIv_right(R.drawable.ic_me);
        Log.e(TAG,"onCreate execute");
        //准备数据
        mData=new ArrayList<Object>();
        for (int i=0;i<4;i++){
            switch ((int)(Math.random()*2)){
                case Type_Data:
                    mData.add(new WorkData("2018-5-20","张三:","123456"));
                    break;
                case Type_Listen:
                    mData.add(new ListenDate("2018-5-23","李四:","4356324"));
                    break;
            }
        }
        bindViews();

        ListView list_content=(ListView)findViewById(R.id.list_content);
        myAdapter=new AdapterLayout(VideoListActivity.this,mData);
        list_content.setAdapter(myAdapter);


        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(VideoListActivity.this,ProjectMessage.class);
                startActivity(intent);
            }
        });
    }
    private void bindViews(){
        list_content=(ListView)findViewById(R.id.list_content);
        add_list=(ListView)findViewById(R.id.add_list);

        list_content.setAdapter(myAdapter);
        add_list.setAdapter(myAdapter);
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
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/guidance/issue/list/time", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String issueMsg=response.body().string();
                try {
                    JSONObject issueObj=new JSONObject(issueMsg);
                    Boolean msgStatus=issueObj.optBoolean("status");
                    //判断是否获取员工信息，如果获取则将信息保存
                    if (msgStatus!=false){
                        JSONArray jsonArray=issueObj.getJSONArray("result");
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject result=jsonArray.getJSONObject(i);
                            String name=result.optString("name");
                            Boolean status=result.optBoolean("status");
                            String code=result.optString("code");
                            String time=result.optString("time");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }







}
