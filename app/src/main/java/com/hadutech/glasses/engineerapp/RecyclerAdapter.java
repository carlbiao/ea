package com.hadutech.glasses.engineerapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<RemoteVideo> list;
    private OnItemClickListener onItemClickListener = null;

    public RecyclerAdapter(List<RemoteVideo> list){
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_recycler_list_item, parent, false);
        RecyclerAdapter.ViewHolder viewHolder = new RecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, final int position) {
        RemoteVideo remoteVideo = list.get(position);
        if (remoteVideo.getType() == RemoteVideo.TYPE_VOICE) {
            holder.answerButton.setVisibility(View.GONE);
            holder.hangupButton.setVisibility(View.GONE);
            holder.btnCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onViewItemClick(list.get(position));
                    }
                }
            });
        } else {
            holder.btnCheck.setVisibility(View.GONE);
            holder.answerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onAnswerClick(list.get(position));
                    }
                }
            });

            holder.hangupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onHangupClick(list.get(position));
                    }
                }
            });
        }

        holder.tvDate.setText(remoteVideo.getTime());
        holder.tvName.setText(remoteVideo.getName() + "：" + remoteVideo.getPersonId());
    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    @Override
    public int getItemViewType(int position){
        list.get(position).getType();
        return list.get(position).getType();
    }

    public void addItem(int index,RemoteVideo s){
        list.add(index,s);
        notifyItemInserted(index);
    }

    public void addItem(RemoteVideo s){
        list.add(s);
        notifyItemInserted(list.size()-1);
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvName;
        Button btnCheck;
        Button answerButton;
        Button hangupButton;

        ViewHolder(View view) {
            super(view);
            tvDate = view.findViewById(R.id.tv_date);
            tvName = view.findViewById(R.id.tv_name);
            btnCheck = view.findViewById(R.id.btn_check);
            answerButton = view.findViewById(R.id.btn_video_recycler_answer);
            hangupButton = view.findViewById(R.id.btn_video_recycler_hangup);
        }
    }

    public interface OnItemClickListener{
        void onViewItemClick(RemoteVideo item);
        void onAnswerClick(RemoteVideo item);
        void onHangupClick(RemoteVideo item);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }
}
