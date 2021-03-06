package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hadutech.glasses.engineerapp.events.RtcEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

public class RtcClient {

    //-----------类的静态常量部分-------------
    public static final int RTC_CLIENT_TYPE_ENGINEER = 2;
    public static final int RTC_CLIENT_TYPE_EMPLOYEE = 1;
    public static final int RTC_MESSAGE_TYPE_JOIN_COMPLETE = 100;
    public static final int RTC_MESSAGE_TYPE_LEAVE = 101;
    public static final int RTC_MESSAGE_TYPE_ONLINE_ENGINEER_LIST = 102;
    public static final int RTC_MESSAGE_TYPE_CALL_ANSWER = 103;
    public static final int RTC_MESSAGE_TYPE_CALL = 104;
    public static final int RTC_MESSAGE_TYPE_REFUSE = 105;//工程师端拒绝
    public static final int RTC_MESSAGE_TYPE_TIMEOUT = 106;//呼叫超时
    public static final int RTC_MESSAGE_TYPE_ICECONNECTIONCHANGE = 107;//ICE状态更改事件
    public static final int RTC_MESSAGE_TYPE_RECEIVE_MESSAGE = 108;//收到工程师发来的消息
    public static final int RTC_MESSAGE_TYPE_RECEIVE_REMOTE_VIDEO = 109;//收到远端的视频画面

    private static final String TAG = "RTCClient";

    private static RtcClient _instance = null;

    public static RtcClient getInstance() {
        if (_instance == null) {
            _instance = new RtcClient();
        }
        return _instance;
    }

    private RtcClient() {
        //初始化coTurn server地址
        iceServers.add(new PeerConnection.IceServer("stun:" + ConfigData.TURN_SERVER));
        iceServers.add(new PeerConnection.IceServer("turn:" + ConfigData.TURN_SERVER + "?transport=tcp", ConfigData.TURN_SERVER_USER, ConfigData.TURN_SERVER_PASSWORD));
        iceServers.add(new PeerConnection.IceServer("turn:" + ConfigData.TURN_SERVER + "?transport=udp", ConfigData.TURN_SERVER_USER, ConfigData.TURN_SERVER_PASSWORD));

        //初始化媒体条件
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }


    //----------------私有成员------------------
    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private MediaConstraints pcConstraints = new MediaConstraints();
    private LocalPeerSdpObserver localSdpObserver = null;
    private GLSurfaceView localVideoView = null;
    private Handler rtcHandler = null;
    private Socket rtcClient;
    private String name;
    private String personId;
    private int rtcType;
    private Peer peer = null;
    private boolean socketConnected = false;//socket是否已经连接

    /**
     * 视频通信中
     */
    private volatile boolean onCalling = false;

    private MediaStream localMediaStream = null;//本地媒体流

    private VideoSource videoSource = null;//本地视频源
    private AudioSource audioSource = null;//本地音频源
    private Gson gson = new Gson();


    //---------------处理信令服务器相关逻辑-------------

