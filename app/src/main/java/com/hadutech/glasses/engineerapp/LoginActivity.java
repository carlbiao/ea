package com.hadutech.glasses.engineerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hadutech.glasses.engineerapp.events.RtcEvent;

import org.greenrobot.eventbus.EventBus;
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
    private View cleanView = null;



    //定义调用接口返回的信息
    String codeMsg = null;
    int engCode=0;
    String engMsg=null;
    //定义用户名，密码
    private String name = "";
    private String password = "";

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
//                    Toast.makeText(LoginActivity.this,codeMsg,Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "codeMsg:"+codeMsg);
//                    ToastUtil toastUtil = new ToastUtil();
//                    toastUtil.Short(LoginActivity.this,codeMsg).show().setToastBackground(Color.WHITE,R.drawable.toast_radius).show();

                    //自定义了Toast
                    View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.toast_view,null);
                    new ToastUtil(LoginActivity.this,view,Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(loginDerect()){
            login();
            return;
        }
        setContentView(R.layout.login_activity);
        setViews();
        bindEvents();
    }

    private void bindEvents(){
        loginButton.setOnClickListener(this);
        forgetPwdButton.setOnClickListener(this);
        cleanView.setOnClickListener(this);
    }


    private void setViews() {
        nameInputText=(EditText)findViewById(R.id.name);
        passwordInputText=(EditText)findViewById(R.id.password);
        loginButton=(Button)findViewById(R.id.btn_login);
        forgetPwdButton = findViewById(R.id.btn_forget_password);
        cleanView = findViewById(R.id.v_login_clear);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                name = "";
                password = "";
                login();
                break;
            case R.id.btn_forget_password:
                break;
            case R.id.v_login_clear:
                name = "";
                password = "";
                nameInputText.setText("");
                passwordInputText.setText("");
                break;
        }
    }

    private void login(){
        if(name == "" || password == ""){
            //获取工号和密码
            name=nameInputText.getText().toString().trim();
            password=passwordInputText.getText().toString().trim();
        }


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
                        name = "";
                        password = "";
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
                        editor.putString("password",password);
                        editor.apply();
                    }
                    RtcEvent event = new RtcEvent(RtcEvent.EVENT_TYPE_WILL_CONNECT_SOCKET);
                    event.setName(engineerName);
                    event.setPersonId(name);
                    EventBus.getDefault().post(event);
                    Intent intent=new Intent(LoginActivity.this,VideoRecyclerActivity.class);
                    startActivity(intent);
                    finish();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 是否直接登录
     * @return
     */
    private boolean loginDerect() {
        SharedPreferences preferences = getSharedPreferences(ConfigData.SHARE_PREFERENCES_PREFIX, MODE_PRIVATE);
        name = preferences.getString("name", "");
        password = preferences.getString("password", "");


        //判断信息是否存在，如果不存在，则返回登录
        if (name == "" || password == "") {
            return false;
        }

        //登录时的系统时间
        int loginTime = preferences.getInt("time", 1);
        long time = System.currentTimeMillis() / 1000;
        int nowTime = new Long(time).intValue();
        //用现在的系统时间减去登录时的系统时间，如果超过3600秒，则返回登录界面，否则跳转至列表活动
        return (nowTime - loginTime) < ConfigData.LOGIN_TIME;

    }



    public void forgetPassword(){
             //TODO 实现具体逻辑
    }
}
