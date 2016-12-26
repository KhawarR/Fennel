package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by irfanayaz on 12/9/16.
 */

public class FieldAgent extends RealmObject implements Parcelable {

    public String name;
    public String phoneNumber;
    public String agentType;
    public String agentId;
    public String agentEmployeeId;
    public String agentAttachmentUrl;

    RealmList<TaskItem> visitLogs = new RealmList<>();
    public boolean isHeader = false;

    public FieldAgent() {

    }

    public FieldAgent(String name, String phoneNumber, String agentType, String agentId, String agentEmployeeId, String attachmentUrl, RealmList<TaskItem> visitLogs, boolean isHeader) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.agentType = agentType;
        this.agentId = agentId;
        this.agentEmployeeId = agentEmployeeId;
        this.agentAttachmentUrl = attachmentUrl;
        this.visitLogs = visitLogs;
        this.isHeader = isHeader;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setVisitLogs(RealmList<TaskItem> logs) {
        this.visitLogs = logs;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAgentType() {
        return agentType;
    }

    public RealmList<TaskItem> getVisitLogs() {
        return visitLogs;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public String getAgentAttachmentUrl() {
        return agentAttachmentUrl;
    }

    public void setAgentAttachmentUrl(String agentAttachmentUrl) {
        this.agentAttachmentUrl = agentAttachmentUrl;
    }

    public String getAgentEmployeeId() {
        return agentEmployeeId;
    }

    public void setAgentEmployeeId(String agentEmployeeId) {
        this.agentEmployeeId = agentEmployeeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.agentType);
        dest.writeString(this.agentId);
        dest.writeString(this.agentEmployeeId);
        dest.writeString(this.agentAttachmentUrl);
        dest.writeTypedList(this.visitLogs);
        dest.writeByte(this.isHeader ? (byte) 1 : (byte) 0);
    }

    protected FieldAgent(Parcel in) {
        this.name = in.readString();
        this.phoneNumber = in.readString();
        this.agentType = in.readString();
        this.agentId = in.readString();
        this.agentEmployeeId = in.readString();
        this.agentAttachmentUrl = in.readString();
        this.visitLogs = new RealmList<>();
        in.readTypedList(this.visitLogs, TaskItem.CREATOR);
        this.isHeader = in.readByte() != 0;
    }

    public static final Creator<FieldAgent> CREATOR = new Creator<FieldAgent>() {
        @Override
        public FieldAgent createFromParcel(Parcel source) {
            return new FieldAgent(source);
        }

        @Override
        public FieldAgent[] newArray(int size) {
            return new FieldAgent[size];
        }
    };
}
