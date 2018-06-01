package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by Administrator on 2018/4/23.
 */

public class RemoteVideoAdapter extends BaseAdapter implements View.OnClickListener {

    private LinkedList<RemoteVideo> list = null;
    private Context context;

    private InnerItemOnclickListener innerItemOnClickListener = null;

    public RemoteVideoAdapter(LinkedList<RemoteVideo> data, Context context ){
        this.list = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RemoteVideo item = list.get(position);
        ViewHolder holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.remote_call_item,parent,false);
            holder = new ViewHolder();
            holder.callDateTextView = convertView.findViewById(R.id.call_date);
            holder.newMarkTextView = convertView.findViewById(R.id.call_item_new_text);
            holder.employeeTextView = convertView.findViewById(R.id.employee_name);
            holder.answerButton = convertView.findViewById(R.id.button_answer);
            holder.hangupButton = convertView.findViewById(R.id.button_hangup);
            convertView.setTag(holder);   //将Holder存储到convertView中
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.answerButton.setOnClickListener(this);
        holder.answerButton.setTag(item);
        holder.hangupButton.setOnClickListener(this);
        holder.hangupButton.setTag(item);


        holder.callDateTextView.setText(item.getTime());
        holder.employeeTextView.setText(item.getName());

        return convertView;
    }

    public void add(RemoteVideo data){
        if(list == null){
            list = new LinkedList<>();
        }
        list.add(data);
        notifyDataSetChanged();
    }

    public void add(int position,RemoteVideo data){
        if(list == null){
            list = new LinkedList<>();
        }
        list.add(position,data);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if(this.innerItemOnClickListener != null){
            this.innerItemOnClickListener.itemClick(v);
        }
    }

    static class ViewHolder{
        TextView callDateTextView;
        TextView newMarkTextView;
        TextView employeeTextView;
        Button answerButton;
        Button hangupButton;
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.innerItemOnClickListener = listener;
    }


}
