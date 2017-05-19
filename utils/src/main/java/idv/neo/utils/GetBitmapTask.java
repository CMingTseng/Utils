package idv.neo.utils;

/**
 * Created by Neo on 2017/3/23.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class GetBitmapTask extends AsyncTask<Object, String, Bitmap> {
    private static final String TAG = GetBitmapTask.class.getSimpleName();
    private OnTaskCompleted mListener;

    public interface OnTaskCompleted {
        void onTaskCompleted(Bitmap bitmap);
    }

    public GetBitmapTask(OnTaskCompleted listener) {
        this.mListener = listener;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap bmp = null;
        try {
            bmp = Glide.with((Context) params[0]).load((File) params[1]).asBitmap().into(-1, -1).get();
        } catch (final ExecutionException e) {
            Log.e(TAG, e.getMessage());
        } catch (final InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onTaskCompleted(bitmap);
        }
    }
}
