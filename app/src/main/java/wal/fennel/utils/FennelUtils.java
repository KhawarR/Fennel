package wal.fennel.utils;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    }

    public static void appendFarmerLog(String [] data) throws IOException {
        File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filePath = rootPath + File.separator + PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.FARMER_LOGS_FILE_NAME;
        File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;
        // File exist
        if(f.exists() && !f.isDirectory()){
            mFileWriter = new FileWriter(filePath , true);
            writer = new CSVWriter(mFileWriter);
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));
        }
        writer.writeNext(data);
        writer.close();
    }
}
