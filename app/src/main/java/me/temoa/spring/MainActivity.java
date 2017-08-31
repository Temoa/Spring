package me.temoa.spring;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.temoa.spring.adapter.MainAdapter;
import me.temoa.spring.bean.Gank;
import me.temoa.spring.network.GankRetrofitClient;
import me.temoa.spring.network.RxCallback;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mContainer;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;

    private int page = 1;
    private boolean isNight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.DayTheme);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mContainer = (RelativeLayout) findViewById(R.id.main_container);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MainAdapter(this, null);
        mAdapter.setItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, String url, int position) {
                Log.d("MainActivity", "onClick() called with: v = [" + v + "], url = [" + url + "], position = [" + position + "]");
            }
        });
        mAdapter.openLoadMore();
        mAdapter.setLoadMoreListener(new MainAdapter.OnLoadMoreListener() {
            @Override
            public void onLoad() {
                getData(true);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        getData(false);
    }

    private void getData(final boolean isLoadMore) {
        RxCallback<Gank> callback = new RxCallback<Gank>() {
            @Override
            public void onSuccess(Gank gank) {
                if (gank.isError()) return;
                List<String> urls = new ArrayList<>();
                for (Gank.Results results : gank.getResults()) {
                    urls.add(results.getUrl());
                }
                if (isLoadMore) {
                    mAdapter.addData(urls);
                } else {
                    mAdapter.setNewData(urls);
                }
                page++;
            }

            @Override
            public void onFinished() {

            }
        };
        GankRetrofitClient.getInstance().get(page, callback);
    }

    private void toggleThemeSetting() {
        if (isNight) {
            isNight = false;
            setTheme(R.style.DayTheme);
        } else {
            isNight = true;
            setTheme(R.style.NightTheme);
        }
    }

    private void changeTheme() {
        showAnimation();
        toggleThemeSetting();
        refreshUi();
    }

    private void refreshUi() {
        TypedValue background = new TypedValue();
        TypedValue textColor = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.cBackground, background, true);
        theme.resolveAttribute(R.attr.cTextColor, textColor, true);

        mContainer.setBackgroundResource(background.resourceId);

        int childCount = mRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LinearLayout childView = (LinearLayout) mRecyclerView.getChildAt(i);
            childView.setBackgroundResource(background.resourceId);
        }

        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(mRecyclerView));
            RecyclerView.RecycledViewPool recycledViewPool = mRecyclerView.getRecycledViewPool();
            recycledViewPool.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshStatusBar();
        refreshActionBar();
    }

    private void refreshStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
            getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId));
        }
    }

    private void refreshActionBar() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(typedValue.resourceId)));
        }
    }

    private void showAnimation() {
        final View decorView = getWindow().getDecorView();
        Bitmap cacheBitmap = getCacheBitmapFromView(decorView);
        if (decorView instanceof ViewGroup && cacheBitmap != null) {
            final View view = new View(this);
            view.setBackground(new BitmapDrawable(getResources(), cacheBitmap));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) decorView).addView(view, params);

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0F, 0F);
            objectAnimator.setDuration(500);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup) decorView).removeView(view);
                }
            });
            objectAnimator.start();
        }
    }

    private Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnable = true;
        view.setDrawingCacheEnabled(drawingCacheEnable);
        view.buildDrawingCache(drawingCacheEnable);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            drawingCache.recycle();
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_night) {
            changeTheme();
        }
        return super.onOptionsItemSelected(item);
    }
}
