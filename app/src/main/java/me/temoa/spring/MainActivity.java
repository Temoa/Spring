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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.temoa.spring.adapter.MainAdapter;
import me.temoa.spring.bean.Gank;
import me.temoa.spring.network.GankRetrofitClient;
import me.temoa.spring.network.RxCallback;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private RelativeLayout mContainer;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private FloatingActionButton mFab;

    private int page = 1;
    private Subscription dataSubscription;

    private boolean isNight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
            dataSubscription = null;
        }
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolBar);
        setSupportActionBar(mToolbar);
        View titleTv = mToolbar.getChildAt(0);
        if (titleTv instanceof TextView) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/IndieFlower.ttf");
            TextView t = (TextView) titleTv;
            t.setTextSize(24.f);
            t.setTypeface(typeface);
        }

        mContainer = (RelativeLayout) findViewById(R.id.main_container);
        mFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerView);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MainAdapter(this, null);
        mAdapter.setItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, String url, int position) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("image_url", url);
                startActivity(intent);
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
                    mAdapter.setLoadCompleted();
                    mAdapter.addData(urls);
                } else {
                    mAdapter.setNewData(urls);
                    open();
                }
                page++;
            }

            @Override
            public void onFinished() {

            }
        };
        dataSubscription = GankRetrofitClient.getInstance().get(page, callback);
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

    private void open() {
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

    private void close() {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRecyclerView,
                mRecyclerView.getWidth(),
                mRecyclerView.getHeight(),
                mRecyclerView.getHeight(),
                0);
        animator.setDuration(700);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRecyclerView.setVisibility(View.INVISIBLE);
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
    }

    private void refreshUi() {
        TypedValue background = new TypedValue();
        TypedValue textColor = new TypedValue();
        TypedValue accentColor = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.cBackground, background, true);
        theme.resolveAttribute(R.attr.cTextColor, textColor, true);
        theme.resolveAttribute(R.attr.colorAccent, accentColor, true);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_night) {
            toggleThemeSetting();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
