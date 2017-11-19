package me.temoa.spring.adapter;

import android.annotation.SuppressLint;
import android.support.v4.view.PagerAdapter;
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

    private List<String> items;

    public void setData(List<String> data) {
        this.items = data;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    @SuppressLint("InflateParams")
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout view = (FrameLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.item_view_pager, null);
        final PhotoView photoView = view.findViewById(R.id.image_item_photoView);
        final ProgressBar progressBar = view.findViewById(R.id.image_item_progressBar);
        Glide.with(container.getContext())
                .load(items.get(position))
                .thumbnail(0.1F)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(photoView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        progressBar.setVisibility(View.GONE);
                    }
                });
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
