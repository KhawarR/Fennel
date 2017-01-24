package wal.fennel.utils;

import android.content.Context;
import android.os.Environment;

import com.crashlytics.android.Crashlytics;
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
import java.util.List;
import java.util.regex.Pattern;

import wal.fennel.dropbox.DropboxClient;
import wal.fennel.dropbox.UploadTask;

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

    public static void uploadDebugLogFile(Context context){

        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(downloadsDirectory.exists()){
            String downloadDirPath = downloadsDirectory.getAbsolutePath();
            String debugLogFileName = PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.DEBUG_LOGS_FILE_NAME;

            File debugLogsFile = new File(downloadDirPath + File.separator + debugLogFileName);
            if(debugLogsFile.exists()) {
                uploadDropboxFile(context, debugLogsFile, Constants.DropboxConstants.DEBUG_LOGS_DROPBOX_PATH);
            }
            else {
                Crashlytics.logException(new Throwable("Debug log file doesn't exist - " + PreferenceHelper.getInstance().readUserId()));
            }
        }
        else {
            Crashlytics.logException(new Throwable("Download directory doesn't exist"));
        }
    }

    public static void uploadFarmerLogFile(Context context){

        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(downloadsDirectory.exists()){
            String downloadDirPath = downloadsDirectory.getAbsolutePath();
            String farmerLogFileName = PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.FARMER_LOGS_FILE_NAME;

            File farmerLogsFile = new File(downloadDirPath + File.separator + farmerLogFileName);
            if(farmerLogsFile.exists()) {
                uploadDropboxFile(context, farmerLogsFile, Constants.DropboxConstants.FARMER_LOGS_DROPBOX_PATH);
            }
            else {
                Crashlytics.logException(new Throwable("Farmer log file doesn't exist - " + PreferenceHelper.getInstance().readUserId()));
            }
        }
        else {
            Crashlytics.logException(new Throwable("Download directory doesn't exist"));
        }
    }

    private static void uploadDropboxFile(Context context, File file, String fileDropboxPath){
        new UploadTask(DropboxClient.getClient(Constants.DropboxConstants.ACCESS_TOKEN), file, context, fileDropboxPath).execute();
    }
}
