package me.temoa.spring;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.temoa.spring.adapter.MainAdapter;
import me.temoa.spring.bean.Gank;
import me.temoa.spring.bean.Jiandan;
import me.temoa.spring.network.GankRetrofitClient;
import me.temoa.spring.network.JiandanRetrofitClient;
import me.temoa.spring.network.RxCallback;
import me.temoa.spring.util.GateUtil;
import me.temoa.spring.util.ThemeUtil;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private LinearLayout mContainer;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;

    private int page = 1;
    private Disposable dataDisposable;

    private boolean isNewWorld;
    private boolean isNight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isNewWorld = GateUtil.getCurGate(this);
        isNight = ThemeUtil.getCurTheme(this);

        if (isNight) setTheme(R.style.NightTheme);
        else setTheme(R.style.DayTheme);

        setContentView(R.layout.activity_main);

        initViews();

        if (savedInstanceState == null) {
            animateToolbar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataDisposable != null && !dataDisposable.isDisposed()) {
            dataDisposable.dispose();
            dataDisposable = null;
        }
    }

    private void initViews() {
        mToolbar = findViewById(R.id.main_toolBar);
        setSupportActionBar(mToolbar);
        View titleTv = mToolbar.getChildAt(0);
        if (titleTv instanceof TextView) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/IndieFlower.ttf");
            TextView t = (TextView) titleTv;
            t.setTextSize(24.f);
            t.setTypeface(typeface);
        }
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecyclerView != null) {
                    GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                    int curPosition = layoutManager.findFirstVisibleItemPosition();
                    if (curPosition > 3) mRecyclerView.smoothScrollToPosition(0);
                }
            }
        });

        mContainer = findViewById(R.id.main_container);

        mRecyclerView = findViewById(R.id.main_recyclerView);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MainAdapter(this, null);
        mAdapter.setItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, String url, int position) {
                ImageActivity.launch(MainActivity.this, url,
                        (ArrayList<String>) mAdapter.getAllData(), position);
            }
        });
        mAdapter.openLoadMore();
        mAdapter.setLoadMoreListener(new MainAdapter.OnLoadMoreListener() {
            @Override
            public void onLoad() {
                if (isNewWorld) getJiandanData(true);
                else getGankData(true);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        if (isNewWorld) getJiandanData(false);
        else getGankData(false);
    }

    private void getGankData(final boolean isLoadMore) {
        RxCallback<Gank> callback = new RxCallback<Gank>() {
            @Override
            public void onSuccess(Gank gank) {
                if (gank.isError()) return;
                List<String> urls = new ArrayList<>();
                for (Gank.Results results : gank.getResults()) {
                    urls.add(results.getUrl());
                }
                if (isLoadMore) {
                    mAdapter.setLoadCompleted();
                    mAdapter.addData(urls);
                } else {
                    mAdapter.setNewData(urls);
                    openAnimation();
                }
                page++;
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void getDisposable(Disposable d) {
                dataDisposable = d;
            }
        };
        GankRetrofitClient.getInstance().get(page, callback);
    }

    private void getJiandanData(final boolean isLoadMore) {
        RxCallback<Jiandan> callback = new RxCallback<Jiandan>() {
            @Override
            public void onSuccess(Jiandan jiandan) {
                String status = jiandan.getStatus();
                if (TextUtils.isEmpty(status) || !TextUtils.equals(status, "ok")) {
                    return;
                }
                List<String> urls = new ArrayList<>();
                for (Jiandan.Comments comments : jiandan.getComments()) {
                    urls.addAll(comments.getPics());
                }
                if (isLoadMore) {
                    mAdapter.setLoadCompleted();
                    mAdapter.addData(urls);
                } else {
                    mAdapter.setNewData(urls);
                    openAnimation();
                }
                page++;
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void getDisposable(Disposable d) {
                dataDisposable = d;
            }
        };
        JiandanRetrofitClient.getInstance().get(page, callback);
    }

    private void animateToolbar() {
        View t = mToolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            title.setAlpha(0f);
            title.setScaleX(0.8f);
            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(500)
                    .setDuration(900)
                    .setInterpolator(new FastOutSlowInInterpolator());
        }
    }

    private void openAnimation() {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRecyclerView,
                mRecyclerView.getWidth() / 2,
                mRecyclerView.getHeight() / 2,
                0,
                mRecyclerView.getHeight());
        animator.setDuration(1000);
        mRecyclerView.setVisibility(View.VISIBLE);
        animator.start();
    }

    private void closeAnimation() {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRecyclerView,
                mRecyclerView.getWidth(),
                0,
                mRecyclerView.getHeight(),
                0);
        animator.setDuration(700);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRecyclerView.setVisibility(View.INVISIBLE);

                page = 1;
                if (isNewWorld) {
                    isNewWorld = false;
                    mAdapter.reset();
                    getGankData(false);
                    GateUtil.setGate(MainActivity.this, isNewWorld);
                } else {
                    isNewWorld = true;
                    mAdapter.reset();
                    getJiandanData(false);
                    GateUtil.setGate(MainActivity.this, isNewWorld);
                }

                supportInvalidateOptionsMenu();
            }
        });
        animator.start();
    }

    private void toggleThemeSetting() {
        showAnimation();
        if (isNight) {
            isNight = false;
            setTheme(R.style.DayTheme);
            ThemeUtil.setTheme(this, isNight);
        } else {
            isNight = true;
            setTheme(R.style.NightTheme);
            ThemeUtil.setTheme(this, isNight);
        }
        refreshUi();
        supportInvalidateOptionsMenu();
    }

    private void refreshUi() {
        TypedValue background = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.cBackground, background, true);

        mContainer.setBackgroundResource(background.resourceId);

//        int childCount = mRecyclerView.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            FrameLayout childView = (FrameLayout) mRecyclerView.getChildAt(i);
//            childView.setBackgroundResource(background.resourceId);
//        }
//        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
//        try {
//            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
//            declaredField.setAccessible(true);
//            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
//            declaredMethod.setAccessible(true);
//            declaredMethod.invoke(declaredField.get(mRecyclerView));
//            RecyclerView.RecycledViewPool recycledViewPool = mRecyclerView.getRecycledViewPool();
//            recycledViewPool.clear();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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
        if (isNight) {
            menu.findItem(R.id.action_night).setTitle("日间模式");
        } else {
            menu.findItem(R.id.action_night).setTitle("夜间模式");
        }

        if (isNewWorld) {
            menu.findItem(R.id.action_gate).setTitle("关上一扇窗");
        } else {
            menu.findItem(R.id.action_gate).setTitle("打开一扇门");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_night) {
            toggleThemeSetting();
            if (isNight) {
                item.setTitle("日间模式");
            } else {
                item.setTitle("夜间模式");
            }
            return true;
        } else if (item.getItemId() == R.id.action_gate) {
            closeAnimation();
            if (isNewWorld) {
                item.setTitle("关上一扇窗");
            } else {
                item.setTitle("打开一扇门");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mRecyclerView.scrollToPosition(data.getIntExtra("index", 0));
        }
    }
}
