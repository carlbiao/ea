package com.hadutech.glasses.engineerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dyhdyh.widget.loading.factory.LoadingFactory;
import com.hadutech.glasses.engineerapp.R;


public class ExLoadingFactory implements LoadingFactory {
    public static final int TYPE_SEND_MESSAGE = 1;
    public static final int TYPE_SCREEN_SHOT = 2;
    private int template;
    @Override
    public View onCreateView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(template, parent, false);
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_process_dialog_screenshot, parent, false);
        // View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_process_dialog_icon, parent, false);

        return view;
    }

    public ExLoadingFactory(int type){
        switch (type){
            case TYPE_SEND_MESSAGE:
                template = R.layout.loading_process_dialog_send_message;
                break;
            case TYPE_SCREEN_SHOT:
                template = R.layout.loading_process_dialog_screenshot;
                break;
        }
    }
}
