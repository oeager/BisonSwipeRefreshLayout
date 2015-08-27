package com.bison.library.extras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.bison.library.ISwipeView;
import com.bison.library.R;

/**
 * Created by oeager on 2015/8/27 0027.
 * email: oeager@foxmail.com
 */
public class PulltoRefreshSwipeView implements ISwipeView {

    private View refreshView;
    private View loadView;
    private View refreshArrow;
    private View loadArrow;
    private View refreshProgress;
    private View loadProgress;
    private TextView refreshTips;
    private TextView loadTips;
    private boolean needRotate = true;

    public PulltoRefreshSwipeView(Context mContext) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        refreshView = inflater.inflate(R.layout.pull_to_refresh_layout, null);
        refreshArrow = refreshView.findViewById(R.id.arrow);
        refreshProgress = refreshView.findViewById(R.id.progressbar);
        refreshTips = (TextView) refreshView.findViewById(R.id.tips);

        loadView = inflater.inflate(R.layout.pull_to_load_layout, null);
        loadArrow = loadView.findViewById(R.id.arrow);
        loadProgress = loadView.findViewById(R.id.progressbar);
        loadTips = (TextView) loadView.findViewById(R.id.tips);

    }

    @Override
    public View getRefreshingView() {
        return refreshView;
    }

    @Override
    public View getLoadingView() {
        return loadView;
    }

    @Override
    public void onLayout(int originOffset, int currentOffset, int parentWidth, int parentHeight, int paddingTop, int paddingBottom, int targetHeight) {
        View refreshView = getRefreshingView();
        int refreshViewWidth = refreshView.getMeasuredWidth();
        int refreshViewHeight = refreshView.getMeasuredHeight();
        refreshView.layout((parentWidth / 2 - refreshViewWidth / 2), currentOffset - paddingTop - refreshViewHeight,
                (parentWidth / 2 + refreshViewWidth / 2), currentOffset - paddingTop);
        View loadView = getLoadingView();
        int loadWidth = refreshView.getMeasuredWidth();
        int loadHeight = refreshView.getMeasuredHeight();
        loadView.layout((parentWidth / 2 - loadWidth / 2), currentOffset + targetHeight + paddingBottom,
                (parentWidth / 2 + loadWidth / 2), currentOffset + targetHeight + loadHeight);
    }

    @Override
    public void offsetTopAndBottom(boolean refresh, int offset) {

    }

    @Override
    public void onSwipeChange(boolean refresh, float factor) {
        if (refresh) {

            refreshTips.setText(R.string.pull_to_refresh);
        } else {

            loadTips.setText(R.string.pull_to_load);
        }
    }

    @Override
    public void onSwipeStart(boolean refresh) {
        if (refresh) {
            if (refreshProgress.getVisibility() != View.GONE) {
                refreshProgress.setVisibility(View.GONE);
            }
            if (refreshArrow.getVisibility() != View.VISIBLE) {
                refreshArrow.setVisibility(View.VISIBLE);
            }
            refreshTips.setText(R.string.pull_to_refresh);
        } else {
            if (loadProgress.getVisibility() != View.GONE) {
                loadProgress.setVisibility(View.GONE);
            }
            if (loadArrow.getVisibility() != View.VISIBLE) {
                loadArrow.setVisibility(View.VISIBLE);
            }
            loadTips.setText(R.string.pull_to_load);
        }
    }

    @Override
    public void onSwipeMax(boolean refresh) {
        if (!needRotate) {
            return;
        }
        needRotate = false;
        View v = refresh ? refreshArrow : loadArrow;
        v.setRotation(0);
        rotateAnimation.reset();
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(rotateAnimation);
        if (refresh) {
            refreshTips.setText(R.string.release_to_refresh);
        } else {
            loadTips.setText(R.string.release_to_load);
        }
    }

    @Override
    public void onRefreshCancel(boolean refresh) {

    }

    @Override
    public void start(boolean refresh) {
        if (refresh) {
            refreshArrow.clearAnimation();
            refreshArrow.setVisibility(View.GONE);
            refreshProgress.setVisibility(View.VISIBLE);
            refreshTips.setText(R.string.refreshing);
        } else {
            loadArrow.clearAnimation();
            loadArrow.setVisibility(View.GONE);
            loadProgress.setVisibility(View.VISIBLE);
            loadTips.setText(R.string.loading);
        }
    }

    private final RotateAnimation rotateAnimation = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);


    @Override
    public void stop(boolean refresh) {
        needRotate = true;
        View v = refresh ? refreshArrow : loadArrow;
        v.clearAnimation();
    }
}
