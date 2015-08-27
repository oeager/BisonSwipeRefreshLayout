package com.bison.library;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;


/**
 * Created by oeager on 2015/8/20 0020.
 * email: oeager@foxmail.com
 */
public class SwipeRefreshLayout extends ViewGroup {
    //Constants
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .6f;

    private static final int REFRESH_TRIGGER_DISTANCE = 120;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final int INVALID_POINTER = -1;

    private static final float DRAG_RATE = .5f;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 400;

    private static final int ANIMATE_TO_START_DURATION = 500;


    //Variable

    private final DecelerateInterpolator mDecelerateInterpolator;

    private final View mTarget;

    private final ISwipeView mSwipeView;

    private SwipeRefreshListener mListener;

    private boolean mOriginalOffsetCalculated = false;

    private SwipeMode mSwipeMode;

    private boolean mReturningToStart;

    private int mCurrentTargetOffsetTop;

    protected int mOriginalOffsetTop;

    private boolean mRefreshing = false;

    private boolean mIsBeingDragged;

    private int mActivePointerId = INVALID_POINTER;

    private float mInitialDownY;

    private float mInitialMotionY;

    private int mTouchSlop;

    private float mTotalDragDistance = -1;

    private float mSpinnerFinalOffset = -1;

    private boolean mNotify;

    protected int mFrom;

    private boolean mBothDirection;

    public SwipeRefreshLayout(View mTarget, ISwipeView swipeView, SwipeMode swipeMode) {
        super(mTarget.getContext());
        if (swipeView == null || swipeView.getRefreshingView() == null || swipeView.getLoadingView() == null) {
            throw new IllegalArgumentException("swipeView can not be null and must have both refreshView and loadView");
        }
        this.mTarget = mTarget;
        this.mSwipeView = swipeView;
        if(swipeMode==SwipeMode.BOTH){
            mSwipeMode = SwipeMode.TOP;
            mBothDirection =true;
        }else{
            mSwipeMode = swipeMode;
            mBothDirection = false;
        }
        mTouchSlop = ViewConfiguration.get(mTarget.getContext()).getScaledTouchSlop();
        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        addView(swipeView.getRefreshingView());
        addView(swipeView.getLoadingView());
        addView(mTarget);
    }

