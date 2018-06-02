package com.hadutech.glasses.engineerapp;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBuilder {

    private View titleView;
    private LinearLayout titleBar;
    private TextView tv_title;
    private ImageView iv_right;

    /**
     * 构造方法：用于获取对象
     */
    public TitleBuilder(Activity context){
        titleView=context.findViewById(R.id.title_bar);
        tv_title=(TextView)titleView.findViewById(R.id.tv_title);
        titleBar=(LinearLayout) titleView.findViewById(R.id.title_bar);
        iv_right=(ImageView)titleView.findViewById(R.id.iv_right);

    }

    /**
     * 用于设置标题栏文字
     * */
    public TitleBuilder setTitleText(String titleText){
        if (!TextUtils.isEmpty(titleText)){
            tv_title.setText(titleText);
        }
        return this;
    }

    /**
     * 用于设置标题栏右边要显示的图片
     */
    public TitleBuilder setIv_right(int resId){
        iv_right.setVisibility(resId>0?View.VISIBLE:View.GONE);
        iv_right.setImageResource(resId);
        return this;
    }
}
