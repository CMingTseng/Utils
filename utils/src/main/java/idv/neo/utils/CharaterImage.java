package idv.neo.utils;

/**
 * Created by Neo on 2017/6/7.
 */

/**
 * 於使用連通區塊標記後各字型區域
 */
public class CharaterImage {
    private int mWidth = 0;
    private int mHeight = 0;
    private Pixel mTop = new Pixel();
    private Pixel mBottom = new Pixel();
    private Pixel mLeft = new Pixel();
    private Pixel mRight = new Pixel();
    private int[][] mImages = null;

    public Pixel getTopPixel() {
        return mTop;
    }

    public void setTopPixel(Pixel top) {
        this.mTop = top;
    }

    public Pixel getBottomPixel() {
        return mBottom;
    }

    public void setBottomPixel(Pixel bottom) {
        this.mBottom = bottom;
    }

    public Pixel getLeftPixel() {
        return mLeft;
    }

    public void setLeftPixel(Pixel left) {
        this.mLeft = left;
    }

    public Pixel getRightPixel() {
        return mRight;
    }

    public void setRightPixel(Pixel right) {
        this.mRight = right;
    }

    public int getWidth() {
        return (mRight.getX() - mLeft.getX());
    }

    public int getHeight() {
        return (mBottom.getY() - mTop.getY());
    }

    public int[][] getImages() {
        return mImages;
    }

    public void setImages(int[][] images) {
        this.mImages = images;
    }
}
