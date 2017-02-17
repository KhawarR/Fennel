package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Khawar on 3/1/2017.
 */

public class FarmVisitLog extends RealmObject implements Parcelable {

    private String farmVisitId = "";
    private String farmingTaskId = "";
    private String taskItemId = "";
    private Date taskModifiedTime = null;
    private String logMessage = "";
    private boolean isDataDirty = false;


    public FarmVisitLog() {

    }

    public FarmVisitLog(String farmVisitId, String farmingTaskId, String taskItemId, Date modified, String message, boolean isDataDirty) {
        this.farmVisitId = farmVisitId;
        this.farmingTaskId = farmingTaskId;
        this.taskItemId = taskItemId;
        this.taskModifiedTime = modified;
        this.logMessage = message;
        this.isDataDirty = isDataDirty;
    }

    public FarmVisitLog(FarmVisitLog other) {
        this.farmVisitId = other.farmVisitId;
        this.farmingTaskId = other.farmingTaskId;
        this.taskItemId = other.taskItemId;
        this.taskModifiedTime = other.taskModifiedTime;
        this.logMessage = other.logMessage;
        this.isDataDirty = other.isDataDirty;
    }

    public void setAll(String farmVisitId, String farmingTaskId, String taskItemId, Date modified, String message, boolean isDataDirty) {
        this.farmVisitId = farmVisitId;
        this.farmingTaskId = farmingTaskId;
        this.taskItemId = taskItemId;
        this.taskModifiedTime = modified;
        this.logMessage = message;
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

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getTaskItemId() {
        return taskItemId;
    }

    public void setTaskItemId(String taskItemId) {
        this.taskItemId = taskItemId;
    }

    public Date getTaskModifiedTime() {
        return taskModifiedTime;
    }

    public void setTaskModifiedTime(Date taskModifiedTime) {
        this.taskModifiedTime = taskModifiedTime;
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
