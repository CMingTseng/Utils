package idv.neo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class Guides extends View {
    private static String TAG = "Guides";
    Paint mPaint = new Paint();
    int sideLength;
    int deviceWidth;
    int deviceHeight;

    public Guides(Context context) {
        super(context);
        mPaint.setColor(Color.RED);
        //Main == LoadCube
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        deviceWidth = display.getWidth();
        deviceHeight = display.getHeight();
        sideLength = (int) (Math.min(deviceWidth, deviceHeight) * .9);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int x0, y0, x1, y1, margin;
        margin = (int) (Math.min(deviceWidth, deviceHeight) * .1);
        for (int i = 1; i < 3; i++) {
            x0 = ((deviceWidth - sideLength) / 2) + (sideLength / 2) * i;
            y0 = margin / 2;
            y1 = y0 + sideLength;
            canvas.drawLine(x0, y0, x0, y1, mPaint);
//			x0 = ((deviceWidth - sideLength) / 2);
//			x1 = ((deviceWidth - sideLength) / 2) + sideLength;
//			y0 = margin/2 + (sideLength / 3) * i;
//			canvas.drawLine(x0,y0,x1,y0,mPaint);
        }

//		mPaint.setTextSize(60);
//		canvas.rotate(-90);
//		x0 = -deviceHeight + margin/2 + 15;
//		y0 = (deviceWidth - sideLength)/2 + 62;
//		canvas.drawText("TL", x0,y0, mPaint);
//		
//		x0 = -deviceHeight + margin/2 + 15;
//		y0 = 62;
//		canvas.drawText(getSide(), x0,y0, mPaint );

        //canvas.drawLine(0, 0, 20, 20, paint);
        //canvas.drawLine(20, 0, 0, 20, paint);
    }
}
