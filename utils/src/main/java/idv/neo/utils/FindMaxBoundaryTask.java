package idv.neo.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Neo on 2017/6/7.
 */

public class FindMaxBoundaryTask extends AsyncTask<Object, Integer, Bitmap> {
    private static final String TAG = FindMaxBoundaryTask.class.getSimpleName();
    private OnTaskCompleted mListener;

    public interface OnTaskCompleted {
        void onTaskCompleted(Bitmap bitmap);
    }

    public FindMaxBoundaryTask(OnTaskCompleted listener) {
        this.mListener = listener;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        final PixelImage pixelImage = (PixelImage) params[0];
        final int pTop = (int) params[1];
        final int pLeft = (int) params[2];
        final CharaterImage maxCharImage = (CharaterImage) params[3];
        final Pixel top = maxCharImage.getTopPixel();
        final Pixel bottom = maxCharImage.getBottomPixel();
        final Pixel left = maxCharImage.getLeftPixel();
        final Pixel right = maxCharImage.getRightPixel();
        final int detectWidth = right.getX() - left.getX() + 1;
        final int detectHeight = bottom.getY() - top.getY() + 1;
        final int[][] detectSeg = ImageProcessingUtils.getCharaterImageRoughDetectArea(pixelImage, pTop, pLeft, maxCharImage);
        return BitmapUtils.doubleIntegerArrayToBitmap(detectSeg, detectWidth, detectHeight);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.d(TAG, "onProgressUpdate Show  Progress  : " + progress[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mListener != null && bitmap != null) {
            mListener.onTaskCompleted(bitmap);
        }
    }
}