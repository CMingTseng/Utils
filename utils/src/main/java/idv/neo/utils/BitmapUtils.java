package idv.neo.utils;

import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
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

    /**
     * 將圖片化成灰度圖
     */
    public static Bitmap converyToGrayImg(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final int pixels[] = new int[width * height];
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        final Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
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
        int[] p = ImageProcessingUtils.getMinMaxGrayValue(width, height, pixels);
        minGrayValue = p[0];
        maxGrayValue = p[1];
        // 計算迭代法閾值
        final int T1 = ImageProcessingUtils.getIterationHresholdValue(width, height, pixels, minGrayValue, maxGrayValue);
        // // 計算大津法閾值
        // int T2 = getOtsuHresholdValue(minGrayValue, maxGrayValue);
        // // 計算最大熵法閾值
        // int T3 = getMaxEntropytHresholdValue(minGrayValue, maxGrayValue);
        // int[] T = { T1, T2, T3 };
        //
        // Bitmap result = selectBinarization(T);
//        Bitmap result = binarization(T1);

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
