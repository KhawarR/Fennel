package tintash.fennel.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tintash.fennel.common.database.models.FarmerTable;
import tintash.fennel.models.Farmer;

/**
 * Created by Irfan Ayaz on 30-Sep-16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "NewLifePrintDB.db";

    private static DatabaseHelper sDataBaseHelper;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sDataBaseHelper == null) {
            sDataBaseHelper = new DatabaseHelper(context);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (sDataBaseHelper == null) {
            throw new IllegalStateException(DatabaseHelper.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sDataBaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FarmerTable.CREATE_TABLE_FARMER);

        Log.d(TAG, "onCreate called");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DATABASE_VERSION == newVersion) {

        }
        db.execSQL("DROP TABLE IF EXISTS " + FarmerTable.TABLE_FARMERS);

        onCreate(db);

        Log.d(TAG, "onUpgrade called");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //region Database Helper Methods
    public void insertFarmer(Farmer newFarmer, String id,  boolean synced) {

        long rowInserted = FarmerTable.insert(this, newFarmer, id,  synced);
        if (rowInserted != -1) {
            Log.i("FENNEL", "Row inserted!");
        }
    }
    //endregion
}
