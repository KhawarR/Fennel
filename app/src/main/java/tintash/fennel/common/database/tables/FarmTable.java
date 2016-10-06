package tintash.fennel.common.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import tintash.fennel.common.database.DatabaseHelper;
import tintash.fennel.models.Farm;
import tintash.fennel.models.Farmer;

/**
 * Created by irfanayaz on 10/6/16.
 */
public class FarmTable {

    public final static String TAG = "Farm";

    public static final String TABLE_FARM = "farm";

    public static final String COLUMN_SFDC_ID = "sfdc_id";
    public static final String COLUMN_FACILITATOR_ID = "facilitator_id";
    public static final String COLUMN_FARMER_ID = "farmer_id";
    public static final String COLUMN_FARMER_ENROLLMENT_DATE = "farmer_enrollment_date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_SUB_LOCATION = "sub_location";
    public static final String COLUMN_VILLAGE_NAME = "village_name";
    public static final String COLUMN_TREE_SPECIES = "tree_species";
    public static final String COLUMN_SYNCED = "synced";

    public static final String CREATE_TABLE_FARM = "CREATE TABLE " +
            TABLE_FARM + "(" +
            COLUMN_SFDC_ID + " text," +
            COLUMN_FACILITATOR_ID + " text," +
            COLUMN_FARMER_ID + " text," +
            COLUMN_FARMER_ENROLLMENT_DATE + " text," +
            COLUMN_LOCATION + " text," +
            COLUMN_SUB_LOCATION + " text," +
            COLUMN_VILLAGE_NAME + " text," +
            COLUMN_TREE_SPECIES + " text," +
            COLUMN_SYNCED + " integer" + ")";


    public static long insert(DatabaseHelper dbHelper, Farm farm, String id, boolean synced) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        if (id != null) {
            values.put(COLUMN_SFDC_ID, id);
        }
        values.put(COLUMN_FACILITATOR_ID, farm.facilitatorId);
        values.put(COLUMN_FARMER_ID, farm.farmerId);
        values.put(COLUMN_FARMER_ENROLLMENT_DATE, farm.farmerEnrollmentDate);
        values.put(COLUMN_LOCATION, farm.location);
        values.put(COLUMN_SUB_LOCATION, farm.subLocation);
        values.put(COLUMN_VILLAGE_NAME, farm.villageName);
        values.put(COLUMN_TREE_SPECIES, farm.treeSpecies);
        values.put(COLUMN_SYNCED, synced);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;

        if (!farmExists(dbHelper, id)) {
            newRowId = db.insert(
                    TABLE_FARM,
                    null,
                    values);
            Log.i(TAG, "Row inserted at ID: " + newRowId);
        }

        return newRowId;
    }


    public static boolean farmExists(DatabaseHelper dbHelper, String idNum) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FARM + " WHERE " + COLUMN_SFDC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{idNum});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public int update(Farmer farmer) {

//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_FOLLOWER_USERNAME, user.username);
//        values.put(COLUMN_FOLLOWER_BOTH, user.both);
//        values.put(COLUMN_FOLLOWER_NAME, user.name);
//        values.put(COLUMN_FOLLOWER_RELATIONSHIP, user.relationship);
//        values.put(COLUMN_FOLLOWER_CREATED_AT, user.createdAt);
//        values.put(COLUMN_FOLLOWER_PROFILE_PHOTO, user.profilephoto);
//        values.put(COLUMN_FOLLOWER_EMAIL, user.email);
//        values.put(COLUMN_FOLLOWER_COVER_PHOTO, user.coverphoto);
//        values.put(COLUMN_FOLLOWER_CREATED_AT, user.createdAt);
//        values.put(COLUMN_FOLLOWER_NOTIFY, user.notify);
//        values.put(COLUMN_FOLLOWER_KEY, user.key);
//        values.put(COLUMN_FOLLOWER_ASK, user.ask);
//        values.put(COLUMN_FOLLOWER_FOLLOW, user.follow);
//        values.put(COLUMN_FOLLOWER_THUMBNAIL, user.thumbnail);

//        // Which row to update, based on the email
//        String selection = COLUMN_FOLLOWER_USERNAME + " = ?";
//        String[] selectionArgs = {user.username};
//
//        int count = db.update(
//                TABLE_FOLLOWER,
//                values,
//                selection,
//                selectionArgs);
//
//        Log.i(TAG, "Rows updated: " + count);
//        return count;

        return 0;
    }

