package me.temoa.spring.network.image;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.util.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Temoa
 * on 2018/1/6.
 */

public class ProgressModelLoader implements StreamModelLoader<String> {

    private Handler mHandler;

    public ProgressModelLoader(Handler handler) {
        mHandler = handler;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(String model, int width, int height) {
        return new ProgressDataFetcher(mHandler, model);
    }

    private static class ProgressDataFetcher implements DataFetcher<InputStream> {

        private Handler mHandler;
        private final String mUrl;
        private InputStream mStream;
        private ResponseBody mResponseBody;
        private volatile boolean isCancelled;

        ProgressDataFetcher(@NonNull Handler handler, String url) {
            mHandler = handler;
            mUrl = url;
        }

        @Override
        public InputStream loadData(Priority priority) throws Exception {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(new ProgressInterceptor(mHandler));
            OkHttpClient client = builder.build();
            Request.Builder requestBuilder = new Request.Builder().url(mUrl);
            Request request = requestBuilder.build();
            if (isCancelled) {
                return null;
            }
            Response response = client.newCall(request).execute();
            mResponseBody = response.body();
            if (!response.isSuccessful() || mResponseBody == null) {
                throw new IOException("Request failed with code: " + response.code());
            }
            mStream = ContentLengthInputStream.obtain(mResponseBody.byteStream(),
                    mResponseBody.contentLength());
            return mStream;
        }

        @Override
        public void cleanup() {
            try {
                if (mStream != null) {
                    mStream.close();
                }
                if (mResponseBody != null) {
                    mResponseBody.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getId() {
            return mUrl;
        }

        @Override
        public void cancel() {
            isCancelled = true;
        }
    }

    private static class ProgressInterceptor implements Interceptor {

        private Handler mHandler;

        ProgressInterceptor(Handler handler) {
            mHandler = handler;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            ResponseBody body = response.body();
            return response.newBuilder().body(new ProgressResponseBody(body, getListener())).build();
        }

        private ProgressListener getListener() {
            return new ProgressListener() {
                @Override
                public void progress(int progress) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = progress;
                        mHandler.sendMessage(msg);
                    }
                }
            };
        }
    }

    private static class ProgressResponseBody extends ResponseBody {

        private BufferedSource mBufferedSource;
        private ResponseBody mResponseBody;
        private ProgressListener mProgressListener;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            mResponseBody = responseBody;
            mProgressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return mResponseBody.contentType();
        }

        @Override
        public long contentLength() {
            return mResponseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (mBufferedSource == null) {
                mBufferedSource = Okio.buffer(new ProgressSource(mResponseBody.source()));
            }
            return mBufferedSource;
        }

        private class ProgressSource extends ForwardingSource {

            long totalBytesRead = 0;
            int currentProgress;
            long lastUpdateProgressTime = 0;

            ProgressSource(Source delegate) {
                super(delegate);
            }

            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                long fullLength = mResponseBody.contentLength();
                if (bytesRead == -1) {
                    totalBytesRead = fullLength;
                } else {
                    totalBytesRead += bytesRead;
                }
                int progress = (int) (100.F * totalBytesRead / fullLength);
                if (mProgressListener != null && progress != currentProgress) {
                    if (System.currentTimeMillis() - lastUpdateProgressTime > 50)
                        mProgressListener.progress(progress);
                    lastUpdateProgressTime = System.currentTimeMillis();
                }
                if (mProgressListener != null && totalBytesRead == fullLength) {
                    mProgressListener = null;
                }
                currentProgress = progress;
                return bytesRead;
            }
        }
    }
}
