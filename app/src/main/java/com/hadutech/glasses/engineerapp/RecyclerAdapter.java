package com.hadutech.glasses.engineerapp;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<RemoteVideo> mRecycleList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvDate;
        TextView tvName;
        TextView tvNumber;
        Button btnCheck;
        View checkView;

        public ViewHolder(View view){
            super(view);
             checkView=view;
             tvDate=(TextView)view.findViewById(R.id.tv_date);
             tvName=(TextView)view.findViewById(R.id.tv_name);
             tvNumber=(TextView)view.findViewById(R.id.tv_number);
             btnCheck=(Button)view.findViewById(R.id.btn_check);
        }
    }

    public RecyclerAdapter(List<RemoteVideo> recycleList){
        mRecycleList=recycleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                RemoteVideo remoteVideo=mRecycleList.get(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        RemoteVideo remoteVideo=mRecycleList.get(position);
        holder.tvDate.setText(remoteVideo.getTime());
        holder.tvName.setText(remoteVideo.getName());
        holder.tvNumber.setText(remoteVideo.getPersonId());
    }

    @Override
    public int getItemCount(){
        return mRecycleList.size();
    }
}
