package me.temoa.spring.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import me.temoa.spring.R;

/**
 * Created by Temoa
 * on 2017/12/2.
 */

public class DragFrameLayout extends FrameLayout {

    private ViewDragHelper mDragHelper;

    private int finalLeft;
    private int finalTop;

    public DragFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ((Activity) getContext())
                .getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.background_night));
        mDragHelper = ViewDragHelper.create(this, 1.F, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return child instanceof ImageView;
            }

            boolean mNeedDrag;
            boolean mNeedRelease;

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                if (mNeedDrag) {
                    return top;
                }
                if (top < 0) {
                    top = 0;
                } else if (top > 100) {
                    mNeedDrag = true;
                }
                return top;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                return mNeedDrag ? left : 0;
            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                mNeedRelease = top > getHeight() * 0.25;

                float present = 1 - (top * 2.F) / getHeight();
                if (getContext() instanceof Activity) {
                    int alpha = Math.min((int) (255 * present), 255);
                    ((Activity) getContext()).getWindow().getDecorView()
                            .setBackgroundColor(Color.argb(alpha, 63, 63, 63));
                }

                float maxScale = Math.min(present, 1.F);
                float minScale = Math.max(0.5F, maxScale);

                changedView.setScaleX(minScale);
                changedView.setScaleY(minScale);
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (mNeedRelease) {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).onBackPressed();
                    }
                } else {
                    mNeedDrag = false;
                    mDragHelper.settleCapturedViewAt(finalLeft, finalTop);
                    releasedChild.setScaleX(1.F);
                    releasedChild.setScaleY(1.F);
                }
                invalidate();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getHeight() / 2;
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View v = null;
        for (int i = 0; i < getChildCount(); i++) {
            v = getChildAt(i);
        }
        if (v != null && v instanceof ImageView) {
            finalLeft = v.getLeft();
            finalTop = v.getTop();
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }
}
