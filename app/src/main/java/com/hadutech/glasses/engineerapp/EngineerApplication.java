package com.hadutech.glasses.engineerapp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.hadutech.glasses.engineerapp.events.RtcEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EngineerApplication extends Application {
    private static final String TAG = "EngineerApplication";

    //声明变量：用户登录时的系统时间
    private long loginTime;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        EventBus.getDefault().register(this);

        //读取出登录信息的方法
        readLoginMsg();



    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userEventBus(RtcEvent event){
        //启动接听列表Activity
        Intent intent = new Intent();
        intent.setAction("my_action");
        intent.addCategory("my_category");
        //intent.setClassName("当前Act的全限定类名","启动Act的全限定类名");
        startActivity(intent);
    }

    //读取出登录信息
    private void readLoginMsg(){
        SharedPreferences preferences=getSharedPreferences(ConfigData.SHARE_PREFERENCES_PREFIX,MODE_PRIVATE);
        //preferences.edit().clear().commit();
        Log.d(TAG, "readLoginMsg:"+preferences);
        String user=preferences.getString("user","");
        Log.d(TAG, "readLoginMsg:"+user);
        String tellphone=preferences.getString("tellphone","");
        String email=preferences.getString("email","");
        String phone=preferences.getString("phone","");
        String ID=preferences.getString("ID","");
        String engineerName=preferences.getString("engineerName","");
        String msgName=preferences.getString("name","");
        Boolean msgStatus=preferences.getBoolean("status",false);
        //登录时的系统时间
        loginTime=preferences.getInt("time",1);

        //判断信息是否存在，如果不存在，则返回登录
        if (user==""){
            Intent intent=new Intent(EngineerApplication.this,LoginActivity.class);
            startActivity(intent);
        }

        //判断登录时长
        booleanTime();

    }

    //获取现在的系统时间
    private void booleanTime(){
        long time=System.currentTimeMillis()/1000;
        int nowTime=new Long(time).intValue();
        //用现在的系统时间减去登录时的系统时间，如果超过3600秒，则返回登录界面，否则跳转至列表活动
        if (nowTime-loginTime>ConfigData.LOGIN_TIME){
            Intent intent=new Intent(EngineerApplication.this,LoginActivity.class);
            startActivity(intent);
        }else {
            Intent intent=new Intent(EngineerApplication.this,VideoRecyclerActivity.class);
            startActivity(intent);
        }
    }
}
