package wal.fennel.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by irfanayaz on 10/6/16.
 */
public class Farm {

    private String farmId;

    @SerializedName("Name")
    private String name = "";

    @SerializedName("Facilitator__c")
    private String facilitatorId;

    @SerializedName("Farmers__c")
    private String farmerId;

    @SerializedName("Farmer_Enrollment_Date__c")
    private String farmerEnrollmentDate;

    @SerializedName("Location__c")
    private String location;

    @SerializedName("Sub_Location__c")
    private String subLocation;

    @SerializedName("Village__c")
    private String villageName;

    @SerializedName("Tree__c")
    private String treeSpecies;

    @SerializedName("Status__c")
    private String farmerStatus;

    public void setFacilitatorId(String facilitatorId) {
        this.facilitatorId = facilitatorId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public void setFarmerEnrollmentDate(String farmerEnrollmentDate) {
        this.farmerEnrollmentDate = farmerEnrollmentDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSubLocation(String subLocation) {
        this.subLocation = subLocation;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public void setTreeSpecies(String treeSpecies) {
        this.treeSpecies = treeSpecies;
    }

    public void setFarmerStatus(String farmerStatus) {
        this.farmerStatus = farmerStatus;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacilitatorId() {
        return facilitatorId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public String getFarmerEnrollmentDate() {
        return farmerEnrollmentDate;
    }

    public String getLocation() {
        return location;
    }

    public String getSubLocation() {
        return subLocation;
    }

    public String getVillageName() {
        return villageName;
    }

    public String getTreeSpecies() {
        return treeSpecies;
    }

    public String getFarmerStatus() {
        return farmerStatus;
    }
}
