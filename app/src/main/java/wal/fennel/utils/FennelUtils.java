package wal.fennel.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
