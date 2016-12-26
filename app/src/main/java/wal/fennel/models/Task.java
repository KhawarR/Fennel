package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by irfanayaz on 11/16/16.
 */
public class Task extends RealmObject implements Parcelable {

    private String taskId;
    private String name;
    private String startedDate;
    private String completionDate;
    private String dueDate;
    private String status;
    private boolean isHeader;
    private RealmList<TaskItem> taskItems = new RealmList<>();

    public String agentId;
    public String agentName;
    public String agentPhoneNumber;
    public String agentEmployeeId;
    public String agentType;
    public String farmerName;
    public String shambaName;

    public Task(){

    }

    public Task(String taskId, String name, String startedDate, String completionDate, String dueDate, String status, boolean isHeader, RealmList<TaskItem> taskItems) {
        this.taskId = taskId;
        this.name = name;
        this.startedDate = startedDate;
        this.completionDate = completionDate;
        this.dueDate = dueDate;
        this.status = status;
        this.isHeader = isHeader;
        this.taskItems = taskItems;
    }

    public Task(Task other) {
        this.taskId = other.taskId;
        this.name = other.name;
        this.startedDate = other.startedDate;
        this.completionDate = other.completionDate;
        this.dueDate = other.dueDate;
        this.status = other.status;
        this.isHeader = other.isHeader;
        this.taskItems = other.taskItems;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(String startedDate) {
        this.startedDate = startedDate;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public RealmList<TaskItem> getTaskItems() {
        return taskItems;
    }

    public void setTaskItems(RealmList<TaskItem> taskItems) {
        this.taskItems = taskItems;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agenntId) {
        this.agentId = agenntId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentPhoneNumber() {
        return agentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        this.agentPhoneNumber = agentPhoneNumber;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getShambaName() {
        return shambaName;
    }

    public void setShambaName(String shambaName) {
        this.shambaName = shambaName;
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
        dest.writeString(this.taskId);
        dest.writeString(this.name);
        dest.writeString(this.startedDate);
        dest.writeString(this.completionDate);
        dest.writeString(this.dueDate);
        dest.writeString(this.status);
        dest.writeInt(isHeader ? 1 : 0);
        dest.writeTypedList(taskItems);

        dest.writeString(this.agentId);
        dest.writeString(this.agentName);
        dest.writeString(this.agentPhoneNumber);
        dest.writeString(this.agentEmployeeId);
        dest.writeString(this.agentType);
        dest.writeString(this.farmerName);
        dest.writeString(this.shambaName);
    }

    protected Task(Parcel in) {
        this.taskId = in.readString();
        this.name = in.readString();
        this.startedDate = in.readString();
        this.completionDate = in.readString();
        this.dueDate = in.readString();
        this.status = in.readString();
        this.isHeader = in.readInt() == 1 ? true : false;

        taskItems = new RealmList<>();
        in.readTypedList(taskItems, TaskItem.CREATOR);

        this.agentId = in.readString();
        this.agentName = in.readString();
        this.agentPhoneNumber = in.readString();
        this.agentEmployeeId = in.readString();
        this.agentType = in.readString();
        this.farmerName = in.readString();
        this.shambaName = in.readString();
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
