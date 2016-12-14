package wal.fennel.common.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.Village;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class VillageTable {

    public final static String TAG = "Village";

    public static final String TABLE_VILLAGE = "village";

    public static final String COLUMN_SFDC_ID = "sfdc_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SUB_LOCATION_ID = "sub_location_id";

    public static final String CREATE_TABLE_VILLAGE = "CREATE TABLE " +
            TABLE_VILLAGE + "(" +
            COLUMN_SFDC_ID + " text," +
            COLUMN_NAME + " text," +
            COLUMN_SUB_LOCATION_ID + " text )";

    public static long insert(DatabaseHelper dbHelper, Village village) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_SFDC_ID, village.id);
        values.put(COLUMN_NAME, village.name);
        values.put(COLUMN_SUB_LOCATION_ID, village.subLocationId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;

        if (!villageExits(dbHelper, village.id)) {
            newRowId = db.insert(
                    TABLE_VILLAGE,
                    null,
                    values);
//            Log.i(TAG, "Row inserted at ID: " + newRowId);
        }

        return newRowId;
    }

    public static boolean villageExits(DatabaseHelper dbHelper, String locId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VILLAGE + " WHERE " + COLUMN_SFDC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{locId});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public static ArrayList<Village> getVillagesFromSubLocation(DatabaseHelper dbHelper, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Village> allLocations = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_VILLAGE + " WHERE " + COLUMN_SUB_LOCATION_ID + " = ? OR " + COLUMN_SUB_LOCATION_ID + " = ''";
        Cursor c = db.rawQuery(query, new String[]{id});

        if (c.moveToFirst()) {
            do {
                Village location = new Village(
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

    public static ArrayList<Village> getAllVillages(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Village> allLocations = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM "
                + TABLE_VILLAGE, null);

        if (c.moveToFirst()) {
            do {
                Village location = new Village(
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
        db.execSQL("delete from " + TABLE_VILLAGE);
        db.close();
    }
}
