package idv.neo.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Neo on 2017/6/7.
 */

public class LabelingBean {
    private static final String TAG = LabelingBean.class.getSimpleName();
    private int[][] mLabelsImage = null;
    private int mWidth = 0;
    private int mHeight = 0;
    //key值為由1~map size的標記點
    private Map<Integer, CharaterImage> mLabelsCharMap = new HashMap<Integer, CharaterImage>();

    public int[][] getLabelsImage() {
        return mLabelsImage;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public void setLabelsImage(int[][] labelsImage) {
        this.mLabelsImage = labelsImage;
    }

    public Map<Integer, CharaterImage> getLabelsCharMap() {
        return mLabelsCharMap;
    }

    public void setLabelsCharMap(Map<Integer, CharaterImage> labelsCharMap) {
        this.mLabelsCharMap = labelsCharMap;
    }

    public static LabelingBean getDefaultBlackWhiteLabelingBean(int[][] images, int width, int height) {
        //預設為白色為背景，黑色為前景
        return getTwoPassLabelingBean(images, width, height, Color.WHITE, Color.BLACK);

    }

    /**
     * two pass Labeling
     */
    public static LabelingBean getTwoPassLabelingBean(int[][] images, int width, int height, int background, int foreground) {
        final LabelingBean labelingBean = new LabelingBean();
        labelingBean.setWidth(width);
        labelingBean.setHeight(height);
        final int[][] rst = new int[width][height];
        // region label starts from 1;
        // this is required as union-find data structure
        int nextLabel = 1;
        final Map<Integer, Set<Integer>> linkedLabel = new HashMap<Integer, Set<Integer>>();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
//                if (x==88&&y==14){
//                	System.out.println("--"+images[x][y]);
//                }
                //白色為背景，標為0
                if (images[x][y] == background) {
                    rst[x][y] = 0;
                    continue;
                }
                //若為前景黑色
                boolean leftBlack = false;
                boolean topBlack = false;
                // if  left and top are 黑色
                if (x > 0 && images[x - 1][y] == foreground) {
                    leftBlack = true;
                }

                if (y > 0 && images[x][y - 1] == foreground) {
                    topBlack = true;
                }
                Set<Integer> set = null;
                //若left and top are White
                if (!leftBlack && !topBlack) {
                    //linked[NextLabel] = set containing NextLabel
                    set = new TreeSet<Integer>();
                    set.add(nextLabel);
                    linkedLabel.put(nextLabel, set);
                    rst[x][y] = nextLabel;
                    nextLabel += 1;
                } else {
                    //若只有左其中一個為黑
                    if (leftBlack && !topBlack) {
                        rst[x][y] = rst[x - 1][y];
                    }
                    //若只有上其中一個為黑
                    if (topBlack && !leftBlack) {
                        rst[x][y] = rst[x][y - 1];
                    }
                    //若左及上皆為黑，取得最小的當(x,y)編號
                    if (leftBlack && topBlack) {
                        if (rst[x - 1][y] < rst[x][y - 1]) {
                            rst[x][y] = rst[x - 1][y];
                            //若左及上不相等
                            if (rst[x - 1][y] != rst[x][y - 1]) {
                                Set<Integer> set1 = linkedLabel.get(rst[x][y]);
                                set1.add(rst[x][y - 1]);
                                Set<Integer> set2 = linkedLabel.get(rst[x][y - 1]);
                                //set2.add( rst[x][y]);
                                set1.addAll(set2);
                                linkedLabel.put(rst[x][y], set1);
                                linkedLabel.put(rst[x][y - 1], set1);
                            }
                        } else {
                            rst[x][y] = rst[x][y - 1];
                            //若左及上不相等
                            if (rst[x - 1][y] != rst[x][y - 1]) {
                                Set<Integer> set1 = linkedLabel.get(rst[x][y]);
                                set1.add(rst[x - 1][y]);
                                Set<Integer> set2 = linkedLabel.get(rst[x - 1][y]);
                                //set.add( rst[x][y]);
                                set1.addAll(set2);
                                linkedLabel.put(rst[x][y], set1);
                                linkedLabel.put(rst[x - 1][y], set1);
                            }
                        }
                    }
                }
            }
        }
        //StartLicenseOCR_V2.doWriteHtml(rst, "lable_pass1.html", mWidth, mHeight);
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        int count = 1;
        for (int i = 0; i < linkedLabel.size(); i++) {
            Set<Integer> set = linkedLabel.get(i + 1);
            boolean isAdd = false;
            for (Integer j : set) {
                if (!map.containsKey(j)) {
                    map.put(j, count);
                    isAdd = true;
                }
            }
            if (isAdd) {
                count++;
            }
        }
        final Map<Integer, CharaterImage> labelsCharMap = labelingBean.getLabelsCharMap();
        // Begin the second pass.  Assign the new labels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                //只要是為黑色，重新標記
                if (images[x][y] == foreground) {
                    Integer z = map.get(rst[x][y]);
                    //System.out.println("x["+x+"] y["+y+"] z["+z+"]");
                    rst[x][y] = z;
                    CharaterImage charaterImage = null;
                    //看是否已經取出此字元陣列
                    if (labelsCharMap.containsKey(z)) {
                        charaterImage = labelsCharMap.get(z);
                        if (!charaterImage.getTopPixel().compareTop(x, y)) {
                            charaterImage.getTopPixel().setXY(x, y);
                        }
                        if (charaterImage.getBottomPixel().compareTop(x, y)) {
                            charaterImage.getBottomPixel().setXY(x, y);
                        }
                        if (!charaterImage.getLeftPixel().compareLeft(x, y)) {
                            charaterImage.getLeftPixel().setXY(x, y);
                        }
                        if (charaterImage.getRightPixel().compareLeft(x, y)) {
                            charaterImage.getRightPixel().setXY(x, y);
                        }

                    } else {
                        charaterImage = new CharaterImage();
                        charaterImage.getTopPixel().setXY(x, y);
                        charaterImage.getBottomPixel().setXY(x, y);
                        charaterImage.getLeftPixel().setXY(x, y);
                        charaterImage.getRightPixel().setXY(x, y);
                    }
                    labelsCharMap.put(z, charaterImage);
                }
            }
        }
        labelingBean.setLabelsImage(rst);
        return labelingBean;
    }
}
