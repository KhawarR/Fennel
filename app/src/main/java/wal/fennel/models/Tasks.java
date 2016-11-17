package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by irfanayaz on 11/16/16.
 */
public class Tasks extends RealmObject implements Parcelable {

    public String taskId;
    public String name;
//    ArrayList<FarmingTaskItem> taskItems;

    public Tasks() {
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

    protected Tasks(Parcel in) {
        this.taskId = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Tasks> CREATOR = new Parcelable.Creator<Tasks>() {
        @Override
        public Tasks createFromParcel(Parcel source) {
            return new Tasks(source);
        }

        @Override
        public Tasks[] newArray(int size) {
            return new Tasks[size];
        }
    };
}
