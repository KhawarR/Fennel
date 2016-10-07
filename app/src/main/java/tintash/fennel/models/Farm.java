package tintash.fennel.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by irfanayaz on 10/6/16.
 */
public class Farm {

    public String farmId;

    @SerializedName("Name")
    public String name = "";

    @SerializedName("Facilitator__c")
    public String facilitatorId;

    @SerializedName("Farmers__c")
    public String farmerId;

    @SerializedName("Farmer_Enrollment_Date__c")
    public String farmerEnrollmentDate;

    @SerializedName("Location__c")
    public String location;

    @SerializedName("Sub_Location__c")
    public String subLocation;

    @SerializedName("Village__c")
    public String villageName;

    @SerializedName("Tree__c")
    public String treeSpecies;

    @SerializedName("Status__c")
    public String farmerStatus;

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

}
