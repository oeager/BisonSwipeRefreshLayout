package com.bison.library;

import android.view.View;

/**
 * Created by oeager on 2015/8/27 0027.
 * email: oeager@foxmail.com
 */
public interface ISwipeView {

    View getRefreshingView();

    View getLoadingView();

    void onLayout(int originOffset,int currentOffset,int parentWidth,int parentHeight,int paddingTop,int paddingBottom,int targetHeight);

    void offsetTopAndBottom(boolean refresh,int offset);

    void onSwipeChange(boolean refresh,float factor);

    void onSwipeStart(boolean refresh);

    void onSwipeMax(boolean refresh);

    void onSwipeBeyond(boolean refresh,float factor);

    void onRefreshCancel(boolean refresh);

    void start(boolean refresh);

    void stop(boolean refresh);

}
