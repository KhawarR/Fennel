package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by irfanayaz on 11/16/16.
 */
public class Task extends RealmObject implements Parcelable {

    public String taskId;
    public String name;
    public String startedDate;
    public String completionDate;
    public String dueDate;
    public String status;

    public Task(String taskId, String name, String startedDate, String completionDate, String dueDate, String status) {
        this.taskId = taskId;
        this.name = name;
        this.startedDate = startedDate;
        this.completionDate = completionDate;
        this.dueDate = dueDate;
        this.status = status;
    }

//    ArrayList<FarmingTaskItem> taskItems;

    public Task() {
    }

    public Task(String taskId, String name) {
        this.taskId = taskId;
        this.name = name;
    }

    public Task(Task other) {
        this.taskId = other.taskId;
        this.name = other.name;
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
    }

    protected Task(Parcel in) {
        this.taskId = in.readString();
        this.name = in.readString();
        this.startedDate = in.readString();
        this.completionDate = in.readString();
        this.dueDate = in.readString();
        this.status = in.readString();
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
