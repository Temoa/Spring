package me.temoa.spring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.temoa.spring.adapter.ImagePagerAdapter;
import me.temoa.spring.widget.PhotoViewPager;
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

    private static final String[] DIALOG_ITEMS = new String[]{"分享图片", "保存图片"};

    private TextView pageNumber;

    private String mCurImageUrl;
    private ArrayList<String> mImageList;
    private int mIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        getIntentData();
        initViews();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mCurImageUrl = intent.getStringExtra(EXTRA_NAME_IMAGE_URL);
        mImageList = intent.getStringArrayListExtra(EXTRA_NAME_IMAGE_LIST);
        mIndex = intent.getIntExtra(EXTRA_NAME_IMAGE_INDEX, 0);
    }

    private void initViews() {
        pageNumber = findViewById(R.id.image_pager_number);

        PhotoViewPager viewPager = findViewById(R.id.image_viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this);
        adapter.setItemClickListener(new ImagePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick() {
                finish();
            }
        });
        adapter.setItemChildClickListener(new ImagePagerAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(View v) {
                showDialog();
                return true;
            }
        });
        adapter.setData(mImageList);
        viewPager.setAdapter(adapter);
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
    }

    private void setIndexTitle() {
        String pageNumberText = (mIndex + 1) + "/" + mImageList.size();
        pageNumber.setText(pageNumberText);
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选择")
                .setItems(DIALOG_ITEMS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                share();
                                break;
                            case 1:
                                preSavePhoto();
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }

    @SuppressLint("CheckResult")
    public void share() {
        Observable
                .create(new ObservableOnSubscribe<File>() {
                    @Override
                    public void subscribe(ObservableEmitter<File> e) throws Exception {
                        File glideFile = Glide.with(MyApp.getInstance())
                                .asFile()
                                .load(mCurImageUrl)
                                .submit()
                                .get();
                        e.onNext(glideFile);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        share(file);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(ImageActivity.this, "分享失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void share(File photoFile) {
        Intent shareIntent = new Intent();
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this, getString(R.string.authorities), photoFile);
        } else {
            imageUri = Uri.fromFile(photoFile);
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    @AfterPermissionGranted(RC_EXTERNAL_STORAGE)
    public void preSavePhoto() {
        if (mCurImageUrl == null) return;
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            save();
        } else {
            String noticeText = "保存图片需要获取读取外部存储的权限";
            EasyPermissions.requestPermissions(this, noticeText, RC_EXTERNAL_STORAGE, perms);
        }
    }

    @SuppressLint("CheckResult")
    private void save() {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        File glideFile = Glide.with(MyApp.getInstance())
                                .asFile()
                                .load(mCurImageUrl)
                                .submit()
                                .get();

                        if (glideFile == null) {
                            throw new RuntimeException("download picture failure!");
                        }

                        final File photoFolder = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                .getAbsoluteFile();
                        if (!photoFolder.exists()) {
                            if (!photoFolder.mkdirs()) {
                                throw new RuntimeException("make picture directory failure!");
                            }
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

                        e.onNext("保存成功");
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        notifySaveResult(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        notifySaveResult("保存失败");
                    }
                });
    }

    private void notifySaveResult(String result) {
        Toast.makeText(ImageActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // 权限全部请求成功的话，EasyPermissions 会调用注解 @AfterPermissionsGranted 的方法
        // 注意，是全部
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
