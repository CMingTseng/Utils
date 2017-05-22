package idv.neo.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Neo on 2017/5/22.
 */

public class QRCodeUtils {
    private static final String TAG = QRCodeUtils.class.getSimpleName();
    // ZXing 還可以生成其他形式條碼，如：BarcodeFormat.CODE_39、BarcodeFormat.CODE_93、BarcodeFormat.CODE_128、BarcodeFormat.EAN_8、BarcodeFormat.EAN_13...
    // QR code 內容編碼
    private static Map<EncodeHintType, Object> sHints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
    private static MultiFormatWriter sWriter = new MultiFormatWriter();

    public static BitMatrix createQR_Code_DateMatrix(EncodeHintType type, Object object, String qrcodeContent, int qrcodewidth, int qrcodeheight) {
        sHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
        // 設定 QR code 容錯率為 H
        sHints.put(type, object);
        // 建立 QR code 的資料矩陣
        if (qrcodeContent == null) {
            qrcodeContent = "no data !!";
        }
        try {
            return sWriter.encode(qrcodeContent, BarcodeFormat.QR_CODE, qrcodewidth, qrcodeheight, sHints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
