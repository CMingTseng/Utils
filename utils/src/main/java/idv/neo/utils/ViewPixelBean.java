package idv.neo.utils;

/**
 * Created by Neo on 2017/6/7.
 */

/**
 * 儲存view vs bitmap之x,y對應
 */
public class ViewPixelBean {
    private final static String TAG = "ViewPixelBean";
    private float mViewX = -1;
    private float mViewY = -1;
    private float mBitmapX = -1;
    private float mBitmapY = -1;

    public float getViewX() {
        return mViewX;
    }

    public void setViewX(float viewX) {
        this.mViewX = viewX;
    }

    public float getViewY() {
        return mViewY;
    }

    public void setViewY(float viewY) {
        this.mViewY = viewY;
    }

    public float getBitmapX() {
        return mBitmapX;
    }

    public void setBitmapX(float bitmapX) {
        this.mBitmapX = bitmapX;
    }

    public float getBitmapY() {
        return mBitmapY;
    }

    public void setBitmapY(float bitmapY) {
        this.mBitmapY = bitmapY;
    }

    @Override
    public boolean equals(Object o) {
        final ViewPixelBean v2 = (ViewPixelBean) o;
        return (mBitmapX == v2.getBitmapX() && mBitmapY == v2.getBitmapY());
    }
}
