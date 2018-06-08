package com.hadutech.glasses.engineerapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.hadutech.glasses.engineerapp.events.RtcEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EngineerApplication extends Application {
    private static final String TAG = "EngineerApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        EventBus.getDefault().register(this);
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
}
