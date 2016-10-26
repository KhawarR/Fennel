package tintash.fennel.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import tintash.fennel.common.database.tables.FarmTable;
import tintash.fennel.common.database.tables.FarmerTable;
import tintash.fennel.common.database.tables.LocationTable;
import tintash.fennel.common.database.tables.SubLocationTable;
import tintash.fennel.common.database.tables.TreeTable;
import tintash.fennel.common.database.tables.VillageTable;
import tintash.fennel.models.Farm;
import tintash.fennel.models.Farmer;
import tintash.fennel.models.Location;
import tintash.fennel.models.SubLocation;
import tintash.fennel.models.Tree;
import tintash.fennel.models.Village;

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
        db.execSQL(FarmTable.CREATE_TABLE_FARM);
        db.execSQL(LocationTable.CREATE_TABLE_LOCATION);
        db.execSQL(SubLocationTable.CREATE_TABLE_SUB_LOCATION);
        db.execSQL(VillageTable.CREATE_TABLE_VILLAGE);
        db.execSQL(TreeTable.CREATE_TABLE_TREE);

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
            Log.i("FENNEL", "Farmer inserted!");
        }
    }

    public void updateFarmer(Farmer newFarmer, boolean synced) {
        long rowsUpdated = FarmerTable.update(this, newFarmer,  synced);
        if (rowsUpdated != -1) {
            Log.i("FENNEL", "Farmer updated!");
        }
    }

    public void insertFarm(Farm newFarm, String id, boolean synced) {

        long rowInserted = FarmTable.insert(this, newFarm, id,  synced);
        if (rowInserted != -1) {
            Log.i("FENNEL", "Farm inserted!");
        }
    }

    public void updateFarm(Farm newFarm, boolean synced) {
        long rowsUpdated = FarmTable.update(this, newFarm,  synced);
        if (rowsUpdated != -1) {
            Log.i("FENNEL", "Farm udpated!");
        }
    }

    public ArrayList<Location> getAllLocations()
    {
        return LocationTable.getAllLocations(this);
    }

    public ArrayList<SubLocation> getAllSubLocations()
    {
        return SubLocationTable.getAllSubLocations(this);
    }

    public ArrayList<SubLocation> getSubLocationsFromLocation(String id)
    {
        return SubLocationTable.getSubLocationsFromLocation(this, id);
    }

    public ArrayList<Village> getAllVillages()
    {
        return VillageTable.getAllVillages(this);
    }

    public ArrayList<Village> getVillagesFromSubLocation(String id)
    {
        return VillageTable.getVillagesFromSubLocation(this, id);
    }

    public ArrayList<Tree> getAllTrees()
    {
        return TreeTable.getAllTrees(this);
    }

    public ArrayList<Tree> getTreesFromSubLocation(String id)
    {
        return TreeTable.getTreesFromSubLocation(this, id);
    }

    public void insertLocation(Location newLocation) {
        long rowInserted = LocationTable.insert(this, newLocation);
        if (rowInserted != -1) {
            Log.i("FENNEL", "Location inserted!");
        }
    }

    public void insertSubLocation(SubLocation newSubLocation) {
        long rowInserted = SubLocationTable.insert(this, newSubLocation);
        if (rowInserted != -1) {
            Log.i("FENNEL", "SubLocation inserted!");
        }
    }

    public void inserVillage(Village newVillage) {
        long rowInserted = VillageTable.insert(this, newVillage);
        if (rowInserted != -1) {
            Log.i("FENNEL", "Village inserted!");
        }
    }

    public void insertTree(Tree newTree) {
        long rowInserted = TreeTable.insert(this, newTree);
        if (rowInserted != -1) {
            Log.i("FENNEL", "Tree inserted!");
        }
    }

    //endregion
}
