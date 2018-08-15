package com.hadutech.glasses.engineerapp;

public class ConfigData {
    /**
     * 登录以后的有效时长，单位：秒
     */
    public static final int LOGIN_TIME = 3600;

    public static final String REST_SERVICE_BASE_URL = "http://118.89.163.26/glasses";
    public static final String WS_SOCKET_URL = "https://118.89.163.26:3000";
    public static final String SHARE_PREFERENCES_PREFIX = "engineerApp_data";

    /**
     * 收到呼叫请求应答的时间，超过该时间还未应答，则自动挂断
     */
    public static final int ANSWER_TIMEOUT = 30 * 1000;
}