    public static SwipeRefreshLayout attach(View target, ISwipeView mSwipeView,SwipeMode mode) {
        if (target == null) {
            throw new IllegalArgumentException("target can not be null");
        }
        LayoutParams params = target.getLayoutParams();
        ViewParent parent = target.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("the target must hava a parent before attach");
        }
        ViewGroup targetParent = (ViewGroup) parent;
        int index = targetParent.indexOfChild(target);
        targetParent.removeView(target);
        SwipeRefreshLayout refreshLayout = new SwipeRefreshLayout(target, mSwipeView,mode);
        targetParent.addView(refreshLayout, index, params);
        return refreshLayout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkTotalDistance();
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        measureChild(mSwipeView.getRefreshingView(), widthMeasureSpec, heightMeasureSpec);
        measureChild(mSwipeView.getLoadingView(), widthMeasureSpec, heightMeasureSpec);
        checkSpinnerFinalOffset();
        if (!mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;
            mCurrentTargetOffsetTop = mOriginalOffsetTop = mTarget.getTop();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childBottom = getPaddingBottom();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - childTop - childBottom;
        mTarget.layout(childLeft, mCurrentTargetOffsetTop, childLeft + childWidth, mCurrentTargetOffsetTop + childHeight);
        mSwipeView.onLayout(mOriginalOffsetTop, mCurrentTargetOffsetTop, width, height, childTop,childBottom,childHeight);


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (mReturningToStart) {
            return false;
        }
        if (!isEnabled()) {
            return false;
        }
        if (mRefreshing) {
            return false;
        }
        switch (mSwipeMode) {

            case BOTTOM:
                if (!mBothDirection && canChildScrollDown()) {
                    return false;
                }
                break;
            case TOP:
            default:
                if (!mBothDirection && canChildScrollUp()) {
                    return false;
                }
                break;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                if (mBothDirection) {
                    if (y > mInitialDownY) {
                        setRawDirection(SwipeMode.TOP);
                    } else if (y < mInitialDownY) {
                        setRawDirection(SwipeMode.BOTTOM);
                    }
                    if ((mSwipeMode == SwipeMode.BOTTOM && canChildScrollDown())
                            || (mSwipeMode == SwipeMode.TOP && canChildScrollUp())) {
                        mInitialDownY = y;
                        return false;
                    }
                }
                float yDiff;
                switch (mSwipeMode) {
                    case BOTTOM:
                        yDiff = mInitialDownY - y;
                        break;
                    case TOP:
                    default:
                        yDiff = y - mInitialDownY;
                        break;
                }
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mInitialMotionY=mInitialDownY+(isLoad()?-mTouchSlop:mTouchSlop);
                    mIsBeingDragged = true;
                   mSwipeView.onSwipeStart(!isLoad());
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (mReturningToStart) {
            return false;
        }
        if (!isEnabled()) {
            return false;
        }
        if (mRefreshing) {
            return false;
        }
        if(isLoad()?canChildScrollDown():canChildScrollUp()){
            return false;
        }
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                float overScroll = Math.abs((mInitialMotionY - y) * DRAG_RATE);
                if (mIsBeingDragged) {
                    float originalDragPercent = overScroll / mTotalDragDistance;
                    if (originalDragPercent < 0) {
                        return false;
                    }
                    float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                    float extraOS = Math.abs(overScroll) - mTotalDragDistance;
                    float tensionSlingshotPercent = Math.max(0,
                            Math.min(extraOS, mSpinnerFinalOffset * 2) / mSpinnerFinalOffset);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                            (tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (mSpinnerFinalOffset) * tensionPercent * 2;
                    int targetY;
                    if (mSwipeMode == SwipeMode.TOP) {
                        targetY = mOriginalOffsetTop + (int) ((mSpinnerFinalOffset * dragPercent) + extraMove);
                    } else {
                        targetY = mOriginalOffsetTop - (int) ((mSpinnerFinalOffset * dragPercent) + extraMove);
                    }
                    View refreshView = isLoad()?mSwipeView.getLoadingView():mSwipeView.getRefreshingView();
                    if (refreshView.getVisibility() != View.VISIBLE) {
                        refreshView.setVisibility(View.VISIBLE);
                    }


                    if (overScroll < mTotalDragDistance) {
                        mSwipeView.onSwipeChange(!isLoad(), dragPercent);
                    } else {
                        mSwipeView.onSwipeMax(!isLoad());
                    }
                    setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop,
                            true /* requires update */);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                float overScroll = Math.abs((y - mInitialMotionY) * DRAG_RATE);
                mIsBeingDragged = false;
                if (overScroll > mTotalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    // cancel refresh
                    mRefreshing = false;
                    mSwipeView.onRefreshCancel(!isLoad());
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop, null);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //no thing
    }

    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                try {
                    if (absListView.getCount() > 0) {
                        if (absListView.getLastVisiblePosition() + 1 == absListView.getCount()) {
                            int lastIndex = absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition();
                            return absListView.getChildAt(lastIndex).getBottom() == absListView.getPaddingBottom();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return true;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mTarget.bringToFront();
        mSwipeView.offsetTopAndBottom(!isLoad(),offset);
        mTarget.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mTarget.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }


    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    boolean isLoad() {
        return mSwipeMode == SwipeMode.BOTTOM;
    }


    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                animateOffsetToStartPosition(mCurrentTargetOffsetTop, mRefreshListener);
            }
        }
    }

    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimateToCorrectPosition.setAnimationListener(listener);
        }
        View refreshView = isLoad()?mSwipeView.getLoadingView():mSwipeView.getRefreshingView();
        if (refreshView != null) {
            refreshView.clearAnimation();
            refreshView.startAnimation(mAnimateToCorrectPosition);
        }
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimateToStartPosition.setAnimationListener(listener);
        }
        View refreshView = isLoad()?mSwipeView.getLoadingView():mSwipeView.getRefreshingView();
        if (refreshView != null) {
            refreshView.clearAnimation();
            refreshView.startAnimation(mAnimateToStartPosition);
        }
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            switch (mSwipeMode) {
                case BOTTOM:
                    endTarget = (int)(mOriginalOffsetTop-mSpinnerFinalOffset);
                    break;
                case TOP:
                default:
                    endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                    break;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop -mTarget.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);

        }
    };

    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            boolean isLoad = isLoad();
            if (mRefreshing) {
                // Make sure the progress view is fully visible
                mSwipeView.onSwipeMax(!isLoad);
                mSwipeView.start(!isLoad);
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh(!isLoad);
                    }
                }
            } else {
                mSwipeView.stop(!isLoad);
                View view = isLoad?mSwipeView.getLoadingView():mSwipeView.getRefreshingView();
                view.setVisibility(View.GONE);
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop,
                        true /* requires update */);

            }
            mCurrentTargetOffsetTop = mTarget.getTop();
        }
    };
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mTarget.getTop();
        /**
         *  mTarget.bringToFront();
         mSwipeView.offsetTopAndBottom(!isLoad(),offset);
         mTarget.offsetTopAndBottom(offset);
         mCurrentTargetOffsetTop = mTarget.getTop();
         if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
         invalidate();
         }
         */
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    // only TOP or Bottom
    private void setRawDirection(SwipeMode direction) {
        if (mSwipeMode == direction) {
            return;
        }
        mSwipeMode = direction;
    }

    void checkTotalDistance() {
        if (mTotalDragDistance == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTotalDragDistance = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    void checkSpinnerFinalOffset() {
        if (mSpinnerFinalOffset == -1) {
            int offset = mSwipeView.getRefreshingView().getMeasuredHeight();
            int shotOffset = mSwipeView.getLoadingView().getMeasuredHeight();
            mSpinnerFinalOffset = Math.max(shotOffset, offset);
        }
    }


    public void setOnSwipeRefreshListener(SwipeRefreshListener listener) {
        this.mListener = listener;
    }

    public interface SwipeRefreshListener {
        void onRefresh(boolean isTopRefresh);
    }
}
