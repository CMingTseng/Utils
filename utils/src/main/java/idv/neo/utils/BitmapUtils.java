package idv.neo.utils;

import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();

    public static Bitmap createQR_Code_Base_Bitmap(int qrcodewidth, int qrcodeheight) {
        return Bitmap.createBitmap(qrcodewidth, qrcodeheight, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap drawQR_CodeToBitmap(Bitmap bitmap, BitMatrix input, int qrcodewidth, int qrcodeheight) {
        Bitmap output = bitmap;
        if (bitmap == null) {
            output = createQR_Code_Base_Bitmap(qrcodewidth, qrcodeheight);
        }
        if (input == null) {
            return null;
        }
// 將 QR code 資料矩陣繪製到點陣圖上
        for (int y = 0; y < qrcodeheight; y++) {
            for (int x = 0; x < qrcodewidth; x++) {
                output.setPixel(x, y, input.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return output;
    }

    //https://stackoverflow.com/questions/9192982/displaying-yuv-image-in-android
    //http://blog.csdn.net/yanzi1225627/article/details/8626411
    //https://stackoverflow.com/questions/9192982/displaying-yuv-image-in-android
    public static Bitmap rawByteArrayUseYuvImagToRGBBitmap(byte[] data, int format, int width, int height) {
        final YuvImage yuv_image = new YuvImage(data, format, width, height, null);
        // Convert YuV to Jpeg
//        final Rect rect = new Rect(0, 0, width, height);
        final ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        yuv_image.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        final byte[] byt = os.toByteArray();
        return BitmapFactory.decodeByteArray(byt, 0, byt.length);
    }

    //http://blog.csdn.net/fireworkburn/article/details/11615531
    public static Bitmap rawByteArrayToRGBABitmap(byte[] data, int width, int height) {
        final int frameSize = width * height;
        final int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static Bitmap getQR_CodeBitmap(Bitmap bitmap, BitMatrix input, int qrcodewidth, int qrcodeheight) {
        return drawQR_CodeToBitmap(bitmap, input, qrcodewidth, qrcodeheight);
    }

    public static Bitmap doubleIntegerArrayToBitmap(int[][] pixelss, int width, int height) {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = pixelss[x][y];
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap getGrayProcessBitmapFromGrayScaleType(final Bitmap src, GrayScaleUtil.GrayScale grayScale) {
        if (null == src || null == grayScale) {
            return null;
        }
        final Bitmap rs = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(rs);
        final Paint paint = new Paint();
        for (int x = 0, width = src.getWidth(); x < width; x++) {
            for (int y = 0, height = src.getHeight(); y < height; y++) {
                int c = src.getPixel(x, y);
                int a = Color.alpha(c);
                int r = Color.red(c);
                int g = Color.red(c);
                int b = Color.blue(c);
                int gc = grayScale.grayScale(r, g, b);
                paint.setColor(Color.argb(a, gc, gc, gc));
                canvas.drawPoint(x, y, paint);
            }
        }
        return rs;
    }

    /**
     * 將圖片化成灰度圖
     */
    public static Bitmap getGrayProcessBitmap(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        result.setPixels(ImageProcessingUtils.transformGrayProcess(width, height), 0, width, 0, 0, width, height);
        return result;
    }

//    //http://androidbiancheng.blogspot.tw/2011/03/blog-post_30.html
//    private Bitmap transformColorProcess(Bitmap src) {
//        int bmWidth = src.getWidth();
//        int bmHeight = src.getHeight();
//        int[] newBitmap = new int[bmWidth * bmHeight];
//        src.getPixels(newBitmap, 0, bmWidth, 0, 0, bmWidth, bmHeight);
//        for (int h = 0; h < bmHeight; h++) {
//            for (int w = 0; w < bmWidth; w++) {
//                int index = h * bmWidth + w;
//                int alpha = newBitmap[index] & 0xff000000;
//                int r = (newBitmap[index] >> 16) & 0xff;
//                int g = (newBitmap[index] >> 8) & 0xff;
//                int b = newBitmap[index] & 0xff;
//                int t = r; //Swap the color
//                r = g;
//                g = b;
//                b = t;
//                newBitmap[index] = alpha | (r << 16) | (g << 8) | b;
//            }
//        }
//        Bitmap bm = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);
//        bm.setPixels(newBitmap, 0, bmWidth, 0, 0, bmWidth, bmHeight);
//        return bm;
//    }

    public static Bitmap getGrayProcessBitmapWithColorMatrix(Bitmap src) {
        final int bmWidth = src.getWidth();
        final int bmHeight = src.getHeight();
        final Bitmap bmpGrayscale = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(bmpGrayscale);
        final Paint paint = new Paint();
        paint.setColorFilter(ImageProcessingUtils.transformGrayWithColorMatrixProcess());
        c.drawBitmap(src, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 轉灰階並加上圓角
     *
     * @param bmpOriginal 來源Bitmap
     * @param pixels      圓角的弧度
     * @return 修改後的圖片
     */
    public static Bitmap getRoundCornerGrayscaleBitmap(Bitmap bmpOriginal, int pixels) {
        return getRoundCornerBitMap(getGrayProcessBitmapWithColorMatrix(bmpOriginal), pixels);
    }

    /**
     * 加上圓角
     * http://www.blogjava.net/jayslong/archive/2011/03/23/android_image_tools.html
     *
     * @param src    來源Bitmap
     * @param pixels 圓角的弧度
     * @return 圓角圖片
     */
    public static Bitmap getRoundCornerBitMap(Bitmap src, int pixels) {
        final Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
        return output;
    }

    /**
     * 對圖像進行預處理
     */
    public static Bitmap doPretreatment(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final int[] pixels = new int[width * height];
        int maxGrayValue = 0, minGrayValue = 255;
        // 計算最大及最小灰度值
        final int[] p = ImageProcessingUtils.getMinMaxGrayValue(width, height, pixels);
        minGrayValue = p[0];
        maxGrayValue = p[1];
        final int T1 = ImageProcessingUtils.getIterationHresholdValue(width, height, pixels, minGrayValue, maxGrayValue);
        return binarization(input, T1);
    }

    public static Bitmap doPretreatmentWithSelectBinarization(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final int[] pixels = new int[width * height];
        int maxGrayValue = 0, minGrayValue = 255;
        // 計算最大及最小灰度值
        final int[] p = ImageProcessingUtils.getMinMaxGrayValue(width, height, pixels);
        minGrayValue = p[0];
        maxGrayValue = p[1];
        // 計算迭代法閾值
        final int T1 = ImageProcessingUtils.getIterationHresholdValue(width, height, pixels, minGrayValue, maxGrayValue);
        // 計算大津法閾值
        int T2 = ImageProcessingUtils.getOtsuHresholdValue(width, height, pixels, minGrayValue, maxGrayValue);
        // 計算最大熵法閾值
        int T3 = ImageProcessingUtils.getMaxEntropytHresholdValue(width, height, pixels, minGrayValue, maxGrayValue);
        final int[] T = {T1, T2, T3};
        return selectBinarization(input, T);
    }

    // 針對單個閾值二值化圖片
    public static Bitmap binarization(Bitmap input, int T) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final int pixels[] = new int[width * height];
        // 用閾值T1對圖像進行二值化
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = pixels[i * width + j];
                if (gray < T) {
                    // 小於閾值設為白色
                    pixels[i * width + j] = Color.rgb(0, 0, 0);
                } else {
                    // 大於閾值設為黑色
                    pixels[i * width + j] = Color.rgb(255, 255, 255);
                }
            }
        }
        final Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 由3個閾值投票二值化圖片
     *
     * @param T 三種方法獲得的閾值
     * @return 二值化的圖片
     */
//    public static Bitmap selectBinarization(Bitmap input,int width, int height, int[] pixels, int[] T) {
    public static Bitmap selectBinarization(Bitmap input, int[] T) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final int pixels[] = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = pixels[i * width + j];
                if (gray < T[0] && gray < T[1] || gray < T[0] && gray < T[2]
                        || gray < T[1] && gray < T[2]) {
                    pixels[i * width + j] = Color.rgb(0, 0, 0);
                } else {
                    pixels[i * width + j] = Color.rgb(255, 255, 255);
                }
            }
        }
        final Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
