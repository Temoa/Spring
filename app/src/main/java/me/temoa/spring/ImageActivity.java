package me.temoa.spring;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import me.temoa.spring.util.ThemeUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Lai
 * on 2017/9/4 18:59
 */

public class ImageActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_EXTERNAL_STORAGE = 0x01;
    private static final String EXTRA_NAME_IMAGE_URL = "image_url";
    private static final String EXTRA_NAME_IMAGE_LIST = "image_list";
    private static final String EXTRA_NAME_IMAGE_INDEX = "image_index";

    private String mCurImageUrl;
    private ArrayList<String> mImageList;
    private int mIndex;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                share((File) msg.obj);
                return;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ThemeUtil.getCurTheme(this)) setTheme(R.style.DayTheme);
        else setTheme(R.style.NightTheme);

        setContentView(R.layout.activity_image);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.image_toolBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCurImageUrl = getIntent().getStringExtra(EXTRA_NAME_IMAGE_URL);
        mImageList = getIntent().getStringArrayListExtra(EXTRA_NAME_IMAGE_LIST);
        mIndex = getIntent().getIntExtra(EXTRA_NAME_IMAGE_INDEX, 0);

        ViewPager viewPager = (ViewPager) findViewById(R.id.image_viewPager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mImageList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                FrameLayout view = (FrameLayout) LayoutInflater.from(container.getContext())
                        .inflate(R.layout.item_view_pager, null);
                PhotoView photoView = view.findViewById(R.id.image_item_photoView);
                final ProgressBar progressBar = view.findViewById(R.id.image_item_progressBar);
                Glide.with(container.getContext())
                        .load(mImageList.get(position))
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
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndex = position;
                mCurImageUrl = mImageList.get(position);
                setIndexTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(mIndex, false);
        setIndexTitle();

        ImageView downloadIv = (ImageView) findViewById(R.id.image_download);
        downloadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preSavePhoto();
            }
        });

        ImageView shareIv = (ImageView) findViewById(R.id.image_share);
        shareIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });
    }

    private void setIndexTitle() {
        setTitle((mIndex + 1) + "/" + mImageList.size());
    }

    private void share() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File glideFile = Glide.with(MyApp.getInstance()).load(mCurImageUrl)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    Message message = Message.obtain();
                    message.obj = glideFile;
                    message.what = 1;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void share(File photoFile) {
        Intent shareIntent = new Intent();
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this, "me.temoa.spring.fileProvider", photoFile);
        } else {
            imageUri = Uri.fromFile(photoFile);
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    @AfterPermissionGranted(RC_EXTERNAL_STORAGE)
    private void preSavePhoto() {
        if (mCurImageUrl == null) return;
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            save();
        } else {
            EasyPermissions.requestPermissions(
                    this, "保存图片需要获取读取外部存储的权限", RC_EXTERNAL_STORAGE, perms);
        }
    }

    private void save() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File glideFile = Glide
                            .with(MyApp.getInstance())
                            .load(mCurImageUrl)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    if (glideFile == null) return;

                    final File photoFolder = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .getAbsoluteFile();
                    if (!photoFolder.exists()) {
                        photoFolder.mkdirs();
                    }
                    String suffix;
                    if (mCurImageUrl.contains("png")) {
                        suffix = ".png";
                    } else if (mCurImageUrl.contains("gif")) {
                        suffix = ".gif";
                    } else {
                        suffix = ".jpg";
                    }

                    String photoName = "Spring-" + System.currentTimeMillis() + suffix;
                    File photoFile = new File(photoFolder, photoName);
                    FileInputStream fis;
                    FileOutputStream fos;
                    fis = new FileInputStream(glideFile);
                    fos = new FileOutputStream(photoFile);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fis.close();
                    fos.close();

                    // 通知图库更新
                    Intent intent = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + photoFile.getAbsolutePath()));
                    ImageActivity.this.sendBroadcast(intent);

                    notifySaveResult(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifySaveResult(false);
                }
            }
        }).start();
    }

    private void notifySaveResult(boolean isSucceed) {
        final String text;
        if (isSucceed) {
            text = "保存成功";
        } else {
            text = "保存失败";
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImageActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_EXTERNAL_STORAGE) {
            preSavePhoto();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    public static void launch(Activity activity, String imageUrl, ArrayList<String> imageList, int position) {
        Intent intent = new Intent(activity, ImageActivity.class);
        intent.putExtra(EXTRA_NAME_IMAGE_URL, imageUrl);
        intent.putStringArrayListExtra(EXTRA_NAME_IMAGE_LIST, imageList);
        intent.putExtra(EXTRA_NAME_IMAGE_INDEX, position);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("index", mIndex);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
