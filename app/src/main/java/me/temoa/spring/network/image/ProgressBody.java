package me.temoa.spring.network.image;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Temoa
 * on 2018/5/7.
 */

class ProgressBody extends ResponseBody {

    private final String url;
    private final ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private final OnProgressListener progressListener;

    ProgressBody(String url, ResponseBody body, OnProgressListener listener) {
        this.url = url;
        responseBody = body;
        progressListener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null)
            bufferedSource = Okio.buffer(source(responseBody.source()));
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalRead = 0;
            int lastPercent = -1;

            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                long read = super.read(sink, byteCount);
                totalRead += (read == -1) ? 0 : read;
                int percent = (int) ((totalRead * 1.0F / contentLength()) * 100.0F);
                if (percent != lastPercent) progressListener.progress(url, percent, (read == -1));
                if (percent == 100) progressListener.progress(url, percent, (read == -1));
                lastPercent = percent;
                return read;
            }
        };
    }
}
