package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Khawar on 22/11/2016.
 */

public class TaskItem extends RealmObject implements Parcelable {

    public int sequence;
    public String id = "";
    public String farmingTaskId = "";
    public String name = "";
    public String recordType = "";
    public String description = "";
    public String textValue = "";
    public String fileType = "";
    public String gpsTakenTime = "";
    public double latitude;
    public double longitude;
    public RealmList<TaskItemOption> options = new RealmList<>();

    public TaskItem(){

    }

    public TaskItem(TaskItem other) {
        this.sequence = other.sequence;
        this.id = other.id;
        this.farmingTaskId = other.farmingTaskId;
        this.name = other.name;
        this.recordType = other.recordType;
        this.description = other.description;
        this.textValue = other.textValue;
        this.fileType = other.fileType;
        this.gpsTakenTime = other.gpsTakenTime;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.options = other.options;
    }

    public TaskItem(int sequence, String id, String farmingTaskId, String name, String recordType, String description, String textValue, String fileType, String gpsTakenTime, double latitude, double longitude, RealmList<TaskItemOption> options) {
        this.sequence = sequence;
        this.id = id;
        this.farmingTaskId = farmingTaskId;
        this.name = name;
        this.recordType = recordType;
        this.description = description;
        this.textValue = textValue;
        this.fileType = fileType;
        this.gpsTakenTime = gpsTakenTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.options = options;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.sequence);
        dest.writeString(this.id);
        dest.writeString(this.farmingTaskId);
        dest.writeString(this.name);
        dest.writeString(this.recordType);
        dest.writeString(this.description);
        dest.writeString(this.textValue);
        dest.writeString(this.fileType);
        dest.writeString(this.gpsTakenTime);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeTypedList(options);
    }

    protected TaskItem(Parcel in) {
        this.sequence = in.readInt();
        this.id = in.readString();
        this.farmingTaskId = in.readString();
        this.name = in.readString();
        this.recordType = in.readString();
        this.description = in.readString();
        this.textValue = in.readString();
        this.fileType = in.readString();
        this.gpsTakenTime = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        options = new RealmList<>();
        in.readTypedList(options, TaskItemOption.CREATOR);
    }

    public static final Parcelable.Creator<TaskItem> CREATOR = new Parcelable.Creator<TaskItem>() {
        @Override
        public TaskItem createFromParcel(Parcel source) {
            return new TaskItem(source);
        }

        @Override
        public TaskItem[] newArray(int size) {
            return new TaskItem[size];
        }
    };

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmingTaskId() {
        return farmingTaskId;
    }

    public void setFarmingTaskId(String farmingTaskId) {
        this.farmingTaskId = farmingTaskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGpsTakenTime() {
        return gpsTakenTime;
    }

    public void setGpsTakenTime(String gpsTakenTime) {
        this.gpsTakenTime = gpsTakenTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public RealmList<TaskItemOption> getOptions() {
        return options;
    }

    public void setOptions(RealmList<TaskItemOption> options) {
        this.options = options;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
