package com.hadutech.glasses.engineerapp;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogoActivity {

    private View logoView;
    private LinearLayout logoBar;
    private ImageView ib_left;
    private TextView msg_title;

    //构造方法：用于获取对象
    public LogoActivity(Activity context){
        logoView=context.findViewById(R.id.logo_bar);
        logoBar=(LinearLayout)logoView.findViewById(R.id.logo_bar);
        ib_left=(ImageView) logoView.findViewById(R.id.ib_left);
        msg_title=(TextView)logoView.findViewById(R.id.msg_title);
    }

    //用于设置标题栏左边要显示的按钮
    public LogoActivity setIb_left(int resId){
        ib_left.setVisibility(resId>0?View.VISIBLE:View.GONE);
        ib_left.setImageResource(resId);
        return this;
    }

    //用于设置标题栏文字
    public LogoActivity setLogoText(String logoText){
        if (!TextUtils.isEmpty(logoText)){
            msg_title.setText(logoText);
        }
        return this;
    }

    //用于设置标题栏左边图片的单击事件
    public LogoActivity setIb_left(View.OnClickListener listener){
        if (ib_left.getVisibility()==View.VISIBLE){
            ib_left.setOnClickListener(listener);
        }
        return this;
    }

}
