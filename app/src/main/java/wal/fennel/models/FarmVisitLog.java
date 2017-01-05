package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Khawar on 3/1/2017.
 */

public class FarmVisitLog extends RealmObject implements Parcelable {

    private String farmVisitId = "";
    private String farmingTaskId = "";
    private boolean isDataDirty = false;

    public FarmVisitLog() {

    }

    public FarmVisitLog(String farmVisitId, String farmingTaskId, boolean isDataDirty) {
        this.farmVisitId = farmVisitId;
        this.farmingTaskId = farmingTaskId;
        this.isDataDirty = isDataDirty;
    }

    public FarmVisitLog(FarmVisitLog other) {
        this.farmVisitId = other.farmVisitId;
        this.farmingTaskId = other.farmingTaskId;
        this.isDataDirty = other.isDataDirty;
    }

    public void setAll(String farmVisitId, String farmingTaskId, boolean isDataDirty) {
        this.farmVisitId = farmVisitId;
        this.farmingTaskId = farmingTaskId;
        this.isDataDirty = isDataDirty;
    }

    public String getFarmVisitId() {
        return farmVisitId;
    }

    public void setFarmVisitId(String farmVisitId) {
        this.farmVisitId = farmVisitId;
    }

    public String getFarmingTaskId() {
        return farmingTaskId;
    }

    public void setFarmingTaskId(String farmingTaskId) {
        this.farmingTaskId = farmingTaskId;
    }

    public boolean isDataDirty() {
        return isDataDirty;
    }

    public void setDataDirty(boolean dataDirty) {
        isDataDirty = dataDirty;
    }

    public static final Creator<FarmVisitLog> CREATOR
            = new Creator<FarmVisitLog>() {
        public FarmVisitLog createFromParcel(Parcel in) {
            return new FarmVisitLog(in);
        }

        public FarmVisitLog[] newArray(int size) {
            return new FarmVisitLog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(farmVisitId);
        out.writeString(farmingTaskId);
        out.writeInt(isDataDirty ? 1 : 0);
    }

    private FarmVisitLog(Parcel in) {
        farmVisitId = in.readString();
        farmingTaskId = in.readString();
        isDataDirty = in.readInt() == 1 ? true : false;
    }
}
