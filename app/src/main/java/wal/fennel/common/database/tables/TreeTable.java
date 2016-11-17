package wal.fennel.common.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.Tree;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class TreeTable {

    public final static String TAG = "Tree";

    public static final String TABLE_TREE = "tree";

    public static final String COLUMN_SFDC_ID = "sfdc_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SUB_LOCATION_ID = "sub_location_id";

    public static final String CREATE_TABLE_TREE = "CREATE TABLE " +
            TABLE_TREE + "(" +
            COLUMN_SFDC_ID + " text," +
            COLUMN_NAME + " text," +
            COLUMN_SUB_LOCATION_ID + " text )";

    public static long insert(DatabaseHelper dbHelper, Tree tree) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_SFDC_ID, tree.id);
        values.put(COLUMN_NAME, tree.name);
        values.put(COLUMN_SUB_LOCATION_ID, tree.subLocationId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;

        if (!treeExists(dbHelper, tree.id, tree.subLocationId)) {
            newRowId = db.insert(
                    TABLE_TREE,
                    null,
                    values);
//            Log.i(TAG, "Row inserted at ID: " + newRowId);
        }

        return newRowId;
    }

    public static boolean treeExists(DatabaseHelper dbHelper, String treeId, String subLocaId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TREE + " WHERE " + COLUMN_SFDC_ID + " = ? AND " + COLUMN_SUB_LOCATION_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{treeId, subLocaId});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public static ArrayList<Tree> getAllTrees(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Tree> allTrees = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM "
                + TABLE_TREE, null);

        if (c.moveToFirst()) {
            do {
                Tree tree = new Tree(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2)
                );

                allTrees.add(tree);
            } while (c.moveToNext());
        }
        c.close();

        return allTrees;
    }

    public static ArrayList<Tree> getTreesFromSubLocation(DatabaseHelper dbHelper, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Tree> allLocations = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_TREE + " WHERE " + COLUMN_SUB_LOCATION_ID + " = ?";
        Cursor c = db.rawQuery(query, new String[]{id});

        if (c.moveToFirst()) {
            do {
                Tree location = new Tree(
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
}
