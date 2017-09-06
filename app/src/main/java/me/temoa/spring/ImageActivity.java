package me.temoa.spring;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

    private static final int RC_WRITE_EXTERNAL_STORAGE = 0x01;
    private static final String EXTRA_NAME_IMAGE_URL = "image_url";

    private String mCurImageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ThemeUtil.getCurTheme(this)) setTheme(R.style.DayTheme);
        else setTheme(R.style.NightTheme);

        setContentView(R.layout.activity_image);

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.image_toolBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_progressBar);

        PhotoView photoView = (PhotoView) findViewById(R.id.image_photoView);
        mCurImageUrl = getIntent().getStringExtra("image_url");
        if (mCurImageUrl != null) {
            Glide.with(this)
                    .load(mCurImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(new GlideDrawableImageViewTarget(photoView) {
                @Override
                public void onResourceReady(GlideDrawable resource,
                                            GlideAnimation<? super GlideDrawable> animation) {
                    super.onResourceReady(resource, animation);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        ImageView downloadIv = (ImageView) findViewById(R.id.image_download);
        downloadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preSavePhoto();
            }
        });
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    private void preSavePhoto() {
        if (mCurImageUrl == null) return;
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            save();
        } else {
            EasyPermissions.requestPermissions(
                    this, "保存图片需要获取读取外部存储的权限", RC_WRITE_EXTERNAL_STORAGE, perms);
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
        if (requestCode == RC_WRITE_EXTERNAL_STORAGE) {
            preSavePhoto();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    public static void launch(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(EXTRA_NAME_IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }
}
