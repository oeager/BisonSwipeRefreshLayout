package com.bison.library.imp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bison.library.BaseSwipeView;

/**
 * Created by oeager on 2015/8/27 0027.
 * email: oeager@foxmail.com
 */
public class DefaultSwipeView extends BaseSwipeView {
    private ImageView refreshView;
    private ImageView loadView;

    public DefaultSwipeView(SwipeDrawable refreshDrawable, SwipeDrawable loadDrawable) {
        super(refreshDrawable, loadDrawable);
        Context context = refreshDrawable.getContext();
        refreshView = new ImageView(context);
        refreshView.setImageDrawable(refreshDrawable);

        loadView = new ImageView(context);
        loadView.setImageDrawable(loadDrawable);
    }

    @Override
    public void onLayout(int originOffset, int currentOffset, int parentWidth, int parentHeight, int paddingTop, int paddingBottom, int targetHeight) {
        View refreshView = getRefreshingView();
        int refreshViewWidth = refreshView.getMeasuredWidth();
        int refreshViewHeight = refreshView.getMeasuredHeight();
        refreshView.layout((parentWidth / 2 - refreshViewWidth / 2),paddingTop,
                (parentWidth / 2 + refreshViewWidth / 2), paddingTop+refreshViewHeight);
        View loadView = getLoadingView();
        int loadWidth = refreshView.getMeasuredWidth();
        int loadHeight = refreshView.getMeasuredHeight();
        loadView.layout((parentWidth / 2 - loadWidth / 2), parentHeight-loadHeight,
                (parentWidth / 2 + loadWidth / 2),parentHeight);
    }

    @Override
    public void offsetTopAndBottom(boolean refresh, int offset) {

    }

    @Override
    public View getRefreshingView() {
        return refreshView;
    }

    @Override
    public View getLoadingView() {
        return loadView;
    }
}
