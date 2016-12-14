package wal.fennel.common.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.SubLocation;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class SubLocationTable {

    public final static String TAG = "SubLocation";

    public static final String TABLE_SUB_LOCATION = "sublocation";

    public static final String COLUMN_SFDC_ID = "sfdc_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION_ID = "location_id";

    public static final String CREATE_TABLE_SUB_LOCATION = "CREATE TABLE " +
            TABLE_SUB_LOCATION + "(" +
            COLUMN_SFDC_ID + " text," +
            COLUMN_NAME + " text," +
            COLUMN_LOCATION_ID + " text )";

    public static long insert(DatabaseHelper dbHelper, SubLocation subLocation) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_SFDC_ID, subLocation.id);
        values.put(COLUMN_NAME, subLocation.name);
        values.put(COLUMN_LOCATION_ID, subLocation.locationId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;

        if (!subLocationsExits(dbHelper, subLocation.id)) {
            newRowId = db.insert(
                    TABLE_SUB_LOCATION,
                    null,
                    values);
//            Log.i(TAG, "Row inserted at ID: " + newRowId);
        }

        return newRowId;
    }

    public static boolean subLocationsExits(DatabaseHelper dbHelper, String locId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SUB_LOCATION + " WHERE " + COLUMN_SFDC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{locId});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public static ArrayList<SubLocation> getSubLocationsFromLocation(DatabaseHelper dbHelper, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<SubLocation> allLocations = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_SUB_LOCATION + " WHERE " + COLUMN_LOCATION_ID + " = ?";
        Cursor c = db.rawQuery(query, new String[]{id});

        if (c.moveToFirst()) {
            do {
                SubLocation location = new SubLocation(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2)
                );

                allLocations.add(location);
            } while (c.moveToNext());
        }
        c.close();

        return allLocations;
    }

    public static ArrayList<SubLocation> getAllSubLocations(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<SubLocation> allLocations = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM "
                + TABLE_SUB_LOCATION, null);

        if (c.moveToFirst()) {
            do {
                SubLocation location = new SubLocation(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2)
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
        db.execSQL("delete from " + TABLE_SUB_LOCATION);
        db.close();
    }
}
