package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by irfanayaz on 12/29/16.
 */

public class DashboardFieldAgent extends RealmObject implements Parcelable {

    String agentName;
    String agentNumber;
    String agentType;
    String agentId;
    String agentEmployeeId;
    String agentAttachmentUrl;

    RealmList<DashboardTask> dashboardTasks = new RealmList<>();

    public boolean isHeader = false;

    public DashboardFieldAgent() {}

    public DashboardFieldAgent(String agentName, String agentNumber, String agentType, String agentId, String agentEmployeeId, String agentAttachmentUrl, RealmList<DashboardTask> dashboardTasks, boolean header) {
        this.agentName = agentName;
        this.agentNumber = agentNumber;
        this.agentType = agentType;
        this.agentId = agentId;
        this.agentEmployeeId = agentEmployeeId;
        this.agentAttachmentUrl = agentAttachmentUrl;
        this.dashboardTasks = dashboardTasks;
        this.isHeader = header;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentNumber() {
        return agentNumber;
    }

    public void setAgentNumber(String agentNumber) {
        this.agentNumber = agentNumber;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentEmployeeId() {
        return agentEmployeeId;
    }

    public void setAgentEmployeeId(String agentEmployeeId) {
        this.agentEmployeeId = agentEmployeeId;
    }

    public String getAgentAttachmentUrl() {
        return agentAttachmentUrl;
    }

    public void setAgentAttachmentUrl(String agentAttachmentUrl) {
        this.agentAttachmentUrl = agentAttachmentUrl;
    }

    public RealmList<DashboardTask> getDashboardTasks() {
        return dashboardTasks;
    }

    public void setDashboardTasks(RealmList<DashboardTask> dashboardTasks) {
        this.dashboardTasks = dashboardTasks;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.agentName);
        dest.writeString(this.agentNumber);
        dest.writeString(this.agentType);
        dest.writeString(this.agentId);
        dest.writeString(this.agentEmployeeId);
        dest.writeString(this.agentAttachmentUrl);
        dest.writeList(this.dashboardTasks);
        dest.writeByte(this.isHeader ? (byte) 1 : (byte) 0);
    }

    protected DashboardFieldAgent(Parcel in) {
        this.agentName = in.readString();
        this.agentNumber = in.readString();
        this.agentType = in.readString();
        this.agentId = in.readString();
        this.agentEmployeeId = in.readString();
        this.agentAttachmentUrl = in.readString();
        this.dashboardTasks = new RealmList<>();
        in.readList(this.dashboardTasks, DashboardTask.class.getClassLoader());
        this.isHeader = in.readByte() != 0;
    }

    public static final Creator<DashboardFieldAgent> CREATOR = new Creator<DashboardFieldAgent>() {
        @Override
        public DashboardFieldAgent createFromParcel(Parcel source) {
            return new DashboardFieldAgent(source);
        }

        @Override
        public DashboardFieldAgent[] newArray(int size) {
            return new DashboardFieldAgent[size];
        }
    };
}
