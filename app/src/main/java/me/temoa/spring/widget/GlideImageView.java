package me.temoa.spring.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import me.temoa.spring.R;
import me.temoa.spring.network.image.GlideApp;
import me.temoa.spring.network.image.GlideProgressManager;
import me.temoa.spring.network.image.OnProgressListener;

/**
 * Created by Temoa
 * on 2018/5/7.
 */

public class GlideImageView extends PhotoView {

    private int mCenterWidth, mCenterHeight;
    private int mArcRadius;
    private int mColor = -1;

    private Paint mProgressCirclePaint;
    private Paint mProgressArcPaint;
    private RectF mProgressRectF;
    private ValueAnimator mProgressAnimator;

    private String mUrl;
    private int mLastPercent = -1, mCurPercent;
    private int mProgress;
    private boolean isStartLoad;
    private boolean isFinish;

    public GlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = null;
            try {
                ta = getContext().obtainStyledAttributes(attrs, R.styleable.GlideImageView);
                mColor = ta.getColor(
                        R.styleable.GlideImageView_glide_image_view_color,
                        Color.parseColor("#CFD8DC")
                );
            } finally {
                if (ta != null) ta.recycle();
            }
        }

        mProgressCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressCirclePaint.setColor(mColor);
        mProgressCirclePaint.setStyle(Paint.Style.STROKE);
        mProgressCirclePaint.setStrokeWidth(3.F);

        mProgressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressArcPaint.setColor(mColor);
        mProgressArcPaint.setStyle(Paint.Style.FILL);
        mProgressArcPaint.setAlpha(128);
    }

    public void load(@NonNull String url) {
        mUrl = url;
        isStartLoad = true;
        GlideProgressManager.addListener(mProgressListener);
        GlideApp
                .with(getContext())
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(this);
    }

    private OnProgressListener mProgressListener = new OnProgressListener() {
        @Override
        public void progress(String url, int percent, boolean finish) {
            if (url.equals(mUrl)) {
                mCurPercent = percent;
                isFinish = finish;
                progressAnimation();
                if (finish) GlideProgressManager.removeListener(this);
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterHeight = h / 2;
        mCenterWidth = w / 2;

        mArcRadius = Math.max(w / 12, 48);
        if (mProgressRectF == null) {
            mProgressRectF = new RectF(-mArcRadius, -mArcRadius, mArcRadius, mArcRadius);
        } else {
            mProgressRectF.left = -mArcRadius;
            mProgressRectF.top = -mArcRadius;
            mProgressRectF.right = mArcRadius;
            mProgressRectF.bottom = mArcRadius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isFinish && isStartLoad) {
            canvas.save();
            canvas.translate(mCenterWidth, mCenterHeight);
            canvas.drawCircle(0, 0, mArcRadius, mProgressCirclePaint);
            canvas.drawArc(mProgressRectF, -90, mProgress, true, mProgressArcPaint);
            canvas.restore();
        }
    }

    private void progressAnimation() {
        if (mCurPercent == mLastPercent) return;
        if (mProgressAnimator == null) {
            mProgressAnimator = ValueAnimator.ofInt(mLastPercent, mCurPercent);
            mProgressAnimator.setInterpolator(new LinearInterpolator());
            mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mProgress = (int) ((value * 360.0F) / 100.0F);
                    invalidate();
                }
            });
        } else {
            if (mProgressAnimator.isRunning()) {
                mProgressAnimator.pause();
            }
            mProgressAnimator.setIntValues(mLastPercent, mCurPercent);
        }
        mProgressAnimator.start();
        mLastPercent = mCurPercent;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putInt("last_percent", mLastPercent);
        bundle.putInt("cur_percent", mCurPercent);
        bundle.putInt("progress_value", mProgress);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mLastPercent = bundle.getInt("last_percent");
            mCurPercent = bundle.getInt("cur_percent");
            mProgress = bundle.getInt("progress_value");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }
        GlideProgressManager.removeListener(mProgressListener);
        mProgressListener = null;
    }
}