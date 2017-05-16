package idv.neo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import idv.neo.R;


/**
 * Created by Neo on 2017/4/15.
 */

public class SensorView extends View {
    final String TAG = SensorView.class.getSimpleName();

    final int dotRadius = 10;
    final float ratio = 50f;

    Paint mPaint = new Paint();
    float mX, mY;
    float mZ = 0f;

    Bitmap mBitmap;
    float mXOrigin, mYOrigin;

    public SensorView(Context context) {
        super(context);

        // rescale the ball so it's about 0.5 cm on screen
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

        mPaint.setColor(Color.RED);
        mPaint.setTextSize(24);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // compute the origin of the screen relative to the origin of
        // the bitmap
        mXOrigin = (w - mBitmap.getWidth()) * 0.5f;
        mYOrigin = (h - mBitmap.getHeight()) * 0.5f;
    }

    public void updatePosition(float[] values) {
        mX = values[0] * ratio;
        mY = values[1] * ratio;
        mZ = values[2] * ratio;

        Log.d(TAG, "updatePosition - values: " + mX + ", " + mY + ", " + mZ);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");

        float x = mXOrigin + mX;
        float y = mYOrigin - mY;
        canvas.drawBitmap(mBitmap, x, y, null);

        canvas.drawCircle(mXOrigin, mYOrigin + mZ, dotRadius, mPaint);
        canvas.drawText("Values: " + mX + ", " + mY + ", " + mZ, 100, 100, mPaint);

        invalidate();
    }
}
