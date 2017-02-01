package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by irfanayaz on 12/29/16.
 */

public class DashboardTask extends RealmObject implements Parcelable, Comparable<DashboardTask> {

    String taskId;
    String taskName;
    String dueDate;
    String completionDate;
    int totalCount;
    int completed;
    int state;

    public DashboardTask() {

    }

    public DashboardTask(String taskId, String taskName, String dueDate, String completionDate, int totalCount, int completed, int state) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.completionDate = completionDate;
        this.totalCount = totalCount;
        this.completed = completed;
        this.state = state;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.taskId);
        dest.writeString(this.taskName);
        dest.writeString(this.dueDate);
        dest.writeString(this.completionDate);
        dest.writeInt(this.totalCount);
        dest.writeInt(this.completed);
        dest.writeInt(this.state);
    }

    protected DashboardTask(Parcel in) {
        this.taskId = in.readString();
        this.taskName = in.readString();
        this.dueDate = in.readString();
        this.completionDate = in.readString();
        this.totalCount = in.readInt();
        this.completed = in.readInt();
        this.state = in.readInt();
    }

    public static final Parcelable.Creator<DashboardTask> CREATOR = new Parcelable.Creator<DashboardTask>() {
        @Override
        public DashboardTask createFromParcel(Parcel source) {
            return new DashboardTask(source);
        }

        @Override
        public DashboardTask[] newArray(int size) {
            return new DashboardTask[size];
        }
    };

    @Override
    public int compareTo(DashboardTask another) {

        String dueDateString = this.getDueDate();
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dueDate = null;
        try {
            if (dueDateString != null && !dueDateString.equalsIgnoreCase("null"))
                dueDate = serverFormat.parse(dueDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String anotherDueDateString = another.getDueDate();
        Date anotherDueDate = null;
        try {
            if (anotherDueDateString != null && !anotherDueDateString.equalsIgnoreCase("null"))
                anotherDueDate = serverFormat.parse(anotherDueDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dueDate.compareTo(anotherDueDate);
    }
}
