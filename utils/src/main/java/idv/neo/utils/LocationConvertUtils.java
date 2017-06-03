package idv.neo.utils;

/**
 * Created by Neo on 2017/4/13.
 */

public class LocationConvertUtils {
    private static final String TAG = LocationConvertUtils.class.getSimpleName();

    //https://stackoverflow.com/questions/5280479/how-to-save-gps-coordinates-in-exif-data-on-android
    public static final String makeLatLongToString(double d) {
        /**
         * convert latitude into DMS (degree minute second) format. For instance<br/>
         * -79.948862 becomes<br/>
         *  79/1,56/1,55903/1000<br/>
         * It works for latitude and longitude<br/>
         * @param latitude could be longitude.
         * @return
         */
        d = Math.abs(d);
        final int degrees = (int) d;
        final double remainder = d - degrees;
        final int minutes = (int) (remainder * 60D);
        int seconds = (int) (((remainder * 60D) - minutes) * 60D * 1000D);
        final String retVal = degrees + "/1," + minutes + "/1," + seconds + "/1000";
        return retVal;
    }

    ///////////////另一個寫的角度轉換寫的仔細的方法
    public static final String convertLatLongToString(double d) {
        final StringBuilder sb = new StringBuilder(20);
        d = Math.abs(d);
//        latitude=Math.abs(latitude);
        final int degree = (int) d;
        d *= 60;
        d -= (degree * 60.0d);
        final int minute = (int) d;
        d *= 60;
        d -= (minute * 60.0d);
        final int second = (int) (d * 1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }

    public static String makeLatStringRef(double lat) {
        ////N跟S代表南北半球
        return lat >= 0D ? "N" : "S";
    }

    public static String makeLonStringRef(double lon) {
        ////E跟W代表東西經
        return lon >= 0.0D ? "E" : "W";
    }
}