    /**
     * 支持https连接
     */
    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }};

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * 连接到信令服务器
     *
     * @param name     工程师姓名
     * @param personId 工程师id
     * @param type     连接类型
     */
    public void connect(String name, String personId, int type) {
        //建立socket连接
        if (socketConnected) {
            return;
        }
        SSLContext sc = null;
        try {
            //https支持
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(new RelaxedHostNameVerifier())
                    .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .build();

            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;

            rtcClient = IO.socket(ConfigData.WS_SOCKET_URL, opts);
            this.name = name;
            this.personId = personId;
            this.rtcType = type;

            MessageHandler messageHandler = new MessageHandler();

            rtcClient.on("id", messageHandler.onId);
            rtcClient.on("message", messageHandler.onMessage);
            rtcClient.on("serverError", messageHandler.serverError);
            rtcClient.on("disconnect", messageHandler.serverError);
            rtcClient.on("error", messageHandler.error);
            rtcClient.connect();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "", e);
        } catch (URISyntaxException e) {
            Log.e(TAG, "", e);
        }
    }

    public void setRtcHandler(Handler socketHandler) {
        this.rtcHandler = socketHandler;
    }

    /**
     * TODO 需要测试
     * 关闭所有连接，只能在注销/应用程序销毁时调用
     */
    public void close() {
        if (socketConnected) {
            rtcClient.close();
            if (this.videoSource != null) {
                this.videoSource.stop();
            }
//            if(this.audioSource != null){
//                this.audioSource.;
//            }
            if (this.peer != null) {
                this.peer.pc.close();
                this.peer.localDataChannel.dispose();
                this.peer = null;
            }
        }
        socketConnected = false;
    }

    /**
     * TODO 需要测试
     * 挂断
     */
    public void hungup() {
        //localMediaStream.dispose();
        if (this.videoSource != null) {
            this.videoSource.stop();
        }
//        if(this.audioSource != null){
//            try{
//                this.audioSource.dispose();
//            }catch (Exception ex){
//                Log.e(TAG,"yichang");
//            }
//
//        }

        if (this.peer != null) {
            this.peer.pc.close();
            //this.peer.pc.dispose();
            //this.peer.localDataChannel.dispose();
            this.peer.localDataChannel.close();
            this.peer = null;
        }
    }

    public void sendImageToPeer(String imageContent) {
        Long msgNum = System.currentTimeMillis();//使用时间戳作为消息编号
        if (this.peer != null && this.peer.pc != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "base64");
            jsonObject.addProperty("content", imageContent);

            String sendContent = gson.toJson(jsonObject);
            StringBuilder stringBuilder = new StringBuilder(sendContent);
            int MAX_TRUNK = 30000;
            int packageNum = (int) Math.ceil(sendContent.length() / MAX_TRUNK) + 1;
            for (int i = 0; i < packageNum; i++) {
                //如果content大于MAX_TRUNK长度，则分包发送
                int end = (i + 1) * MAX_TRUNK;
                if (end >= stringBuilder.length()) {
                    end = stringBuilder.length();
                }
                String tmpContent = stringBuilder.substring(i * MAX_TRUNK, end);
                JsonObject sendJsonObject = new JsonObject();
                sendJsonObject.addProperty("id", msgNum);
                sendJsonObject.addProperty("total", packageNum);
                sendJsonObject.addProperty("content", tmpContent);
                sendJsonObject.addProperty("index", i);

                DataChannel.Buffer buffer = new DataChannel.Buffer(ByteBuffer.wrap(gson.toJson(sendJsonObject).getBytes()), false);
                this.peer.remoteDataChannel.send(buffer);
            }
        }
    }

    /**
     * 信令服务器消息回调处理Handler类
     */
    private class MessageHandler {

        private MessageHandler() {

        }

        /**
         * 处理不同类型的信令消息
         */
        private Emitter.Listener onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Log.i(TAG, "Receive single server message, message = " + data.toString());
                try {
                    String messageType = data.getString("type");
                    if (messageType.equals("joinComplete")) {
                        rtcHandler.sendEmptyMessage(RTC_MESSAGE_TYPE_JOIN_COMPLETE);
                    } else if (messageType.equals("leave")) {
                        rtcHandler.sendEmptyMessage(RTC_MESSAGE_TYPE_LEAVE);
                    } else if (messageType.equals("onlineEngineerList")) {
                        //拿到工程师在线状态
                        Message message = new Message();
                        message.obj = data.getJSONObject("result");
                        message.what = RTC_MESSAGE_TYPE_ONLINE_ENGINEER_LIST;
                        rtcHandler.sendMessage(message);
                    } else if (messageType.equals("callAnswer")) {
                        //呼叫后，工程师那边接听答复，创建offer
                        String remoteId = data.getString("from");
                        peer = new Peer(remoteId);
                        localSdpObserver = new LocalPeerSdpObserver(remoteId, peer.pc);
                        peer.pc.createOffer(localSdpObserver, pcConstraints);
                    } else if (messageType.equals("call")) {
                        JSONObject streamData = data.getJSONObject("stream");
                        //收到员工的call
                        if (onCalling) {
                            sendMessage(String.valueOf(streamData.get("streamId")), "refuse", null);
                            return;
                        }
                        onCalling = true;
                        RtcEvent event = new RtcEvent(RtcEvent.EVENT_TYPE_ON_CALL);

                        event.setName(String.valueOf(streamData.get("name")));
                        event.setPersonId(String.valueOf(streamData.get("personId")));
                        event.setRemoteSocketId(String.valueOf(streamData.get("streamId")));
                        String code = String.valueOf(data.get("code"));
                        event.setId(code);
                        EventBus.getDefault().post(event);
                    } else if (messageType.equals("offer")) {
                        //收到offer
                        String remoteSocketId = (String) data.get("from");
                        JSONObject payload = data.getJSONObject("payload");

                        peer = new Peer(remoteSocketId);
                        SessionDescription sdp = null;
                        try {
                            sdp = new SessionDescription(SessionDescription.Type.OFFER, payload.getString("sdp"));
                        } catch (JSONException e) {
                            Log.e(TAG, "", e);
                        }
                        RemotePeerSdpObserver sdpObserver = new RemotePeerSdpObserver(remoteSocketId, peer.pc);
                        //先设置远端的sdp对象
                        peer.pc.setRemoteDescription(sdpObserver, sdp);
                    } else if (messageType.equals("answer")) {
                        //呼叫后，工程师那边接听答复
                        SessionDescription sdp = new SessionDescription(
                                SessionDescription.Type.ANSWER,
                                data.getJSONObject("payload").getString("sdp")
                        );
                        peer.pc.setRemoteDescription(localSdpObserver, sdp);
                    } else if (messageType.equals("candidate")) {
                        String sdpMid = data.getJSONObject("payload").getString("id");
                        int sdpMLineIndex = data.getJSONObject("payload").getInt("label");
                        String sdp = data.getJSONObject("payload").getString("candidate");
                        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

                        peer.pc.addIceCandidate(iceCandidate);
                    } else if (messageType.equals("refuse")) {
                        //拒绝通话
                        rtcHandler.sendEmptyMessage(RTC_MESSAGE_TYPE_REFUSE);
                    } else if (messageType.equals("hungup")) {
                        //TODO 接收到对方挂断消息，主动关闭peer
                        if (videoSource != null) {
                            videoSource.stop();
                        }
                        if (peer != null) {
                            peer.pc.close();
                            peer.localDataChannel.close();
                            peer = null;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }
            }
        };

        /**
         * 和信令服务器成功建立socket连接后拿到ID
         */
        private Emitter.Listener onId = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketConnected = true;
                String id = (String) args[0];
                Log.i(TAG, "Connect to the single server, id = " + id);

                JSONObject message = new JSONObject();
                try {
                    message.put("personId", personId);
                    message.put("name", name);
                    message.put("type", rtcType);
                    Log.i(TAG, "Send join message to single server, message " + message.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }

                rtcClient.emit("join", message);
            }
        };

        /**
         * 信令服务器业务逻辑错误回调
         */
        private Emitter.Listener serverError = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String msg = (String) args[0];
                socketConnected = false;
                Log.e(TAG, "Server error!" + msg);

            }
        };
        /**
         * 网络异常回调
         */
        private Emitter.Listener error = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "error!" + args[0]);
            }
        };
    }

    /**
     * 给信令服务器发送消息
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        rtcClient.emit("message", message);
    }

    public void getEngineersOnlineStatus(List<String> engieers) {
        JSONArray engineerPostDatas = new JSONArray();
        for (String pid : engieers) {
            engineerPostDatas.put(pid);
        }

        rtcClient.emit("onlineEngineerList", engineerPostDatas);
    }

    public void callTo(String engineerId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("personId", engineerId);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        //发送call
        rtcClient.emit("call", jsonObject);
    }

    public void refuse(String remoteSocketId) {
        try {
            sendMessage(remoteSocketId, "refuse", null);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }

        onCalling = false;
    }

    //--------------------处理信令服务器相关逻辑结束-------------------------------------

    //--------------------处理RTC相关逻辑-------------------

    public void startAnswer(RemoteVideo remoteVideo) {
        //回复
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "callAnswer");
            jsonObject.put("from", rtcClient.id());
            jsonObject.put("to", remoteVideo.getRemoteSocketId());
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }

        rtcClient.emit("message", jsonObject);
    }


    public void startCamera(Context context, GLSurfaceView localVideoView, boolean initializeVideo, boolean initializeAudio, int videoWidth, int videoHeight) {
        if (factory == null) {
            PeerConnectionFactory.initializeAndroidGlobals(context, initializeAudio, true, true, null);
            // PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            factory = new PeerConnectionFactory();
        }

        localMediaStream = factory.createLocalMediaStream("102");

        if (initializeVideo) {

            //启动相机
            String frontFacingCam = VideoCapturerAndroid.getNameOfFrontFacingDevice();//前面的摄像头
            String backFacingCam = VideoCapturerAndroid.getNameOfBackFacingDevice();//后面的摄像头
            //TODO 使用前置摄像头，眼镜需要改为使用后端摄像头
            VideoCapturer videoCapturerAndroid = VideoCapturerAndroid.create(frontFacingCam);
            MediaConstraints videoConstraints = new MediaConstraints();//使用720p
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", "1280"));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", "720"));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", "640"));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight", "480"));
            videoSource = factory.createVideoSource(videoCapturerAndroid, videoConstraints);
            VideoTrack localVideoTrack = factory.createVideoTrack("100", videoSource);
            if (localVideoView != null) {
                this.localVideoView = localVideoView;
                VideoRenderer.Callbacks localRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
                localVideoTrack.addRenderer(new VideoRenderer(localRender));
            }
            localMediaStream.addTrack(localVideoTrack);
        }
        if (initializeAudio) {
            //启动麦克风
            audioSource = factory.createAudioSource(pcConstraints);
            AudioTrack localAudioTrack = factory.createAudioTrack("101", audioSource);
            localMediaStream.addTrack(localAudioTrack);
        }
    }


    public void onPause() {
        if (this.localVideoView != null) {
            this.localVideoView.onPause();
            this.videoSource.stop();
        }

    }

    public void onResume() {
        if (this.localVideoView != null) {
            this.localVideoView.onResume();
            this.videoSource.restart();
        }

    }

    public void onDestroy() {
        hungup();
    }

    /**
     * RTC端点处理对象
     */
    private class Peer implements PeerConnection.Observer, DataChannel.Observer {
        private PeerConnection pc;
        private DataChannel localDataChannel;
        private DataChannel remoteDataChannel;
        private String remoteSocketId;
        private Map<String, StringBuilder> receivedMsgMap = new ConcurrentHashMap<>();

        public Peer(String remoteSocketId) {
            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
            this.remoteSocketId = remoteSocketId;
            pc.addStream(localMediaStream);
            DataChannel.Init init = new DataChannel.Init();
            this.localDataChannel = this.pc.createDataChannel("sendChannel", init);
            this.localDataChannel.registerObserver(this);
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            //if (iceConnectionState == ) {
            Log.e(TAG, "onIceConnectionChange:" + iceConnectionState.toString());
            String state = iceConnectionState.toString();
            if (state.equals("DISCONNECTED")) {
                state = "CLOSED";
            }
            if (state.equals("CLOSED")) {
                try {
                    sendMessage(remoteSocketId, "hungup", null);
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }

                onCalling = false;
            }
            if (state.equals("COMPLETED")) {
                onCalling = true;
            }

            Message msg = new Message();
            msg.what = RTC_MESSAGE_TYPE_ICECONNECTIONCHANGE;
            msg.obj = state;
            rtcHandler.sendMessage(msg);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                sendMessage(remoteSocketId, "candidate", payload);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.e(TAG, "onAddStream " + mediaStream.label());
            Message msg = new Message();
            msg.what = RTC_MESSAGE_TYPE_RECEIVE_REMOTE_VIDEO;
            msg.obj = mediaStream;
            rtcHandler.sendMessage(msg);
            //mediaStream.videoTracks.getFirst();
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.e(TAG, "onRemoveStream " + mediaStream.label());
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            this.remoteDataChannel = dataChannel;
        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {

        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            ByteBuffer data = buffer.data;
            final byte[] bytes = new byte[data.capacity()];
            data.get(bytes);
            String msg = new String(bytes);
            JsonObject msgJson = gson.fromJson(msg, JsonObject.class);
            String msgId = msgJson.get("id").getAsString();
            int total = msgJson.get("total").getAsInt();
            int index = msgJson.get("index").getAsInt();
            if (total == 1) {
                //不分包
                String content = msgJson.get("content").getAsString();
                Message message = new Message();
                message.what = RTC_MESSAGE_TYPE_RECEIVE_MESSAGE;
                message.obj = content;
                rtcHandler.sendMessage(message);
            } else {
                StringBuilder stringBuilder;
                //分包
                if (index == 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(msgJson.get("content").getAsString());
                    this.receivedMsgMap.put(msgId, stringBuilder);
                } else {
                    stringBuilder = this.receivedMsgMap.get(msgId);
                    stringBuilder.append(msgJson.get("content").getAsString());
                }
                if (index == (total - 1)) {
                    //已经拿到所有分包
                    String allContent = stringBuilder.toString();
                    this.receivedMsgMap.remove(msgId);

                    Message message = new Message();
                    message.what = RTC_MESSAGE_TYPE_RECEIVE_MESSAGE;
                    message.obj = allContent;
                    rtcHandler.sendMessage(message);
                }
            }
        }
    }

    /**
     * 处理远端sdp对象
     */
    private class RemotePeerSdpObserver implements SdpObserver {
        private String remoteId;
        private PeerConnection pc;
        private boolean isSetLocal = false;
        private JSONObject payload = null;

        public RemotePeerSdpObserver(String remoteId, PeerConnection pc) {
            this.remoteId = remoteId;
            this.pc = pc;
        }

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            // 创建answer成功
            try {
                payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                isSetLocal = true;
                pc.setLocalDescription(this, sdp);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }
        }

        @Override
        public void onSetSuccess() {
            //设置本地则不需要答复了，
            if (isSetLocal == false) {
                //创建答复
                peer.pc.createAnswer(this, pcConstraints);
            } else {
                Log.e(TAG, "set Local SDP complete!");
                try {
                    sendMessage(this.remoteId, "answer", payload);
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }
            }
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure" + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure" + s);
        }
    }

    /**
     * 处理本地sdp对象
     */
    private class LocalPeerSdpObserver implements SdpObserver {
        private String remoteId;
        private PeerConnection pc;
        private JSONObject payload = null;

        public LocalPeerSdpObserver(String remoteId, PeerConnection pc) {
            this.remoteId = remoteId;
            this.pc = pc;
        }

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            // 创建offer成功
            try {
                payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                pc.setLocalDescription(this, sdp);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }
        }

        @Override
        public void onSetSuccess() {
            try {
                sendMessage(this.remoteId, "offer", payload);
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure" + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure" + s);
        }
    }

}
