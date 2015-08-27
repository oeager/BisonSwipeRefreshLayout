package com.bison.library;

import android.view.View;

import com.bison.library.imp.SwipeDrawable;

/**
 * Created by oeager on 2015/8/27 0027.
 * email: oeager@foxmail.com
 */
public abstract class BaseSwipeView implements ISwipeView{

    private final SwipeDrawable refreshDrawable;

    private final SwipeDrawable loadDrawable;

    public BaseSwipeView (SwipeDrawable refreshDrawable,SwipeDrawable loadDrawable){
        this.refreshDrawable = refreshDrawable;
        this.loadDrawable = loadDrawable;
    }


    @Override
    public void onLayout(int originOffset, int currentOffset, int parentWidth, int parentHeight,int paddingTop,int paddingBottom,int targetHeight) {
        View refreshView = getRefreshingView();
        int refreshViewWidth = refreshView.getMeasuredWidth();
        int refreshViewHeight = refreshView.getMeasuredHeight();
        refreshView.layout((parentWidth / 2 - refreshViewWidth / 2), currentOffset-paddingTop - refreshViewHeight,
                (parentWidth / 2 + refreshViewWidth / 2), currentOffset-paddingTop);
        View loadView = getLoadingView();
        int loadWidth = refreshView.getMeasuredWidth();
        int loadHeight = refreshView.getMeasuredHeight();
        loadView.layout((parentWidth / 2 - loadWidth / 2), currentOffset + targetHeight + paddingBottom,
                (parentWidth / 2 + loadWidth / 2), currentOffset + targetHeight + loadHeight);
    }

    @Override
    public void offsetTopAndBottom(boolean refresh, int offset) {
        if(refresh){
            getRefreshingView().offsetTopAndBottom(offset);
        }else{
            getLoadingView().offsetTopAndBottom(offset);
        }
    }

    @Override
    public void onSwipeBeyond(boolean refresh, float factor) {
        
    }

    @Override
    public void onSwipeChange(boolean refresh, float factor) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.onSwipeChange(factor);
    }

    @Override
    public void onSwipeStart(boolean refresh) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.onSwipeStart();
    }

    @Override
    public void onSwipeMax(boolean refresh) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.onSwipeMax();
    }

    @Override
    public void onRefreshCancel(boolean refresh) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.onRefreshCancel();
    }

    @Override
    public void start(boolean refresh) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.start();
    }

    @Override
    public void stop(boolean refresh) {
        SwipeDrawable drawable = refresh?refreshDrawable:loadDrawable;
        drawable.stop();
    }
}
