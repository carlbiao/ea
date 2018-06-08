package com.hadutech.glasses.engineerapp.events;

import android.graphics.Bitmap;

public class ScreenShotEvent {
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;
}
