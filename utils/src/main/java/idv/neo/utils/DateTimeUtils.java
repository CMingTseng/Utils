package idv.neo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Neo on 2015/10/3.
 */

public class DateTimeUtils {
    private static final String TAG = DateTimeUtils.class.getSimpleName();

    private final static SimpleDateFormat getSpecificDateFormat(String datetimeformat, Locale locale) {
        if (datetimeformat == null) {
            datetimeformat = "yyyy-MM-dd HH:mm:ss";
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return new SimpleDateFormat(datetimeformat, locale);
    }

    public static Calendar getStringToCalendar(String datetimeformat, Locale locale, String inputdatetime) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(getStringDateTimeToDate(datetimeformat, locale, inputdatetime));
        return cal;
    }

    public static boolean getDateTimeSinceBeforeDateTimeTo(String datetimeformat, Locale locale, String firstDate, String secondDate) {
        Calendar mFisrtDateTime = getStringToCalendar(datetimeformat, locale, firstDate);
        Calendar mSecondDateTime = getStringToCalendar(datetimeformat, locale, secondDate);

        if (mFisrtDateTime.equals(mSecondDateTime))
            return false;
        return mFisrtDateTime.before(mSecondDateTime);
    }

    public static Date getStringDateTimeToDate(String datetimeformat, Locale locale, String inputdatetime) {
        try {
            return getSpecificDateFormat(datetimeformat, locale).parse(inputdatetime);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static Date getStringDateTimeToISO8601Date(String datetimeformat, Locale locale, String inputdatetime) {
        if (!datetimeformat.contains("T") | !datetimeformat.contains("Z")) {
//            datetimeformat = datetimeformat.replace("dd","dd'T'");
//            datetimeformat = datetimeformat.replace("ss","ss'Z'");
            datetimeformat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        }
        return getStringDateTimeToDate(datetimeformat, locale, inputdatetime);
    }

    public static long getStringDateTimeTolong(String datetimeformat, Locale locale, String inputdatetime) {
        return getStringDateTimeToDate(datetimeformat, locale, inputdatetime).getTime();
    }

    public static Date getCurrentDate() {
        return (Calendar.getInstance()).getTime();
    }

    public static long getCurrentDatelong() {
        return getCurrentDate().getTime();
    }

    public static String getDateTimeToString(Date input, SimpleDateFormat trsformat) {
        if (trsformat == null) {
            trsformat = getSpecificDateFormat(null, null);
        }
        return trsformat.format(input);
    }

    public static String getlongTimeToString(long input, SimpleDateFormat trsformat) {
        return getDateTimeToString(new Date(input), trsformat);
    }
}
