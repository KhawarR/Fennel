package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Khawar on 22/11/2016.
 */

public class TaskItem extends RealmObject implements Parcelable {

    private int sequence;
    private String id = "";
    private String farmingTaskId = "";
    private String name = "";
    private String recordType = "";
    private String description = "";
    private String textValue = "";
    private String fileType = "";
    private String fileActionType = "";
    private String fileActionPerformed = "";
    private String gpsTakenTime = "";
    private double latitude;
    private double longitude;
    private RealmList<TaskItemOption> options = new RealmList<>();

    private String attachmentPath = "";
    private Date dateModified = null;
    private String agentName = "";
    private String farmerName = "";
    private String agentAttachmentId = "";
    private boolean isTaskDone = false;

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
        this.fileActionType = other.fileActionType;
        this.fileActionPerformed = other.fileActionPerformed;
        this.gpsTakenTime = other.gpsTakenTime;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.options = other.options;
        this.isTaskDone = other.isTaskDone;

        this.dateModified = other.dateModified;
        this.agentName = other.agentName;
        this.farmerName = other.farmerName;
        this.agentAttachmentId = other.agentAttachmentId;
    }

    public TaskItem(int sequence, String id, String farmingTaskId, String name, String recordType, String description, String textValue, String fileType, String fileActionType, String fileActionPerformed, String gpsTakenTime, double latitude, double longitude, RealmList<TaskItemOption> options, Date lastModified, String agent, String farmer, String attachmentId, boolean isTaskDone) {
        this.sequence = sequence;
        this.id = id;
        this.farmingTaskId = farmingTaskId;
        this.name = name;
        this.recordType = recordType;
        this.description = description;
        this.textValue = textValue;
        this.fileType = fileType;
        this.fileActionType = fileActionType;
        this.fileActionPerformed = fileActionPerformed;
        this.gpsTakenTime = gpsTakenTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.options = options;
        this.isTaskDone = isTaskDone;

        this.dateModified = lastModified;
        this.agentName = agent;
        this.farmerName = farmer;
        this.agentAttachmentId = attachmentId;
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
        dest.writeString(this.fileActionType);
        dest.writeString(this.fileActionPerformed);
        dest.writeString(this.gpsTakenTime);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.isTaskDone ? 1 : 0);
        dest.writeTypedList(options);

        dest.writeLong(dateModified == null ? 0 : dateModified.getTime());
        dest.writeString(this.agentName);
        dest.writeString(this.farmerName);
        dest.writeString(this.agentAttachmentId);

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
        this.fileActionType = in.readString();
        this.fileActionPerformed = in.readString();
        this.gpsTakenTime = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.isTaskDone = in.readInt() == 1 ? true : false;
        options = new RealmList<>();
        in.readTypedList(options, TaskItemOption.CREATOR);

        this.dateModified = new Date(in.readLong());
        this.agentName = in.readString();
        this.farmerName = in.readString();
        this.agentAttachmentId = in.readString();
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

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getAgentAttachmentId() {
        return agentAttachmentId;
    }

    public void setAgentAttachmentId(String attachmentId) {
        this.agentAttachmentId = attachmentId;
    }

    public boolean isTaskDone() {
        return isTaskDone;
    }

    public void setTaskDone(boolean taskDone) {
        isTaskDone = taskDone;
    }

    public String getFileActionType() {
        return fileActionType;
    }

    public void setFileActionType(String fileActionType) {
        this.fileActionType = fileActionType;
    }

    public String getFileActionPerformed() {
        return fileActionPerformed;
    }

    public void setFileActionPerformed(String fileActionPerformed) {
        this.fileActionPerformed = fileActionPerformed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}
