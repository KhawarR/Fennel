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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.taskId);
        dest.writeString(this.name);
    }

    protected Task(Parcel in) {
        this.taskId = in.readString();
        this.name = in.readString();
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
