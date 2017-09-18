package me.temoa.spring.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.temoa.spring.R;

/**
 * Created by Lai
 * on 2017/8/24 22:37
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ListHolder> {

    private Context mContext;
    private List<String> mItems;
    private LayoutInflater mLayoutInflater;

    private OnItemClickListener mItemClickListener;
    private OnLoadMoreListener mLoadMoreListener;

    private boolean isOpenLoad = false;
    private boolean isLoading = false;

    public MainAdapter(Context context, List<String> items) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_mian, parent, false);
        return new ListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListHolder holder, int position) {
        if (mItems.get(position).contains("gif")) {
            holder.gifIv.setVisibility(View.VISIBLE);
        } else {
            holder.gifIv.setVisibility(View.GONE);
        }
        Glide.with(mContext)
                .load(mItems.get(position))
                .asBitmap()
                .dontAnimate()
                .into(holder.iv);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null)
                    mItemClickListener.onClick(
                            holder.iv,
                            mItems.get(holder.getAdapterPosition()),
                            holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (!isOpenLoad) return;
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isLoading && findLastVisibleItemPosition(layoutManager) + 1 == getItemCount()) {
                        if (mLoadMoreListener != null) {
                            mLoadMoreListener.onLoad();
                            isLoading = true;
                        }
                    }
                }
            }
        });
    }

    private int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager)
                    .findLastVisibleItemPositions(null);
            int max = lastVisibleItemPositions[0];
            for (int value : lastVisibleItemPositions) {
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
        return -1;
    }

    public void setNewData(List<String> data) {
        if (mItems == null) mItems = new ArrayList<>();
        mItems.clear();
        mItems.addAll(data);
        notifyItemRangeChanged(0, mItems.size());
    }

    public void addData(List<String> data) {
        int originalSize = mItems.size();
        mItems.addAll(data);
        notifyItemRangeInserted(originalSize, data.size());
    }

    public List<String> getAllData() {
        return mItems;
    }

    public void reset() {
        if (mItems == null || mItems.size() == 0) return;
        mItems.clear();
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public void openLoadMore() {
        isOpenLoad = true;
    }

    public void setLoadCompleted() {
        isLoading = false;
    }

    class ListHolder extends RecyclerView.ViewHolder {

        private ImageView iv;
        private ImageView gifIv;

        ListHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.item_main_iv);
            gifIv = itemView.findViewById(R.id.item_main_gif_iv);
        }
    }

    public interface OnItemClickListener {
        void onClick(View v, String url, int position);
    }

    public interface OnLoadMoreListener {
        void onLoad();
    }
}
