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
    private static final int TYPE_VOICE=0;
    private static final int TYPE_RTC=1;
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
        if (mData.get(position)instanceof RemoteVideo){
            return TYPE_VOICE;
        }else if (mData.get(position)instanceof RemoteVideo){
            return TYPE_RTC;
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
                  case TYPE_VOICE:
                      holder1=new ViewHolder1();
                      convertView= LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
                      holder1.date=(TextView)convertView.findViewById(R.id.tv_date);
                      holder1.name=(TextView)convertView.findViewById(R.id.tv_name);
                      holder1.number=(TextView)convertView.findViewById(R.id.tv_number);
                      holder1.check=(Button)convertView.findViewById(R.id.btn_check);
                      convertView.setTag(R.id.Tag_Data,holder1);
                      break;
                  case TYPE_RTC:
                      holder2=new ViewHolder2();
                      convertView=LayoutInflater.from(mContext).inflate(R.layout.list_item2,parent,false);
                      holder2.date2=(TextView)convertView.findViewById(R.id.tv_date2);
                      holder2.name2=(TextView)convertView.findViewById(R.id.tv_name2);
                      holder2.number2=(TextView)convertView.findViewById(R.id.tv_number2);
                      holder2.newIV=(TextView) convertView.findViewById(R.id.tv_newIV);
                      holder2.start=(Button)convertView.findViewById(R.id.btn_start);
                      holder2.stop=(Button)convertView.findViewById(R.id.btn_stop);
                      convertView.setTag(R.id.Tag_Listen,holder2);
                      break;
              }
          }else {
              switch (type){
                  case TYPE_VOICE:
                      holder1=(ViewHolder1)convertView.getTag(R.id.Tag_Data);
                      break;
                  case TYPE_RTC:
                      holder2=(ViewHolder2)convertView.getTag(R.id.Tag_Listen);
                      break;
              }
          }
          Object object=mData.get(position);
          //设置下控件的值
        switch (type){
            case TYPE_VOICE:
                WorkData workData=(WorkData) object;
                if (workData!=null) {
                    holder1.date.setText(workData.getDate());
                    holder1.name.setText(workData.getName());
                    holder1.number.setText(workData.getNumber());
                    Log.e("AdapterLayout", "getView");
                }
                break;
            case TYPE_RTC:
                ListenDate listenDate=(ListenDate)object;
                if (listenDate!=null){
                    holder2.date2.setText(listenDate.getDate2());
                    holder2.name2.setText(listenDate.getName2());
                    holder2.number2.setText(listenDate.getNumber2());
                }
                break;
        }



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

    public interface Callback{
        public void click(View view);
    }

    @Override
    public void onClick(View view){
        mCallback.click(view);
    }


}
