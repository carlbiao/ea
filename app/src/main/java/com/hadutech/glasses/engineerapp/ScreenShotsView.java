package com.hadutech.glasses.engineerapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ScreenShotsView extends View {
    private static final String TAG = "ScreenShotsView";

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap = null;
    Paint paint;

    private float startX = 0;
    private float startY = 0;

    /**
     * 用来保存绘制的路径
     */
    ArrayList<PathModel> paths;
    private int color;
    private int width = 4;
    private Path path;

    public ScreenShotsView(Context context, AttributeSet attrs) {
        super(context,attrs);
        //初始化画笔
        color = getResources().getColor(R.color.draw_stroke_red);
        paint = new Paint();
        paint.setStrokeWidth(width);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paths = new ArrayList<>();
    }
    public ScreenShotsView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    public ScreenShotsView(Context context) {
        super(context);
    }

//    @Override
//    protected void onDraw(Canvas canvas){
//        Log.e(TAG,"onDraw");
//        if(bitmap != null){
//            canvas.drawBitmap(bitmap,0,0,null);
//            Log.e(TAG,"Draw bitmap");
//        }
//        //drawPaths(canvas);
//    }

    private void drawPaths(Canvas canvas){
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

    public void reset(){
        paths.clear();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(bitmap != null){
            canvas.drawBitmap(bitmap,0,0,null);
            Log.e(TAG,"Draw bitmap");
        }
        drawPaths(canvas);
    }

    /**
     * 获取base64格式的截图和标注信息
     * @return
     */
    public String getBase64ImageContent(){
        int w = this.getWidth();
        int h = this.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);

        /** 如果不设置canvas画布为白色，则生成透明 */

        //this.layout(0, 0, w, h);
        this.draw(c);

        //
        //convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();

        //base64 encode
        byte[] encode = Base64.encode(bytes,Base64.DEFAULT);
        String encodeString = new String(encode);
        return encodeString;
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
