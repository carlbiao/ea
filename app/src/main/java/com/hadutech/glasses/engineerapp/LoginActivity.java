package com.hadutech.glasses.engineerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends Activity implements View.OnClickListener{

    private static final String TAG="LoginActivity";
    //声明变量
    private EditText nameInputText;
    private EditText passwordInputText;
    private Button loginButton;
    private Button forgetPwdButton = null;

    //定义调用接口返回的信息
    String codeMsg = null;
    int engCode=0;
    String engMsg=null;
    //定义用户名，密码
    private String name,password;

    private ListView testLv;//ListView组件
    private Button updateDataBtn;//动态加载数据组件

    private List<String> dataList = new ArrayList<>();//存储数据


    final Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 2:
                    Toast.makeText(LoginActivity.this,"网络链接异常",Toast.LENGTH_SHORT).show();
                case 3:
                    Toast.makeText(LoginActivity.this,codeMsg,Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "codeMsg:"+codeMsg);



            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setViews();
        bindEvents();



    }

    private void bindEvents(){
        loginButton.setOnClickListener(this);
        forgetPwdButton.setOnClickListener(this);

    }


    private void setViews() {
        nameInputText=(EditText)findViewById(R.id.name);
        passwordInputText=(EditText)findViewById(R.id.password);
        loginButton=(Button)findViewById(R.id.btn_login);
        forgetPwdButton = findViewById(R.id.btn_forget_password);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_forget_password:
                break;
        }
    }

    private void login(){
        //获取工号和密码
        name=nameInputText.getText().toString().trim();
        password=passwordInputText.getText().toString().trim();

        //如果工号、密码为空则提示输入，如果错误则显示相应的错误
        if (TextUtils.isEmpty(name)){
            Toast.makeText(LoginActivity.this,"请输入工号",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String,Object> postParams=new HashMap<String, Object>();
        postParams.put("n",name);
        postParams.put("p",password);

        //调用登录接口
        HttpUtil.doPost(ConfigData.REST_SERVICE_BASE_URL+"/login",postParams, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg=new Message();
                msg.what=2;
                handler.sendMessage(msg);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //将返回的json字符串赋值给resultBody
                String resultBody=response.body().string();
                Log.d(TAG, "resultBody: "+resultBody);
                try {
                    //使用JSONObject解析登录之后得到的json字符串
                    JSONObject obj=new JSONObject(resultBody);
                    Log.d(TAG, ""+obj);
                    JSONObject res=obj.getJSONObject("result");
                    Boolean status=res.getBoolean("status");
                    Log.d(TAG, "status:"+status);
                    if (status){
                        //调用获取员工信息接口
                        getMessage();
                    }else {
                        codeMsg=res.getString("msg");
                        Message msg=new Message();
                        msg.what=3;
                        handler.sendMessage(msg);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //调用获取员工信息的接口
    private void getMessage(){
        HttpUtil.doGet(ConfigData.REST_SERVICE_BASE_URL + "/manage/user/current", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg=new Message();
                msg.what=2;
                handler.sendMessage(msg);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //将员工信息的json赋值给engMessign
                String engMessige=response.body().string();
                try {
                    //使用JSONObject解析员工信息
                    JSONObject msgObj=new JSONObject(engMessige);
                    engCode=msgObj.optInt("code");
                    engMsg=msgObj.optString("msg");
                    JSONObject resMsg=msgObj.optJSONObject("result");
                    Log.d(TAG, "resMsg:"+resMsg);
                    String user=resMsg.optString("a");
                    String tellphone=resMsg.optString("te");
                    String email=resMsg.optString("e");
                    String phone=resMsg.optString("ph");
                    String ID=resMsg.optString("i");
                    String engineerName=resMsg.optString("rn");
                    String name=resMsg.optString("n");
                    Boolean status=resMsg.optBoolean("status");
                    //判断是否获取员工信息，如果获取则将信息保存
                    if (status!=false) {
                        //获取登录时的系统时间
                        long time=System.currentTimeMillis()/1000;
                        int loginTime=new Long(time).intValue();
                        Log.d(TAG, "onResponse:"+loginTime);
                        //使用SharedPreference将员工信息保存起来
                        SharedPreferences.Editor editor = getSharedPreferences(ConfigData.SHARE_PREFERENCES_PREFIX, MODE_PRIVATE).edit();
                        editor.putString("user", user);
                        editor.putString("tellphone", tellphone);
                        editor.putString("email", email);
                        editor.putString("phone", phone);
                        editor.putString("ID", ID);
                        editor.putString("engineerName", engineerName);
                        editor.putString("name", name);
                        editor.putBoolean("status", status);
                        //将获取的时间保存起来
                        editor.putInt("time",loginTime);
                        editor.apply();
                    }
                    Intent intent=new Intent(LoginActivity.this,IssueCodeActivity.class);
                    startActivity(intent);
//                    Intent intent=new Intent(LoginActivity.this,VideoRecyclerActivity.class);
//                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }



    public void forgetPassword(){
             //TODO 实现具体逻辑
    }
}
