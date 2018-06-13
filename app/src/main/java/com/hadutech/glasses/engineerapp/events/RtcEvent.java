package com.hadutech.glasses.engineerapp.events;

public class RtcEvent {

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

    public String getRemoteSocketId() {
        return remoteSocketId;
    }

    public void setRemoteSocketId(String remoteSocketId) {
        this.remoteSocketId = remoteSocketId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

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
     * 远程视频远端的socketId
     */
    private String remoteSocketId;

    /**
     * 远程人员ID
     */
    private String personId;
}
