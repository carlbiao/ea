package com.hadutech.glasses.engineerapp.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import com.hadutech.glasses.engineerapp.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DrawView extends View {
    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //初始化画笔
        color = getResources().getColor(R.color.draw_stroke_red);
        paint = new Paint();
        paint.setStrokeWidth(width);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paths = new ArrayList<>();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Paint paint;

    float startX = 0;
    float startY = 0;

    /**
     * 用来保存绘制的路径
     */
    ArrayList<PathModel> paths;
    private int color;
    private int width = 4;
    private Path path;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 循环绘制集合里面的路径
         */
        for (int i = 0; i < paths.size(); i++) {
            PathModel p = paths.get(i);
            //每次绘制路径都需要设置画笔的宽度，颜色
            paint.setStrokeWidth(p.getWidth());
            paint.setColor(p.getColor());
            canvas.drawPath(p.getPath(), paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下的时候开始记录绘制路径
                path = new Path();
                PathModel p = new PathModel();
                p.setPath(path);//保存当前路径
                p.setColor(paint.getColor());//保存路径的颜色
                p.setWidth((int) paint.getStrokeWidth()); //保存路径的大小
                paths.add(p);

                startX = event.getX();
                startY = event.getY();
                path.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        //刷新绘制画布
        invalidate();

        return true;
    }

    class PathModel{
        float width;
        int color;
        Path path;

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }
    }



}
