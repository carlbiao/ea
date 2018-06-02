package com.hadutech.glasses.engineerapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class PlayAudioTest extends AppCompatActivity implements View.OnClickListener{

    private MediaPlayer mediaPlayer=new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio_test);
        Button play=(Button)findViewById(R.id.play);
//        Button pause=(Button)findViewById(R.id.pause);
        Button stop=(Button)findViewById(R.id.stop);
        play.setOnClickListener(this);
//        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(PlayAudioTest.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PlayAudioTest.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }else {
            //初始化MediaPlayer
            initMediaPlayer();
        }
    }

    private void initMediaPlayer(){
        try {
            //File file=new File(Environment.getExternalStorageDirectory(),"music.mp3");
            //mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.setDataSource(this, Uri.parse("http://118.89.163.26/static/resource/issue/2018/4/22/987749837531250688.mp3"));
            mediaPlayer.prepare();//让MediaPlayer
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else {
                    Toast.makeText(this,"拒绝权限将无法使用程序",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.play:
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();//开始播放
                }
                break;
//            case R.id.pause:
//                if (!mediaPlayer.isPlaying()){
//                    mediaPlayer.pause();//暂停播放
//                }
//                break;
            case R.id.stop:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.reset();//停止播放
                    initMediaPlayer();
                }
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
