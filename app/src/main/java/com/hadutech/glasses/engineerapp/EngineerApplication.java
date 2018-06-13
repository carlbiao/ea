package com.hadutech.glasses.engineerapp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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
        Log.e(TAG, "onCreate");
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userEventBus(RtcEvent event) {
        switch (event.getType()) {
            case RtcEvent.EVENT_TYPE_WILL_CONNECT_SOCKET:
                Log.e(TAG,"connect to socket server...");
                Handler socketHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if(msg.what == RtcClient.RTC_MESSAGE_TYPE_JOIN_COMPLETE){
                            Log.e(TAG,"connect to socket server complete!");
                        }
                    }
                };
                RtcClient.getInstance().connect(socketHandler,event.getName(),event.getPersonId(),RtcClient.RTC_CLIENT_TYPE_ENGINEER);
                break;
            case RtcEvent.EVENT_TYPE_ON_CALL:
                //启动接听列表Activity
                Intent intent = new Intent();
                intent.setAction("my_action");
                intent.addCategory("my_category");
                startActivity(intent);
                break;
        }


    }



    //获取现在的系统时间
    private void booleanTime() {

    }
}
