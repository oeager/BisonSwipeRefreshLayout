package com.bison.library.imp;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by oeager on 2015/8/13 0013.
 * email: oeager@foxmail.com
 */
public abstract class SwipeDrawable extends Drawable implements SwipeDrawableController,Drawable.Callback {

    private Context mContext;
    public SwipeDrawable(Context mContext){
        this.mContext = mContext;
    }
    @Override
    public void invalidateDrawable(Drawable who) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }
    @Override
    public void setAlpha(int alpha) {

    }
    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    public Context getContext(){
        return mContext;
    }
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


}
