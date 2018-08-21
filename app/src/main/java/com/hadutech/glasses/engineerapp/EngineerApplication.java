package com.hadutech.glasses.engineerapp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.hadutech.glasses.engineerapp.events.AppEvent;
import com.hadutech.glasses.engineerapp.events.RtcEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EngineerApplication extends Application {
    private static final String TAG = "EngineerApplication";

    /**
     * 当前登录用户JSON格式信息
     */
    private JSONObject userJson;

    public JSONObject getUserJson() {
        return userJson;
    }

    public void setUserJson(JSONObject userJson) {
        this.userJson = userJson;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userEventBus(final RtcEvent event) {
        switch (event.getType()) {
            case RtcEvent.EVENT_TYPE_WILL_CONNECT_SOCKET:
                Handler socketHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == RtcClient.RTC_MESSAGE_TYPE_JOIN_COMPLETE) {
                            Log.i(TAG, "Join the single server complete, name = " + event.getName() + ", personId = " + event.getPersonId());
                        }
                    }
                };
                RtcClient.getInstance().setRtcHandler(socketHandler);
                RtcClient.getInstance().connect(event.getName(), event.getPersonId(), RtcClient.RTC_CLIENT_TYPE_ENGINEER);
                break;
            case RtcEvent.EVENT_TYPE_ON_CALL:
                //启动接听列表Activity
                Intent intent = new Intent();
                intent.setAction("open_video_action");
                intent.addCategory("open_video_category");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("name", event.getName());
                intent.putExtra("personId", event.getPersonId());
                intent.putExtra("remoteSocketId", event.getRemoteSocketId());
                intent.putExtra("code", event.getId());
                startActivity(intent);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userEventBus(AppEvent event) {
        switch (event.getType()) {
            case AppEvent.EVENT_TYPE_LOGOUT:
                try {
                    SharedPreferences.Editor editor = getSharedPreferences(ConfigData.SHARE_PREFERENCES_PREFIX, MODE_PRIVATE).edit();
                    editor.clear();
                    editor.commit();

                    //断开rtc
                    RtcClient.getInstance().close();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }

                Intent intent = new Intent(EngineerApplication.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }
}
