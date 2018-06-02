package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VideoList extends AppCompatActivity implements View.OnClickListener{



    private static final int Type_Data=0;
    private static final int Type_Listen=1;
    private ListView list_content;
    private ListView add_list;
    private ArrayList<Object> mData=null;
    private AdapterLayout myAdapter=null;

    private Button btn_add;
    private Button check;

    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
//        ToolBarView toolBarView=new ToolBarView(getBaseContext());
//        linearLayout=(LinearLayout)findViewById(R.id.ll_content);
//        toolBarView.setTitle(R.id.tv_title)
//                .setRightIcon(R.id.iv_right)
//                .setRightIconOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                }).addIn(linearLayout);//一定要调用这个方法，不然就没有实现这个标题栏


        new TitleBuilder(this).setTitleText("远程视频链接").setIv_right(R.drawable.ic_me);
        Log.e("VideoList","onCreate execute");

        //准备数据
        mData=new ArrayList<Object>();
        for (int i=0;i<4;i++){
            switch ((int)(Math.random()*2)){
                case Type_Data:
                    mData.add(new WorkData("2018-5-20","张三:","123456"));
                    break;
                case Type_Listen:
                    mData.add(new ListenDate("2018-5-23","李四:","4356324"));
                    break;
            }
        }
        bindViews();

        ListView list_content=(ListView)findViewById(R.id.list_content);
        myAdapter=new AdapterLayout(VideoList.this,mData);
        list_content.setAdapter(myAdapter);


        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(VideoList.this,ProjectMessage.class);
                startActivity(intent);
            }
        });




    }







    private void bindViews(){
        list_content=(ListView)findViewById(R.id.list_content);
        add_list=(ListView)findViewById(R.id.add_list);
        btn_add=(Button)findViewById(R.id.btn_add);

        list_content.setAdapter(myAdapter);
        add_list.setAdapter(myAdapter);
        btn_add.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_add:
                myAdapter.add(new WorkData("2018-5-20","添加","123456"));
                break;


        }
    }







}
