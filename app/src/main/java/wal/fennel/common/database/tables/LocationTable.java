package wal.fennel.common.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.Location;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class LocationTable {

    public final static String TAG = "Location";

    public static final String TABLE_LOCATION = "location";

    public static final String COLUMN_SFDC_ID = "sfdc_id";
    public static final String COLUMN_NAME = "name";

    public static final String CREATE_TABLE_LOCATION = "CREATE TABLE " +
            TABLE_LOCATION + "(" +
            COLUMN_SFDC_ID + " text," +
            COLUMN_NAME + " text )";

    public static long insert(DatabaseHelper dbHelper, Location location) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_SFDC_ID, location.getId());
        values.put(COLUMN_NAME, location.getName());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;

        if (!locationExists(dbHelper, location.getId())) {
            newRowId = db.insert(
                    TABLE_LOCATION,
                    null,
                    values);
//            Log.i(TAG, "Row inserted at ID: " + newRowId);
        }

        return newRowId;
    }


    public static boolean locationExists(DatabaseHelper dbHelper, String locId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_SFDC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{locId});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public static ArrayList<Location> getAllLocations(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Location> allLocations = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM "
                + TABLE_LOCATION, null);

        if (c.moveToFirst()) {
            do {
                Location location = new Location(
                        c.getString(0),
                        c.getString(1)
                );

                allLocations.add(location);
            } while (c.moveToNext());
        }
        c.close();

        return allLocations;
    }

    public static void deleteAll(DatabaseHelper dbHelper)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.execSQL("delete from " + TABLE_LOCATION);
        db.close();
    }
}
