package com.hadutech.glasses.engineerapp;

import android.os.Bundle;

/**
 * Created by wenyb on 2018/4/23.
 */

public class RemoteVideo {
    /**
     * 远程视频
     */
    public static final int TYPE_RTC = 1;

    /**
     * 历史留言
     */
    public static final int TYPE_VOICE = 1;

    /**
     * 时间
     */
    private String time;

    /**
     * 人员名称
     */
    private String name;

    /**
     *  状态，0：未读；1：已读
     */
    private boolean status;

    /**
     * 语音ID
     */
    private String id;

    /**
     * 类型
     */
    private int type;

    /**
     * 远程视频远端的socketId
     */
    private String remoteSocketId;

    /**
     * 远程人员ID
     */
    private String personId;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getRemoteSocketId() {
        return remoteSocketId;
    }

    public void setRemoteSocketId(String remoteSocketId) {
        this.remoteSocketId = remoteSocketId;
    }



    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bundle toBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("personId",this.getPersonId());
        bundle.putString("name",this.getName());
        bundle.putString("remoteSocketId",this.getRemoteSocketId());
        bundle.putString("time",this.getTime());
        bundle.putString("id",this.getId());
        bundle.putInt("type",this.getType());
        return bundle;
    }

    public static RemoteVideo toRemoteVideo(Bundle bundle){
        RemoteVideo remoteVideo = new RemoteVideo();
        remoteVideo.setPersonId(bundle.getString("personId"));
        remoteVideo.setName(bundle.getString("name"));
        remoteVideo.setRemoteSocketId(bundle.getString("remoteSocketId"));
        remoteVideo.setTime(bundle.getString("time"));
        remoteVideo.setId(bundle.getString("id"));
        remoteVideo.setType(bundle.getInt("type"));
        return remoteVideo;
    }
}
