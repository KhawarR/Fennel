package wal.fennel.models;

import android.os.Parcel;
import android.os.Parcelable;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Khawar on 3/1/2017.
 */

public class FarmVisit extends RealmObject implements Parcelable {

    private String farmVisitId = "";
    private String loggedInPersonRole = "";
    private String loggedInPersonId = "";
    private String shambaId = "";
    private String farmerId = "";
    private long visitedDate = 0;
    private boolean isDataDirty = false;

    public FarmVisit() {

    }

    public FarmVisit(String farmVisitId, String shambaId, String farmerId, String loggedInPersonId, String loggedInPersonRole, long visitedDate, boolean isDataDirty) {
        this.loggedInPersonRole = loggedInPersonRole;
        this.loggedInPersonId = loggedInPersonId;
        this.visitedDate = visitedDate;
        this.farmVisitId = farmVisitId;
        this.shambaId = shambaId;
        this.farmerId = farmerId;
        this.isDataDirty = isDataDirty;
    }

    public FarmVisit(FarmVisit other) {
        this.loggedInPersonRole = other.loggedInPersonRole;
        this.loggedInPersonId = other.loggedInPersonId;
        this.visitedDate = other.visitedDate;
        this.farmVisitId = other.farmVisitId;
        this.shambaId = other.shambaId;
        this.farmerId = other.farmerId;
        this.isDataDirty = other.isDataDirty;
    }

    public void setAll(String farmVisitId, String shambaId, String farmerId, String loggedInPersonId, String loggedInPersonRole, long visitedDate, boolean isDataDirty) {
        this.loggedInPersonRole = loggedInPersonRole;
        this.loggedInPersonId = loggedInPersonId;
        this.visitedDate = visitedDate;
        this.farmVisitId = farmVisitId;
        this.shambaId = shambaId;
        this.farmerId = farmerId;
        this.isDataDirty = isDataDirty;
    }

    public String getFarmVisitId() {
        return farmVisitId;
    }

    public void setFarmVisitId(String farmVisitId) {
        this.farmVisitId = farmVisitId;
    }

    public String getShambaId() {
        return shambaId;
    }

    public void setShambaId(String shambaId) {
        this.shambaId = shambaId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getLoggedInPersonId() {
        return loggedInPersonId;
    }

    public void setLoggedInPersonId(String loggedInPersonId) {
        this.loggedInPersonId = loggedInPersonId;
    }

    public String getLoggedInPersonRole() {
        return loggedInPersonRole;
    }

    public void setLoggedInPersonRole(String loggedInPersonRole) {
        this.loggedInPersonRole = loggedInPersonRole;
    }

    public long getVisitedDate() {
        return visitedDate;
    }

    public void setVisitedDate(long visitedDate) {
        this.visitedDate = visitedDate;
    }

    public boolean isDataDirty() {
        return isDataDirty;
    }

    public void setDataDirty(boolean dataDirty) {
        isDataDirty = dataDirty;
    }

    public static final Parcelable.Creator<FarmVisit> CREATOR
            = new Parcelable.Creator<FarmVisit>() {
        public FarmVisit createFromParcel(Parcel in) {
            return new FarmVisit(in);
        }

        public FarmVisit[] newArray(int size) {
            return new FarmVisit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(farmVisitId);
        out.writeString(shambaId);
        out.writeString(farmerId);
        out.writeString(loggedInPersonId);
        out.writeString(loggedInPersonRole);
        out.writeLong(visitedDate);
        out.writeInt(isDataDirty ? 1 : 0);
    }

    private FarmVisit(Parcel in) {
        farmVisitId = in.readString();
        shambaId = in.readString();
        farmerId = in.readString();
        loggedInPersonId = in.readString();
        loggedInPersonRole = in.readString();
        visitedDate = in.readLong();
        isDataDirty = in.readInt() == 1 ? true : false;
    }
}
