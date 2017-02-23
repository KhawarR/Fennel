package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Khawar on 22/11/2016.
 */

public class LogTaskItem extends RealmObject implements Parcelable, Comparable<LogTaskItem> {

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
    private Date gpsTakenTime = null;
    private double latitude;
    private double longitude;
    private RealmList<TaskItemOption> options = new RealmList<>();
    private boolean isDataDirty = false;
    private boolean isPicUploadDirty = false;

    private String attachmentPath = "";
    private String attachmentId = "";
    private Date dateModified = null;
    private String agentName = "";
    private String farmerName = "";
    private String agentAttachmentId = "";
    private boolean isTaskDone = false;

    private String logbookMessage = "";

    public LogTaskItem(){

    }

    public LogTaskItem(LogTaskItem other) {
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

        this.attachmentPath = other.attachmentPath;
        this.attachmentId = other.attachmentId;
        this.dateModified = other.dateModified;
        this.agentName = other.agentName;
        this.farmerName = other.farmerName;
        this.agentAttachmentId = other.agentAttachmentId;
        this.isDataDirty = other.isDataDirty;
        this.isPicUploadDirty = other.isPicUploadDirty;

        this.logbookMessage = other.logbookMessage;
    }

    public LogTaskItem(TaskItem other) {
        this.sequence = other.getSequence();
        this.id = other.getId();
        this.farmingTaskId = other.getFarmingTaskId();
        this.name = other.getName();
        this.recordType = other.getRecordType();
        this.description = other.getDescription();
        this.textValue = other.getTextValue();
        this.fileType = other.getFileType();
        this.fileActionType = other.getFileActionType();
        this.fileActionPerformed = other.getFileActionPerformed();
        this.gpsTakenTime = other.getGpsTakenTime();
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.options = other.getOptions();
        this.isTaskDone = other.isTaskDone();

        this.attachmentPath = other.getAttachmentPath();
        this.attachmentId = other.getAttachmentId();
        this.dateModified = other.getDateModified();
        this.agentName = other.getAgentName();
        this.farmerName = other.getFarmerName();
        this.agentAttachmentId = other.getAgentAttachmentId();
        this.isDataDirty = other.isDataDirty();
        this.isPicUploadDirty = other.isPicUploadDirty();

        this.logbookMessage = other.getLogbookMessage();
    }

    public LogTaskItem(int sequence, String id, String farmingTaskId, String name, String recordType,
                       String description, String textValue, String fileType, String fileActionType,
                       String fileActionPerformed, Date gpsTakenTime, double latitude,
                       double longitude, RealmList<TaskItemOption> options, Date lastModified,
                       String agent, String farmer, String agentAttachmentId, boolean isTaskDone,
                       String attachmentPath, String attachmentId, boolean isDataDirty, boolean isPicUploadDirty, String logbookMessage) {
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

        this.attachmentPath = attachmentPath;
        this.attachmentId = attachmentId;
        this.dateModified = lastModified;
        this.agentName = agent;
        this.farmerName = farmer;
        this.agentAttachmentId = agentAttachmentId;
        this.isDataDirty = isDataDirty;
        this.isPicUploadDirty = isPicUploadDirty;

        this.logbookMessage = logbookMessage;
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
        dest.writeLong(this.gpsTakenTime == null ? 0 : gpsTakenTime.getTime());
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.isTaskDone ? 1 : 0);
        dest.writeTypedList(options);

        dest.writeString(this.attachmentPath);
        dest.writeString(this.attachmentId);
        dest.writeLong(dateModified == null ? 0 : dateModified.getTime());
        dest.writeString(this.agentName);
        dest.writeString(this.farmerName);
        dest.writeString(this.agentAttachmentId);
        dest.writeInt(this.isDataDirty ? 1 : 0);
        dest.writeInt(this.isPicUploadDirty ? 1 : 0);

        dest.writeString(this.logbookMessage);
    }

    protected LogTaskItem(Parcel in) {
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
        this.gpsTakenTime = new Date(in.readLong());
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.isTaskDone = in.readInt() == 1 ? true : false;
        options = new RealmList<>();
        in.readTypedList(options, TaskItemOption.CREATOR);

        this.attachmentPath = in.readString();
        this.attachmentId = in.readString();
        this.dateModified = new Date(in.readLong());
        this.agentName = in.readString();
        this.farmerName = in.readString();
        this.agentAttachmentId = in.readString();
        this.isDataDirty = in.readInt() == 1 ? true : false;
        this.isPicUploadDirty = in.readInt() == 1 ? true : false;

        this.logbookMessage = in.readString();
    }

    public static final Creator<LogTaskItem> CREATOR = new Creator<LogTaskItem>() {
        @Override
        public LogTaskItem createFromParcel(Parcel source) {
            return new LogTaskItem(source);
        }

        @Override
        public LogTaskItem[] newArray(int size) {
            return new LogTaskItem[size];
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

    public Date getGpsTakenTime() {
        return gpsTakenTime;
    }

    public void setGpsTakenTime(Date gpsTakenTime) {
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

    public boolean isDataDirty() {
        return isDataDirty;
    }

    public void setDataDirty(boolean dataDirty) {
        isDataDirty = dataDirty;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public boolean isPicUploadDirty() {
        return isPicUploadDirty;
    }

    public void setPicUploadDirty(boolean picUploadDirty) {
        isPicUploadDirty = picUploadDirty;
    }

    public String getLogbookMessage() {
        return logbookMessage;
    }

    public void setLogbookMessage(String logbookMessage) {
        this.logbookMessage = logbookMessage;
    }

    @Override
    public int compareTo(LogTaskItem another) {
        return this.dateModified.compareTo(another.dateModified);
    }
}
