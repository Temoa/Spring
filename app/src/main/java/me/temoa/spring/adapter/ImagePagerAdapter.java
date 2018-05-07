package me.temoa.spring.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import me.temoa.spring.widget.GlideImageView;
import me.temoa.spring.R;

/**
 * Created by Temoa
 * on 2017/11/19.
 */

public class ImagePagerAdapter extends PagerAdapter {

    private final LayoutInflater mLayoutInflater;
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
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        FrameLayout view = (FrameLayout) mLayoutInflater.inflate(R.layout.item_view_pager, null);
        final GlideImageView iv = view.findViewById(R.id.image_item_photoView);
        iv.load(mItems.get(position));
        if (mItemClickListener != null) {
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick();
                }
            });
        }
        if (mItemChildClickListener != null) {
            iv.setOnLongClickListener(new View.OnLongClickListener() {
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
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public interface OnItemClickListener {
        void onItemClick();
    }

    public interface OnItemChildClickListener {
        boolean onItemChildClick(View v);
    }
}
