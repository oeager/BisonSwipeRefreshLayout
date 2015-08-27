package com.bison.library.imp;

import android.graphics.drawable.Animatable;

/**
 * Created by oeager on 2015/8/13 0013.
 * email: oeager@foxmail.com
 */
public interface SwipeDrawableController extends Animatable {
    void onSwipeChange(float factor);

    void onSwipeStart();

    void onSwipeMax();

    void onRefreshCancel();

    void start();

    void stop();
}
