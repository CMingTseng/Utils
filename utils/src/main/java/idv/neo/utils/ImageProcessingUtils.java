package idv.neo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;

import java.util.Map;
import java.util.Set;

/**
 * Created by Neo on 2017/6/7.
 */

public class ImageProcessingUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();

    // 計算最大最小灰度,保存在陣列中
    public static int[] getMinMaxGrayValue(int width, int height, int[] pixels) {
        final int[] p = new int[2];
        int minGrayValue = 255;
        int maxGrayValue = 0;
        for (int i = 0; i < height - 1; i++) {
            for (int j = 0; j < width - 1; j++) {
                int gray = pixels[i * width + height];
                if (gray < minGrayValue)
                    minGrayValue = gray;
                if (gray > maxGrayValue)
                    maxGrayValue = gray;
            }
        }
        p[0] = minGrayValue;
        p[1] = maxGrayValue;
        return p;
    }

    /**
     * 轉變為2維灰度圖像
     */
    public static int[][] toGrays(int[] pixels, int width, int height) {
        final int[][] grayPixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = pixels[y * width + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = (pixel) & 0xff;
                int gray = new Double(0.299 * r + 0.587 * g + 0.114 * b).intValue();
                //Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue
                grayPixels[x][y] = 255 << 24 | gray << 16 | gray << 8 | gray;
            }
        }
        return grayPixels;
    }

    /**
     * 轉變為2維灰度圖像
     */
    public static int[][] toGrays(int[][] pixels, int width, int height) {
        final int[][] grayPixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = pixels[x][y];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = (pixel) & 0xff;
                int gray = new Double(0.299 * r + 0.587 * g + 0.114 * b).intValue();
                //Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue
                grayPixels[x][y] = 255 << 24 | gray << 16 | gray << 8 | gray;
            }
        }
        return grayPixels;
    }

    // 利用迭代法計算閾值
    public static int getIterationHresholdValue(int width, int height, int[] pixels, int minGrayValue, int maxGrayValue) {
        int T1;
        int T2 = (maxGrayValue + minGrayValue) / 2;
        do {
            T1 = T2;
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    final int gray = pixels[width * i + j];
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
    public static int getOtsuHresholdValue(int width, int height, int[] pixels, int minGrayValue, int maxGrayValue) {
        int T = 0;
        double U = 0, U0 = 0, U1 = 0;
        double G = 0;
        for (int i = minGrayValue; i <= maxGrayValue; i++) {
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int j = 0; j < height - 1; j++) {
                for (int k = 0; k < width - 1; k++) {
                    int gray = pixels[width * j + k];
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

    /**
     *
     *
     * */
    public static int otsuHresholdValue(int[][] pix, int width, int height) {
        final int wh = width * height;
        int i, j, t;
        int L = 256;
        final int[][] tempPix = new int[width][height];
        double[] p = new double[L];
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                tempPix[i][j] = pix[i][j] & 0xff; //取出RGB
            }
        }
        for (i = 0; i < L; i++) {
            p[i] = 0;
        }
        //計算各灰階像素出現次數
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                p[tempPix[i][j]]++;//計算RGB於256之直方圖
            }
        }
        //計算各灰階像素出現機率
        for (int m = 0; m < L; m++) {
            p[m] = p[m] / wh;
        }
        final double[] sigma = new double[L];
        for (t = 0; t < L; t++) {
            double w0 = 0;
            //影像灰度值小於等於t值的機率
            for (int m = 0; m < t + 1; m++) {
                w0 += p[m];
            }
            //影像灰度值大於t值的機率
            double w1 = 1 - w0;
            double u0 = 0;
            //影像灰度值小於等於t值的平均值(mean)
            for (int m = 0; m < t + 1; m++) {
                u0 += m * p[m] / w0;
            }
            double u1 = 0;
            //影像灰度值大於等於t值的平均值(mean)
            for (int m = t; m < L; m++) {
                u1 += m * p[m] / w1;
            }
            sigma[t] = w0 * w1 * (u0 - u1) * (u0 - u1);
        }
        double max = 0.0;
        int T = 0;
        for (i = 0; i < L - 1; i++) {
            if (max < sigma[i]) {
                max = sigma[i];
                T = i;
            }
        }
        return T;
    }

    // 採用一維最大熵法計算閾值
    public static int getMaxEntropytHresholdValue(int width, int height, int[] pixels, int minGrayValue, int maxGrayValue) {
        int T3 = minGrayValue, sum = 0;
        double E = 0, Ht = 0, Hl = 0;
        int[] p = new int[maxGrayValue - minGrayValue + 1];
        for (int i = minGrayValue; i <= maxGrayValue; i++) {
            for (int j = 0; j < p.length; j++) {
                p[j] = 0;
            }
            sum = 0;
            for (int j = 0; j < height - 1; j++) {
                for (int k = 0; k < width - 1; k++) {
                    int gray = pixels[width * j + k];
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

    //最佳閾值分割
    public static int bestThreshValue(int[][] pix, int width, int height) {
        int i;
        int j;
        int thresh;
        int newthresh;
        int gmax;
        int gmin;         //最大,最小灰度值
        final double[] p = new double[256];
        final int[][] im = new int[width][height];
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                im[i][j] = pix[i][j] & 0xff;
            }
        }
        for (i = 0; i < 256; i++) {
            p[i] = 0;
        }
        //1.統計各灰度級出現的次數、灰度最大和最小值
        gmax = 0;
        gmin = 255;
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                int g = im[i][j];
                p[g]++;
                if (g > gmax) {
                    gmax = g;
                }
                if (g < gmin) {
                    gmin = g;
                }
            }
        }
        thresh = 0;
        newthresh = (gmax + gmin) / 2;
        int meangray1, meangray2;
        long p1, p2, s1, s2;
        for (i = 0; (thresh != newthresh) && (i < 100); i++) {
            thresh = newthresh;
            p1 = 0;
            p2 = 0;
            s1 = 0;
            s2 = 0;
            //2. 求兩個區域的灰度平均值
            for (j = gmin; j < thresh; j++) {
                p1 += p[j] * j;
                s1 += p[j];
            }
            meangray1 = (int) (p1 / s1);

            for (j = thresh + 1; j < gmax; j++) {
                p2 += p[j] * j;
                s2 += p[j];
            }
            meangray2 = (int) (p2 / s2);
            //3. 計算新閾值
            newthresh = (meangray1 + meangray2) / 2;
        }
        return newthresh;
    }

    // 計算圖元點（x,y)周圍圖元點的中值
    public static int getCenterValue(Bitmap img, int x, int y) {
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

    //http://aiilive.blog.51cto.com/1925756/1718960
    public static int getGray(int argb) {
        int alpha = 0xFF << 24;
        int red = ((argb & 0x00FF0000) >> 16);
        int green = ((argb & 0x0000FF00) >> 8);
        int blue = (argb & 0x000000FF);
        int grey;
        grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
        grey = alpha | (grey << 16) | (grey << 8) | grey;
        return grey;
    }

    public static int[] transformGrayProcess(int srcwidth, int srcheight) {
        final int[] pixels = new int[srcwidth * srcheight];
        for (int i = 0; i < srcheight; i++) {
            for (int j = 0; j < srcwidth; j++) {
                int operate_index = pixels[srcwidth * i + j];
                operate_index = getGray(pixels[srcwidth * i + j]);
                pixels[srcwidth * i + j] = getGray(operate_index);
            }
        }
        return pixels;
    }

    public static ColorMatrixColorFilter transformGrayWithColorMatrixProcess() {
        final ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        return new ColorMatrixColorFilter(cm);
    }

    /**
     * Sobel對灰階圖片運算
     *
     * thresh閥值-1代表不需要
     */
    public static int[][] doSobelGray(int[][] gray, int iw, int ih, int thresh) {
        byte[][] sx = {{1, 0, -1}, {2, 0, -2}, {1, 0, -1}};
        byte[][] sy = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
        int[][] edger1 = edge(gray, sx, iw, ih);
        int[][] edger2 = edge(gray, sy, iw, ih);
        int[][] sobels = new int[iw][ih];
        int g = 0;
        //File fileTxt=new File(FileOperator.IMAGE_FILE_PATH+"/doSobelGray.txt");
        //FileOutputStream fileOutputStream = new FileOutputStream(fileTxt);
        //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"utf-8");
        for (int j = 0; j < ih; j++) {
            for (int i = 0; i < iw; i++) {
                if (thresh > 0) {
                    if (Math.max(edger1[i][j], edger2[i][j]) > thresh) {
                        g = 0;
                    } else {
                        g = 255;
                    }
                } else {
                    //System.out.println(edger1[i][j]+"|"+edger2[i][j]);
                    g = Math.max(edger1[i][j], edger2[i][j]);//若上下左右相同顏色則值為0，故為黑色
                }
                //System.out.println(g);
                //outputStreamWriter.write(g+" ");
//                px[i+j*iw] = (255<<24)|(g<<16)|(g<<8)|g;
//                sobels[i][j] = (255 << 24) | (g << 16) | (g << 8) | g;
//	        	if(gray[i][j]!=black&&gray[i][j]!=white){
//
//	        		System.out.println("===>g ["+g+"]");
//	        	}
            }
        }
        return sobels;
    }

    /**
     * sobel 運算
     */
    public static int[][] edge(int[][] in, byte[][] tmp, int iw, int ih) {
        int[][] ed = new int[iw][ih];
        for (int j = 1; j < ih - 1; j++) {
            for (int i = 1; i < iw - 1; i++) {
                ed[i][j] = Math.abs(tmp[0][0] * in[i - 1][j - 1] + tmp[0][1] * in[i - 1][j] +
                        tmp[0][2] * in[i - 1][j + 1] + tmp[1][0] * in[i][j - 1] +
                        tmp[1][1] * in[i][j] + tmp[1][2] * in[i][j + 1] +
                        tmp[2][0] * in[i + 1][j - 1] + tmp[2][1] * in[i + 1][j] +
                        tmp[2][2] * in[i + 1][j + 1]);
                //k+=ed[i][j];
            }
        }
        return ed;
    }

    /**
     * 將得到的標記後圖片LabelingBean
     * 取出最大區塊
     */
    public static CharaterImage getMarkLabelingBeanMax(LabelingBean labelingBean) {
        final Map<Integer, CharaterImage> map = labelingBean.getLabelsCharMap();
        CharaterImage maxCharImage = null;
        Log.d(TAG, "===>getTwoPassLabelingBean size [" + map.size() + "]");
        final Set<Map.Entry<Integer, CharaterImage>> set = map.entrySet();

        for (Map.Entry<Integer, CharaterImage> entry : set) {
            CharaterImage charaterImage = entry.getValue();
            if (maxCharImage == null ||
                    (maxCharImage.getHeight() * maxCharImage.getWidth() < (charaterImage.getHeight() * charaterImage.getWidth()))) {
                maxCharImage = charaterImage;
            }
        }
        return maxCharImage;
    }

    /**
     * 將得到的標記後圖片LabelingBean
     * 做簡單紀錄
     */
    public static int[][] doRecMarkLabelingBean(LabelingBean labelingBean) {
        final Map<Integer, CharaterImage> map = labelingBean.getLabelsCharMap();
        int[][] datas = labelingBean.getLabelsImage();
        int width = labelingBean.getWidth();
        int height = labelingBean.getHeight();
        int[][] hdatas = new int[width][height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                //只要是為黑色，重新標記
                if (datas[x][y] > 0) {
                    hdatas[x][y] = new Color().BLACK;
                    //System.out.println("--------");
                } else {
                    hdatas[x][y] = new Color().WHITE;

                }
            }
        }
        final Set<Map.Entry<Integer, CharaterImage>> set = map.entrySet();
        for (Map.Entry<Integer, CharaterImage> entry : set) {
            drawCharaterImageRectangle(entry.getValue(), hdatas, Color.RED);
        }
        return hdatas;
    }

    //畫出矩型範圍
    public static void drawCharaterImageRectangle(CharaterImage charaterImage, int[][] hdatas, int color) {
        final Pixel top = charaterImage.getTopPixel();
        final Pixel bottom = charaterImage.getBottomPixel();
        final Pixel left = charaterImage.getLeftPixel();
        final Pixel right = charaterImage.getRightPixel();
        Log.d(TAG, " top [" + top.toString() + "] bottom [" + bottom.toString() + "] left[" + left.toString() + "] right[" + right + "]");
        int topY = top.getY();
        int bottomY = bottom.getY();
        int leftX = left.getX();
        int rightX = right.getX();
        //(左上->右上)畫最上及最下
        for (int x = leftX; x < (rightX + 1); x++) {
            hdatas[x][topY] = color;
            hdatas[x][bottomY] = color;
        }
        //(左下->右下)畫最左及最右
        for (int y = topY; y < (bottomY + 1); y++) {
            hdatas[leftX][y] = color;
            hdatas[rightX][y] = color;
        }
    }

    //畫出矩型範圍
    public static int[][] getCharaterImageRoughDetectArea(PixelImage pixelImage, int pTop, int pLeft, CharaterImage charaterImage) {
        final Pixel top = charaterImage.getTopPixel();
        final Pixel bottom = charaterImage.getBottomPixel();
        final Pixel left = charaterImage.getLeftPixel();
        final Pixel right = charaterImage.getRightPixel();
        //車牌粗定位
        int detectWidth = right.getX() - left.getX() + 1;
        int detectHeight = bottom.getY() - top.getY() + 1;
        int[][] detectSeg = new int[detectWidth][detectHeight];
        for (int x = left.getX(); x <= right.getX(); x++) {
            for (int y = top.getY(); y <= bottom.getY(); y++) {
                detectSeg[x - left.getX()][y - top.getY()] = pixelImage.getPixels()[(y + (int) pTop) * pixelImage.getWidth() + (x + (int) pLeft)];
            }
        }
        return detectSeg;
    }
}
