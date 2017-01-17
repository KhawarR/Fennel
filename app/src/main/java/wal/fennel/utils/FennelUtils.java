package wal.fennel.utils;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;
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

    public static void appendDebugLog(String text) {
        text = getFormattedTime(System.currentTimeMillis(), Constants.STR_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS) + " - " + text;

        File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logFile = new File(rootPath.getPath() + File.separator + PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.DEBUG_LOGS_FILE_NAME);

        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("FennelSyncLog", text);
    }

    public static void appendFarmerLog(String [] data) throws IOException {
        String [] dataHeader = new String[]{"Timestamp", "Username", "UserID", "RecordType",
                "ID Number", "Full name", "First Name", "Middle Name", "Last Name", "Location",
                "SubLocation", "Village", "Tree Specie", "Mobile", "Is Leader?", "Is Farmer Home?",
                "Farmer Photo", "Farmer National ID Photo", "Farmer ID", "Location ID",
                "Sub Location ID", "Village ID", "Tree specie ID"};

        File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filePath = rootPath + File.separator + PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.FARMER_LOGS_FILE_NAME;
        File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;

        // File exist
        if(f.exists() && !f.isDirectory()){

            CSVReader reader = new CSVReader(new FileReader(filePath));
            List myEntries = reader.readAll();
            reader.close();

            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);

            if(myEntries.size() > 0) {
                String [] arrLastEntry = (String[]) myEntries.get(myEntries.size() - 1);
                if(arrLastEntry.length != dataHeader.length) {
                    writer.writeNext(dataHeader);
                }
            }

            writer.writeNext(data);
            writer.close();
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));
            writer.writeNext(dataHeader);
            writer.close();
            appendFarmerLog(data);
        }
    }

    public static Date getDateFromString(String dateStr) {

        Date date = null;
        if (dateStr != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (dateStr != null && !dateStr.equalsIgnoreCase("null"))
                    date= dateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
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

    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static String getMonthString(int monthIndex) {
        String monthStr = null;

        switch (monthIndex) {
            case 0:
                monthStr = "JAN";
                break;
            case 1:
                monthStr = "FEB";
                break;
            case 2:
                monthStr = "MAR";
                break;
            case 3:
                monthStr = "APR";
                break;
            case 4:
                monthStr = "MAY";
                break;
            case 5:
                monthStr = "JUN";
                break;
            case 6:
                monthStr = "JUL";
                break;
            case 7:
                monthStr = "AUG";
                break;
            case 8:
                monthStr = "SEP";
                break;
            case 9:
                monthStr = "OCT";
                break;
            case 10:
                monthStr = "NOV";
                break;
            case 11:
                monthStr = "DEC";
                break;
        }
        return monthStr;
    }
}
