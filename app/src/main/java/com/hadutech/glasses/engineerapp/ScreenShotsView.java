package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ScreenShotsView extends View {
    private static final String TAG = "ScreenShotsView";

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap = null;

    public ScreenShotsView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }
    public ScreenShotsView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    public ScreenShotsView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas){
        Log.e(TAG,"onDraw");
        if(bitmap != null){
            canvas.drawBitmap(bitmap,0,0,null);
            Log.e(TAG,"Draw bitmap");
        }
    }


}
