package me.temoa.spring.network.image;

/**
 * Created by Temoa
 * on 2018/5/7.
 */
public interface OnProgressListener {
    void progress(String url, int percent, boolean finish);
}
