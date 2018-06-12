package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class VideoAdapter extends ArrayAdapter<RemoteVideo> {
    private int resourceId;

    public VideoAdapter(Context context, int textResourceId, List<RemoteVideo> objects){
        super(context,textResourceId,objects);
        resourceId=textResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
         RemoteVideo video=getItem(position);
         View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView tvDate=(TextView)view.findViewById(R.id.tv_date);
        TextView tvName=(TextView)view.findViewById(R.id.tv_name);
        TextView tvNumber=(TextView)view.findViewById(R.id.tv_number);
        Button btnCheck=(Button)view.findViewById(R.id.btn_check);

        tvDate.setText(video.getTime());
        tvName.setText(video.getName());
        tvNumber.setText(video.getPersonId());

        return view;

    }
}
