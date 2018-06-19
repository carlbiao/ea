package com.hadutech.glasses.engineerapp.events;

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


    public  AppEvent(int type){
        this.type = type;
    }
}
