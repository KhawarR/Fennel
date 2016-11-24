package wal.fennel.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Khawar on 14/11/2016.
 */
public class FennelUtils {

    public static String getFormattedTime(long time, String format){
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat(format);
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    public static long getTimeInMillis(String time, String format) throws ParseException {
        Date date = new SimpleDateFormat(format, Locale.ENGLISH).parse(time);
        long milliseconds = date.getTime();
        return milliseconds;
    }

    public static String getFormattedTime(String time, String inFormat, String outFormat) throws ParseException {
        Date date = new SimpleDateFormat(inFormat, Locale.ENGLISH).parse(time);
        long milliseconds = date.getTime();

        Date dateStr = new Date(milliseconds);
        DateFormat formatter = new SimpleDateFormat(outFormat);
        String dateFormatted = formatter.format(dateStr);
        return dateFormatted;
    }
}
