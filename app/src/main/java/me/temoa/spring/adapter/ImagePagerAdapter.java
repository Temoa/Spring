package me.temoa.spring.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import me.temoa.spring.R;

/**
 * Created by Temoa
 * on 2017/11/19.
 */

public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<String> mItems;
    private OnItemClickListener mItemClickListener;
    private OnItemChildClickListener mItemChildClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setItemChildClickListener(OnItemChildClickListener itemChildClickListener) {
        mItemChildClickListener = itemChildClickListener;
    }

    public void setData(List<String> data) {
        this.mItems = data;
    }

    public ImagePagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    @SuppressLint("InflateParams")
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout view = (FrameLayout) mLayoutInflater.inflate(R.layout.item_view_pager, null);
        final PhotoView photoView = view.findViewById(R.id.image_item_photoView);
        ViewCompat.setTransitionName(photoView, mItems.get(position));
        final ProgressBar progressBar = view.findViewById(R.id.image_item_progressBar);
        Glide.with(mContext)
                .load(mItems.get(position))
                .thumbnail(0.1F)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(photoView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        progressBar.setVisibility(View.GONE);
                    }
                });
        if (mItemClickListener != null) {
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick();
                }
            });
        }
        if (mItemChildClickListener != null) {
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mItemChildClickListener.onItemChildClick(v);
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public interface OnItemClickListener {
        void onItemClick();
    }

    public interface OnItemChildClickListener {
        boolean onItemChildClick(View v);
    }
}
