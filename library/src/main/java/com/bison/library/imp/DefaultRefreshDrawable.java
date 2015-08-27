package com.bison.library.imp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;

import com.bison.library.Utils;

/**
 * Created by oeager on 2015/8/26 0026.
 * email: oeager@foxmail.com
 */
public class DefaultRefreshDrawable extends SwipeDrawable {

    private static final int MAX_LEVEL = 200;
    private static final int DEFAULT_BOUND_WIDTH = 65;
    private static final int DEFAULT_INSET = 15;
    private static final int DEFAULT_STROKE_SIZE = 2;
    private static final int MAX_ANGLE = 340;
    private static final int START_ANGLE = 270;
    private boolean isRunning;
    private RectF mBounds;
    private Paint mPaint;
    private Path mPath;
    private float mAngle;
    private int[] mColorSchemeColors = {Color.BLUE};
    private Handler mHandler = new Handler();
    private int mLevel;
    private float mDegrees;
    private final int mSize;
    private final int inset;

    public DefaultRefreshDrawable(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Utils.dp2Px(context, DEFAULT_STROKE_SIZE));
        mPath = new Path();
        mSize = Utils.dp2Px(context, DEFAULT_BOUND_WIDTH);
        inset = Utils.dp2Px(context, DEFAULT_INSET);
    }

    public void setColorSchemeColors(int[] colorSchemeColors) {
        mColorSchemeColors = colorSchemeColors;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mBounds = new RectF(left, top, right, bottom);
        mBounds.inset(inset, inset);
    }

    @Override
    public int getIntrinsicWidth() {
        return mSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return mSize;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(mDegrees, mBounds.centerX(), mBounds.centerY());
        mPath.reset();
        mPath.arcTo(mBounds, START_ANGLE, mAngle, true);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    @Override
    public void onSwipeChange(float factor) {
        if (factor <= 0) {
            mAngle = 0;
        } else {
            mAngle = MAX_ANGLE * factor;
            if(mColorSchemeColors.length>1){
                mPaint.setColor(evaluate(factor, mColorSchemeColors[0], mColorSchemeColors[1]));
            }else{
                mPaint.setColor(mColorSchemeColors[0]);
            }

        }
        invalidateSelf();
    }

    @Override
    public void onSwipeStart() {
        if (mAngle != 0) {
            mAngle = 0;
            invalidateSelf();
        }
    }

    @Override
    public void onSwipeMax() {
        if (mAngle != MAX_ANGLE) {
            mAngle = MAX_ANGLE;
            invalidateSelf();
        }
    }

    @Override
    public void onRefreshCancel() {

    }

    @Override
    public void start() {
        mLevel = 50;
        isRunning = true;
        mHandler.post(mAnimationTask);
    }

    @Override
    public void stop() {
        isRunning = false;
        mHandler.removeCallbacks(mAnimationTask);
        mDegrees = 0;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }


    private final Runnable mAnimationTask = new Runnable() {
        @Override
        public void run() {
            if (isRunning()) {
                mLevel++;
                if (mLevel > MAX_LEVEL)
                    mLevel = 0;
                updateLevel(mLevel);
                invalidateSelf();
                mHandler.postDelayed(this, 20);
            }
        }
    };

    private void updateLevel(int level) {
        int animationLevel = level == MAX_LEVEL ? 0 : level;

        int stateForLevel = (animationLevel / 50);

        float percent = level % 50 / 50f;
        int startColor = mColorSchemeColors[Math.min(mColorSchemeColors.length-1,stateForLevel)];
        int endColor = mColorSchemeColors[(stateForLevel + 1) % mColorSchemeColors.length];
        mPaint.setColor(evaluate(percent, startColor, endColor));

        mDegrees = 360 * percent;
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        int startInt = startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }



}
