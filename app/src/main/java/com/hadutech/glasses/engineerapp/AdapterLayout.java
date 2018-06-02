package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterLayout extends BaseAdapter implements View.OnClickListener{

    //定义两个类别标志
    private static final int Type_Data=0;
    private static final int Type_Listen=1;
    private Context mContext;
    private ArrayList<Object> mData=null;

    private Callback mCallback;

    public AdapterLayout(Context mContext,ArrayList<Object>mData){
        this.mContext=mContext;
        this.mData=mData;
//        mCallback=callback;
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public Object getItem(int position){
        return mData.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    //多布局的核心，通过这个判断类别
    @Override
    public int getItemViewType(int position){
        if (mData.get(position)instanceof WorkData){
            return Type_Data;
        }else if (mData.get(position)instanceof ListenDate){
            return Type_Listen;
        }else {
            return super.getItemViewType(position);
        }
    }

    //类别数目
    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent){
          int type=getItemViewType(position);
          ViewHolder1 holder1=null;
          ViewHolder2 holder2=null;
          if (convertView==null){
              switch (type){
                  case Type_Data:
                      holder1=new ViewHolder1();
                      convertView= LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
                      holder1.date=(TextView)convertView.findViewById(R.id.date);
                      holder1.name=(TextView)convertView.findViewById(R.id.name);
                      holder1.number=(TextView)convertView.findViewById(R.id.number);
                      holder1.check=(Button)convertView.findViewById(R.id.check);
                      convertView.setTag(R.id.Tag_Data,holder1);
                      break;
                  case Type_Listen:
                      holder2=new ViewHolder2();
                      convertView=LayoutInflater.from(mContext).inflate(R.layout.list_item2,parent,false);
                      holder2.date2=(TextView)convertView.findViewById(R.id.date2);
                      holder2.name2=(TextView)convertView.findViewById(R.id.name2);
                      holder2.number2=(TextView)convertView.findViewById(R.id.number2);
                      holder2.newIV=(TextView) convertView.findViewById(R.id.newIV);
                      holder2.start=(Button)convertView.findViewById(R.id.start);
                      holder2.stop=(Button)convertView.findViewById(R.id.stop);
                      convertView.setTag(R.id.Tag_Listen,holder2);
                      break;
              }
          }else {
              switch (type){
                  case Type_Data:
                      holder1=(ViewHolder1)convertView.getTag(R.id.Tag_Data);
                      break;
                  case Type_Listen:
                      holder2=(ViewHolder2)convertView.getTag(R.id.Tag_Listen);
                      break;
              }
          }
          Object object=mData.get(position);
          //设置下控件的值
        switch (type){
            case Type_Data:
                WorkData workData=(WorkData) object;
                if (workData!=null) {
                    holder1.date.setText(workData.getDate());
                    holder1.name.setText(workData.getName());
                    holder1.number.setText(workData.getNumber());
                    Log.e("AdapterLayout", "getView");
                }
                break;
            case Type_Listen:
                ListenDate listenDate=(ListenDate)object;
                if (listenDate!=null){
                    holder2.date2.setText(listenDate.getDate2());
                    holder2.name2.setText(listenDate.getName2());
                    holder2.number2.setText(listenDate.getNumber2());
                }
                break;
        }

//        holder1.check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                monItemButton.onCheck();
//            }
//        });
//        holder1.check.setOnClickListener(this);


        return convertView;
    }

    //两个不同的ViewHolder
    private static class ViewHolder1{
        TextView date;
        TextView name;
        TextView number;
        Button check;
    }

    private static class ViewHolder2{
        TextView date2;
        TextView name2;
        TextView number2;
        TextView newIV;
        Button start;
        Button stop;
    }

    public void add(WorkData workData){
        if (mData==null){
            mData=new ArrayList<>();
        }
        mData.add(workData);
        notifyDataSetChanged();
    }

    //用于回调的抽象类
//    public static abstract class MyClickListener implements View.OnClickListener{
//        @Override
//        public void onClick(View v){
//            myOnClick((Integer)v.getTag(),v);
//        }
//
//        public abstract void myOnClick(int position,View v);
//    }

    //自定义接口，用于回调按钮点击事件到Activity
    public interface Callback{
        public void click(View view);
    }

    @Override
    public void onClick(View view){
        mCallback.click(view);
    }

//    public interface onItemButton{
//        void onCheck();
//    }
//
//    private onItemButton monItemButton;
//
//    public void setOnItemButton(onItemButton monItemButton){
//        this.monItemButton=monItemButton;
//    }
//    @Override
//    public void onClick(View view){
//        switch (view.getId()){
//            case R.id.check:
//                Intent intent=new Intent(AdapterLayout.this,PlayAudioTest.class);
//                startActivity(intent);
//        }
//    }


}
