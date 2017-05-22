package idv.neo.utils;

import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();
    private static Bitmap img;
    private static int imgWidth;
    private static int imgHeight;
    private static int[] imgPixels;

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

    public static Bitmap getQR_CodeBitmap(Bitmap bitmap, BitMatrix input, int qrcodewidth, int qrcodeheight) {
        return drawQR_CodeToBitmap(bitmap, input, qrcodewidth, qrcodeheight);
    }

    private static void setImgInfo(Bitmap image) {
        img = image;
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        imgPixels = new int[imgWidth * imgHeight];
        img.getPixels(imgPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
    }

    /**
     * 將圖片化成灰度圖
     */
    public static Bitmap converyToGrayImg(Bitmap img) {

        setImgInfo(img);

        return getGrayImg();
    }

    /**
     * 對圖像進行預處理
     */
    public static Bitmap doPretreatment(Bitmap img) {

        setImgInfo(img);

        Bitmap grayImg = getGrayImg();

        int[] p = new int[2];
        int maxGrayValue = 0, minGrayValue = 255;
        // 計算最大及最小灰度值
        getMinMaxGrayValue(p);
        minGrayValue = p[0];
        maxGrayValue = p[1];
        // 計算迭代法閾值
        int T1 = getIterationHresholdValue(minGrayValue, maxGrayValue);
        // // 計算大津法閾值
        // int T2 = getOtsuHresholdValue(minGrayValue, maxGrayValue);
        // // 計算最大熵法閾值
        // int T3 = getMaxEntropytHresholdValue(minGrayValue, maxGrayValue);
        // int[] T = { T1, T2, T3 };
        //
        // Bitmap result = selectBinarization(T);
        Bitmap result = binarization(T1);

        return result;
    }

    /**
     * 獲取當前圖片的灰度圖
     *
     * @param img 原圖片
     * @return 灰度圖
     */
    private static Bitmap getGrayImg() {

        int alpha = 0xFF << 24;
        for (int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                imgPixels[imgWidth * i + j] = grey;
            }
        }
        Bitmap result = Bitmap
                .createBitmap(imgWidth, imgHeight, Config.RGB_565);
        result.setPixels(imgPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        return result;
    }

    private static int getGray(int argb) {
        int alpha = 0xFF << 24;
        int red = ((argb & 0x00FF0000) >> 16);
        int green = ((argb & 0x0000FF00) >> 8);
        int blue = (argb & 0x000000FF);
        int grey;
        grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
        grey = alpha | (grey << 16) | (grey << 8) | grey;
        return grey;
    }

    // 利用迭代法計算閾值
    private static int getIterationHresholdValue(int minGrayValue,
                                                 int maxGrayValue) {
        int T1;
        int T2 = (maxGrayValue + minGrayValue) / 2;
        do {
            T1 = T2;
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int i = 0; i < imgHeight; i++) {
                for (int j = 0; j < imgWidth; j++) {
                    int gray = imgPixels[imgWidth * i + j];
                    if (gray < T1) {
                        s += gray;
                        cs++;
                    }
                    if (gray > T1) {
                        l += gray;
                        cl++;
                    }
                }
            }
            T2 = (int) (s / cs + l / cl) / 2;
        } while (T1 != T2);
        return T1;
    }

    /*
     * 用大津法計算閾值T 大津法又稱為最大類間方差法，由大津在1979年提出，選取使類間方差最
     * 大的灰度級作為分割閾值，方差值越大，說明圖像兩部分差別越大。
     */
    private static int getOtsuHresholdValue(int minGrayValue, int maxGrayValue) {
        int T = 0;
        double U = 0, U0 = 0, U1 = 0;
        double G = 0;
        for (int i = minGrayValue; i <= maxGrayValue; i++) {
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int j = 0; j < imgHeight - 1; j++) {
                for (int k = 0; k < imgWidth - 1; k++) {
                    int gray = imgPixels[imgWidth * j + k];
                    if (gray < i) {
                        s += gray;
                        cs++;
                    }
                    if (gray > i) {
                        l += gray;
                        cl++;
                    }
                }
            }
            U0 = s / cs;
            U1 = l / cl;
            U = (s + l) / (cs + cl);
            double g = (cs / (cs + cl)) * (U0 - U) * (U0 - U)
                    + (cl / (cl + cs)) * (U1 - U) * (U1 - U);
            if (g > G) {
                T = i;
                G = g;
            }
        }
        return T;
    }

    // 採用一維最大熵法計算閾值
    private static int getMaxEntropytHresholdValue(int minGrayValue,
                                                   int maxGrayValue) {
        int T3 = minGrayValue, sum = 0;
        double E = 0, Ht = 0, Hl = 0;
        int[] p = new int[maxGrayValue - minGrayValue + 1];
        for (int i = minGrayValue; i <= maxGrayValue; i++) {
            for (int j = 0; j < p.length; j++) {
                p[j] = 0;
            }
            sum = 0;
            for (int j = 0; j < imgHeight - 1; j++) {
                for (int k = 0; k < imgWidth - 1; k++) {
                    int gray = imgPixels[imgWidth * j + k];
                    p[gray - minGrayValue] += 1;
                    sum++;
                }
            }

            double pt = 0;
            int offset = maxGrayValue - i;
            for (int j = 0; j < p.length - offset; j++) {
                if (p[j] != 0) {
                    Ht += (p[j] * (Math.log(p[j]) - Math.log(sum))) / sum;
                    pt += p[j];
                }
            }
            for (int j = p.length - offset; j < maxGrayValue - minGrayValue + 1; j++) {
                if (p[j] != 0) {
                    Ht += (p[j] * (Math.log(p[j]) - Math.log(sum))) / sum;
                }
            }
            pt /= sum;
            double e = Math.log(pt * (1 - pt)) - (Ht / pt) - Hl / (1 - pt);

            if (E < e) {
                E = e;
                T3 = i;
            }
        }
        return T3;
    }

    // 針對單個閾值二值化圖片
    private static Bitmap binarization(int T) {
        // 用閾值T1對圖像進行二值化
        for (int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int gray = imgPixels[i * imgWidth + j];
                if (gray < T) {
                    // 小於閾值設為白色
                    imgPixels[i * imgWidth + j] = Color.rgb(0, 0, 0);
                } else {
                    // 大於閾值設為黑色
                    imgPixels[i * imgWidth + j] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap result = Bitmap
                .createBitmap(imgWidth, imgHeight, Config.RGB_565);
        result.setPixels(imgPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        return result;
    }

    // 計算最大最小灰度,保存在陣列中
    private static void getMinMaxGrayValue(int[] p) {
        int minGrayValue = 255;
        int maxGrayValue = 0;
        for (int i = 0; i < imgHeight - 1; i++) {
            for (int j = 0; j < imgWidth - 1; j++) {
                int gray = imgPixels[i * imgWidth + imgHeight];
                if (gray < minGrayValue)
                    minGrayValue = gray;
                if (gray > maxGrayValue)
                    maxGrayValue = gray;
            }
        }
        p[0] = minGrayValue;
        p[1] = maxGrayValue;
    }

    /**
     * 由3個閾值投票二值化圖片
     *
     * @param img 原圖片
     * @param T   三種方法獲得的閾值
     * @return 二值化的圖片
     */
    private static Bitmap selectBinarization(int[] T) {
        for (int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int gray = imgPixels[i * imgWidth + j];
                if (gray < T[0] && gray < T[1] || gray < T[0] && gray < T[2]
                        || gray < T[1] && gray < T[2]) {
                    imgPixels[i * imgWidth + j] = Color.rgb(0, 0, 0);
                } else {
                    imgPixels[i * imgWidth + j] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap result = Bitmap
                .createBitmap(imgWidth, imgHeight, Config.RGB_565);
        result.setPixels(imgPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        return result;
    }

    // 計算圖元點（x,y)周圍圖元點的中值
    private static int getCenterValue(Bitmap img, int x, int y) {
        int[] pix = new int[9];

        int w = img.getHeight() - 1;
        int h = img.getWidth() - 1;
        //
        if (x > 0 && y > 0)
            pix[0] = getGray(img.getPixel(x - 1, y - 1));
        if (y > 0)
            pix[1] = getGray(img.getPixel(x, y - 1));
        if (x < h && y > 0)
            pix[2] = getGray(img.getPixel(x + 1, y - 1));
        if (x > 0)
            pix[3] = getGray(img.getPixel(x - 1, y));
        pix[4] = getGray(img.getPixel(x, y));
        if (x < h)
            pix[5] = getGray(img.getPixel(x + 1, y));
        if (x > 0 && y < w)
            pix[6] = getGray(img.getPixel(x - 1, y + 1));
        if (y < w)
            pix[7] = getGray(img.getPixel(x, y + 1));
        if (x < h && y < w)
            pix[8] = getGray(img.getPixel(x + 1, y + 1));

        int max = 0, min = 255;
        for (int i = 0; i < pix.length; i++) {
            if (pix[i] > max)
                max = pix[i];
            if (pix[i] < min)
                min = pix[i];
        }
        int count = 0;
        int i = 0;
        for (i = 0; i < 9; i++) {
            if (pix[i] >= min)
                count++;
            if (count == 5)
                break;
        }
        return pix[i];
    }
}
