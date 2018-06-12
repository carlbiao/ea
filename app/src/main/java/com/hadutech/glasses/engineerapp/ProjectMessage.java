package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ProjectMessage extends AppCompatActivity {

    private Button play_music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_message);

        new LogoActivity(this).setIb_left(R.drawable.ic_back).setLogoText("远程视频列表");


        Button play_music=(Button)findViewById(R.id.play_music);
        play_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProjectMessage.this,PlayAudioTest.class);
                startActivity(intent);
            }
        });

    }

    private View.OnClickListener leftReturnListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(ProjectMessage.this,VideoListActivity.class);
            startActivity(intent);
        }
    };
}
