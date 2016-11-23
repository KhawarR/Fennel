package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmModel;
import io.realm.RealmObject;

/**
 * Created by Khawar on 22/11/2016.
 */

public class TaskItemOption extends RealmObject implements Parcelable{

    private String id = "";
    private String name = "";
    private boolean isValue = false;

    public TaskItemOption(){

    }

    public TaskItemOption(TaskItemOption other) {
        this.id = other.id;
        this.name = other.name;
        this.isValue = other.isValue;
    }

    public TaskItemOption(String id, String name, boolean isValue) {
        this.id = id;
        this.name = name;
        this.isValue = isValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValue() {
        return isValue;
    }

    public void setValue(boolean value) {
        isValue = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(isValue ? 1 : 0);
    }

    protected TaskItemOption(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.isValue = in.readInt() == 1 ? true : false;
    }

    public static final Parcelable.Creator<TaskItemOption> CREATOR = new Parcelable.Creator<TaskItemOption>() {
        @Override
        public TaskItemOption createFromParcel(Parcel source) {
            return new TaskItemOption(source);
        }

        @Override
        public TaskItemOption[] newArray(int size) {
            return new TaskItemOption[size];
        }
    };
}