//    public int delete(String username) {
//
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Define 'where' part of query.
//        String selection = COLUMN_FOLLOWER_USERNAME + " = ?";
//        // Specify arguments in placeholder order.
//        String[] selectionArgs = {username};
//        // Issue SQL statement.
//        int count = db.delete(TABLE_FOLLOWER, selection, selectionArgs);
//        Log.i(TAG, "Rows deleted: " + count);
//        return count;
//    }
//
//    public void deleteAll() {
//
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.execSQL("delete from " + TABLE_FOLLOWER);
//        db.close();
//        Log.i(TAG, "All Rows deleted from FoolowTable: ");
//    }
//
//    public User getFollower(String username) {
//
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        User user = null;
//
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                COLUMN_FOLLOWER_ID,
//                COLUMN_FOLLOWER_USERNAME,
//                COLUMN_FOLLOWER_BOTH,
//                COLUMN_FOLLOWER_NAME,
//                COLUMN_FOLLOWER_RELATIONSHIP,
//                COLUMN_FOLLOWER_CREATED_AT,
//                COLUMN_FOLLOWER_PROFILE_PHOTO,
//                COLUMN_FOLLOWER_EMAIL,
//                COLUMN_FOLLOWER_COVER_PHOTO,
//                COLUMN_FOLLOWER_NOTIFY,
//                COLUMN_FOLLOWER_KEY,
//                COLUMN_FOLLOWER_ASK,
//                COLUMN_FOLLOWER_FOLLOW,
//                COLUMN_FOLLOWER_THUMBNAIL
//        };
//
//        String selection = COLUMN_FOLLOWER_USERNAME + "= ?";
//        String[] selectionArgs = new String[]{
//                username
//        };
//
//        // How you want the results sorted in the resulting Cursor
//        String sortOrder =
//                COLUMN_FOLLOWER_CREATED_AT + " DESC";
//
//        Cursor c = db.query(
//                TABLE_FOLLOWER,  // The table to query
//                projection,                               // The columns to return
//                selection,                                // The columns for the WHERE clause
//                selectionArgs,                            // The values for the WHERE clause
//                null,                                     // don't group the rows
//                null,                                     // don't filter by row groups
//                null                                 // The sort order
//        );
//
//        if (c.moveToFirst()) {
//            do {
//                user = new User(
//                        c.getString(1),
//                        (c.getInt(2) != 0),
//                        c.getString(3),
//                        c.getString(4),
//
//                        c.getInt(5),
//                        c.getString(6),
//                        c.getString(7),
//                        c.getString(8),
//
//                        c.getString(9),
//                        c.getString(10),
//                        (c.getInt(11) != 0),
//                        c.getString(12),
//                        c.getString(13)
//                );
//                // do what ever you want here
//            } while (c.moveToNext());
//        }
//        c.close();
//
//        return user;
//    }
//
//    public ArrayList<User> getAllFollowers() {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        ArrayList<User> allFollowers = new ArrayList<>();
//
//        Cursor c = db.rawQuery("SELECT * FROM "
//                + TABLE_FOLLOWER, null);
//
//        if (c.moveToFirst()) {
//            do {
//                User user = new User(
//                        c.getString(1),
//                        (c.getInt(2) != 0),
//                        c.getString(3),
//                        c.getString(4),
//
//                        c.getInt(5),
//                        c.getString(6),
//                        c.getString(7),
//                        c.getString(8),
//
//                        c.getString(9),
//                        c.getString(10),
//                        (c.getInt(11) != 0),
//                        c.getString(12),
//                        c.getString(13)
//                );
//
//                allFollowers.add(user);
//            } while (c.moveToNext());
//        }
//        c.close();
//
//        return allFollowers;
//
//    }

}

