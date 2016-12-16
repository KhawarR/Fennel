package wal.fennel.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    public static String getTimeAgo(long time) {
//        if (time < 1000000000000L) {
//            // if timestamp given in seconds, convert to millis
//            time *= 1000;
//        }

        long now = System.currentTimeMillis();
        int gmtOffset = TimeZone.getDefault().getRawOffset();

        now = now + gmtOffset;
        now = Math.round(now / 1000);
        time = time + gmtOffset;
        time = Math.round(time / 1000);

        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        final long diffMinutes = Math.round((diff / 60));

        int hours = 0;

        if (diff < 30) {
            return "Just now";
        } else if (diff < 60) {
            return diff + " seconds ago";
        } else if (diff < 120) {
            return "A minute ago";
        } else if (diffMinutes < 60) {
            return diffMinutes + " minutes ago";
        } else if (diffMinutes < 120) {
            return "An hour ago";
        } else if (diffMinutes < (24 * 60)) {
            hours = (int) Math.floor(diffMinutes / 60);
            return hours + " hours ago";
        } else if (diffMinutes < (24 * 60 * 2)) {
            return "Yesterday";
        } else if (diffMinutes < (24 * 60 * 7)) {
            hours = (int) Math.floor(diffMinutes / (60 * 24));
            return hours + " days ago";
        } else if (diffMinutes < (24 * 60 * 14)) {
            return "Last week";
        } else if (diffMinutes < (24 * 60 * 31)) {
            hours = (int) Math.floor(diffMinutes / (60 * 24 * 7));
            return hours + " weeks ago";
        } else if (diffMinutes < (24 * 60 * 61)) {
            return "Last month";
        } else if (diffMinutes < (24 * 60 * 365.25)) {
            hours = (int) Math.floor(diffMinutes / (60 * 24 * 30));
            return hours + " months ago";
        } else if (diffMinutes < (24 * 60 * 731)) {
            return "Last year";
        } else {
            hours = (int) Math.floor(diffMinutes / (60 * 24 * 365));
            return hours + " years ago";
        }
    }
}
