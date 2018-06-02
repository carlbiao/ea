package com.hadutech.glasses.engineerapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    //声明变量
    private EditText nameInputText;
    private EditText passwordInputText;
    private EditText verification;
    private ImageView imgPic;
    private Button loginButton;
    private Bitmap bitmap;
    private String detail="";
    private boolean flag = false;
    private String result="";
    private boolean status=false;
    private final static String PIC_URL = "http://118.89.163.26/glasses/api/getCaptcha";
    private final static String LOGIN_URL = "http://118.89.163.26/glasses/login";
    //定义用户名，密码，验证码
    String n,p,v;

    private ListView testLv;//ListView组件
    private Button updateDataBtn;//动态加载数据组件

    private List<String> dataList = new ArrayList<>();//存储数据


    // 用于刷新界面
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                  imgPic.setVisibility(View.VISIBLE);
//                  imgPic.setImageBitmap(bitmap);
//                  Toast.makeText(MainActivity.this, "验证码加载完毕", Toast.LENGTH_SHORT).show();
//                  break;
//                case 2:
//
//                    Toast.makeText(MainActivity.this,"请输入工号和密码",Toast.LENGTH_LONG).show();
//            }
//        }
//
//
//    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setViews();




        Log.i("GetUtils","");
        //loginButton = findViewById(R.id.Login);
//        verification=findViewById(R.id.textshow);
//        nameInputText=findViewById(R.id.name);
//        passwordInputText=findViewById(R.id.password);
        loginButton=(Button)findViewById(R.id.Login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                EditText name=(EditText)findViewById(R.id.name);
//                n=name.getText().toString();
//                EditText pwd=(EditText)findViewById(R.id.password);
//                p=pwd.getText().toString();
//                EditText ver=(EditText)findViewById(R.id.textshow);
//                v=ver.getText().toString();

                Intent it=new Intent(LoginActivity.this,VideoList.class);
                startActivity(it);
                Log.e("MainAvtivity","onCreate execute");

//                new Thread(){
//                    @Override
//                    public void run(){
//                        //boolean status;
//
//                        if (status=true){
//                            Intent it=new Intent(MainActivity.this,VideoList.class);
//                            startActivity(it);
//
//                        }else {
//                            Toast.makeText(getApplicationContext(),"工号或密码错误",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }.start();


            }
        });

//        verification.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendHttpRequest();
//            }
//        });
//        new Thread(){
//            public void run(){
//                result=PostUtils.LoginByPost(nameInputText.getText().toString(),
//                        passwordInputText.getText().toString(),
//                        verification.getText().toString());
//                //引用谷歌的json包
//                Gson gson=new Gson();
//                ResultUser resultUser=gson.fromJson(result,ResultUser.class);
//                status=resultUser.isStatus();
//                handler.sendEmptyMessage(2);
//            };
//          }.start();
    }





    private void initView(){
//        nameInputText=(EditText)findViewById(R.id.name);
//        passwordInputText=(EditText)findViewById(R.id.password);
//        verification=(EditText)findViewById(R.id.textshow);
        loginButton=(Button)findViewById(R.id.Login);
    }

    private void setViews() {
        //imgPic=(ImageView)findViewById(R.id.imgPic);
        //verification=(EditText) findViewById(R.id.textshow);
        loginButton=(Button)findViewById(R.id.Login);

    }

//    private void sendHttpRequest(){
//
//        Thread sendThread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                URL url = null;
//                try {
//                    url = new URL(PIC_URL);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    // 设置连接超时为5秒
//                    conn.setConnectTimeout(5000);
//                    // 设置请求类型为Get类型
//                    conn.setRequestMethod("GET");
//                    InputStream inStream = conn.getInputStream();
//                    byte[] bt = StreamTool.read(inStream);
//                    String msg = new String(bt);
//                    bitmap = stringtoBitmap(msg);
//
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//               handler.sendEmptyMessage(1);
//            }
//        });
//        sendThread.start();
//
//    }



    //解码
    private Bitmap stringtoBitmap(String string){
        Bitmap bitmap=null;

        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
