package wal.fennel.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

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

    public static Date getLastModifiedDateFromString(String inDateValue, String dateFormat){
        if(inDateValue == null){
            return null;
        }
        String [] arrLastModifiedDate = inDateValue.split(Pattern.quote("."));
        String dateValue = inDateValue;
        if(arrLastModifiedDate.length > 0){
            dateValue = arrLastModifiedDate[0];
        }
        DateFormat df = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = df.parse(dateValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}
