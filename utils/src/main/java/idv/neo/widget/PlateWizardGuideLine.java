package idv.neo.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class PlateWizardGuideLine extends View {
    private static String TAG = PlateWizardGuideLine.class.getSimpleName();
    private Paint mPaint;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private boolean isLandscape;
    private int mPointOne = 0;
    private int mPointTwo = 0;

    public PlateWizardGuideLine(Context context) {
        super(context);
        // 畫筆
        mPaint = new Paint();
        // 顏色
        mPaint.setColor(Color.RED);
        // 反鋸齒
        mPaint.setAntiAlias(true);
        // 線寬
        mPaint.setStrokeWidth(3);
        //空心效果
        mPaint.setStyle(Paint.Style.STROKE);
        //Main == LoadCube
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDeviceWidth = display.getWidth();
        mDeviceHeight = display.getHeight();
        Log.d(TAG, "display width is " + mDeviceWidth);
        Log.d(TAG, "display height is " + mDeviceHeight);
//        if (mDeviceWidth < mDeviceHeight) {
//            Log.d(TAG, "Device is in portrait mode ");
//        } else {
//            isLandscape = true;
//            Log.d(TAG, "Device is in landscape  mode");
//        }
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "Device getResources is in portrait  mode");
        }
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "Device getResources is in landscape  mode");
            isLandscape = true;
        }
    }

    //http://blog.csdn.net/rhljiayou/article/details/7212620
    @Override
    public void onDraw(Canvas canvas) {
        final int sideLength = (int) (Math.min(mDeviceWidth, mDeviceHeight) * .8);
        int x0 = 0, y0 = 0, x1 = 0, y1 = 0;
        final int margin = (int) (Math.min(mDeviceWidth, mDeviceHeight) * .1);
//        for (int i = 1; i < 3; i++) {
//            x0 = ((mDeviceWidth - sideLength) / 2) + (sideLength / 2) * i;
//            y0 = margin / 2;
//            y1 = y0 + sideLength;
//            canvas.drawLine(x0, y0, x0, y1, mPaint);
////			x0 = ((deviceWidth - sideLength) / 2);
////			x1 = ((deviceWidth - sideLength) / 2) + sideLength;
////			y0 = margin/2 + (sideLength / 3) * i;
////			canvas.drawLine(x0,y0,x1,y0,mPaint);
//        }
//        for (int i = 1; i < 3; i++) {
//            if (isLandscape) {
//                x0 = ((mDeviceWidth - sideLength) / 2) + (sideLength / 2) * i;
//                y0 = margin / 2;
//                y1 = y0 + sideLength;
//                canvas.drawLine(x0, y0, x0, y1, mPaint);
//                mPaint.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(x0, y0, 10.0f, mPaint);
//                canvas.drawCircle(x1, y0, 10.0f, mPaint);
//                mPaint.setStyle(Paint.Style.STROKE);
//            } else {
//                x0 = ((mDeviceWidth - sideLength) / 2);
//                x1 = ((mDeviceWidth - sideLength) / 2) + sideLength;
//                y0 = margin / 2 + (sideLength / 3) * i;
//                canvas.drawLine(x0, y0, x1, y0, mPaint);
//                mPaint.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(x0, y0, 10.0f, mPaint);
//                canvas.drawCircle(x1, y0, 10.0f, mPaint);
//                mPaint.setStyle(Paint.Style.STROKE);
//            }
//        }

        if (isLandscape) {
            x0 = (mDeviceWidth / 2) - (sideLength / 2);
            Log.d(TAG, " X0 : " + x0);
            x1 = x0 + 9 * margin;
            Log.d(TAG, "  x1 : " + x1);
            y0 = margin / 2 + (sideLength / 3) * 2;
            Log.d(TAG, " y0 : " + y0);
            y1 = y0 + 3 * margin;
            Log.d(TAG, " y1 : " + y1);
            canvas.drawRect(new Rect(x0, y0, x1, y1), mPaint);
        } else {
            x0 = (mDeviceWidth / 2) - (sideLength / 2);
            Log.d(TAG, " X0 : " + x0);
            x1 = x0 + 8 * margin;

            Log.d(TAG, "  x1 : " + x1);
            y0 = mDeviceHeight / 2 + (sideLength / 2);

            Log.d(TAG, " y0 : " + y0);
            y1 = y0 + 3 * margin;
            Log.d(TAG, " y1 : " + y1);
            canvas.drawRect(x0, y0, x1, y1, mPaint);
        }
    }
}
