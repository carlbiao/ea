package com.hadutech.glasses.engineerapp.events;

import android.app.Activity;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

public class AppEvent {
    /**
     * 注销
     */
    public static final int EVENT_TYPE_LOGOUT = 1;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type = 0;


    public AppEvent(int type) {
        this.type = type;
    }

    /**
     * 页面登出
     */
    public static class Logout implements View.OnClickListener {

        private Activity activity;

        public Logout(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new AppEvent(AppEvent.EVENT_TYPE_LOGOUT));
            this.activity.finish();
        }
    }
}
