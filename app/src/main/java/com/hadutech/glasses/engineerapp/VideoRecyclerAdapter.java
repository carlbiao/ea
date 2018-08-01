package com.hadutech.glasses.engineerapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder> {

    private List<RemoteVideo> list;
    private HashSet<String> idSet;
    private OnItemClickListener onItemClickListener = null;


    public VideoRecyclerAdapter(List<RemoteVideo> list){
        this.list = list;
        this.idSet = new HashSet<>();
        for(RemoteVideo remoteVideo : list){
            this.idSet.add(remoteVideo.getId());
        }
    }

    @NonNull
    @Override
    public VideoRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_recycler_list_item, parent, false);
        VideoRecyclerAdapter.ViewHolder viewHolder = new VideoRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoRecyclerAdapter.ViewHolder holder, final int position) {
        RemoteVideo remoteVideo = list.get(position);
        if (remoteVideo.getType() == RemoteVideo.TYPE_VOICE) {
            holder.answerButton.setVisibility(View.GONE);
            holder.hangupButton.setVisibility(View.GONE);
            holder.btnCheck.setVisibility(View.VISIBLE);
            if(!remoteVideo.isStatus()){
                holder.tvNewMarker.setVisibility(View.VISIBLE);
            }
            holder.btnCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onViewItemClick(list.get(position));
                    }
                }
            });
        } else {
            holder.answerButton.setVisibility(View.VISIBLE);
            holder.hangupButton.setVisibility(View.VISIBLE);
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

    public void addItem(int index,RemoteVideo s){
        if(idSet.contains( s.getId())){
            return;
        }
        list.add(index,s);
        idSet.add(s.getId());
        notifyItemInserted(index);
    }

    public void addItem(RemoteVideo s){
        if(idSet.contains( s.getId())){
            return;
        }
        list.add(s);
        idSet.add(s.getId());
        notifyItemInserted(list.size()-1);
    }

    public void removeItem(int index){
        RemoteVideo removeItem = list.remove(index);
        idSet.remove(removeItem.getId());
        notifyItemRemoved(index);
    }

    public void updateItem(int index,RemoteVideo item){
        list.set(index,item);
        notifyItemChanged(index);
    }

    public void removeItemBySocketId(String socketId){
        for(RemoteVideo video : list){
            if(socketId.equals(video.getRemoteSocketId())){
                int index = list.indexOf(video);
                removeItem(index);
                idSet.remove(video.getId());
                return;
            }
        }
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvName;
        Button btnCheck;
        Button answerButton;
        Button hangupButton;
        TextView tvNewMarker;

        ViewHolder(View view) {
            super(view);
            tvDate = view.findViewById(R.id.tv_date);
            tvName = view.findViewById(R.id.tv_name);
            btnCheck = view.findViewById(R.id.btn_check);
            answerButton = view.findViewById(R.id.btn_video_recycler_answer);
            hangupButton = view.findViewById(R.id.btn_video_recycler_hangup);
            tvNewMarker = view.findViewById(R.id.tv_new_mark);
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
