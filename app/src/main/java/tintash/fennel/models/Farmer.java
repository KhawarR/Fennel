package tintash.fennel.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Khawar on 30/9/2016.
 */
public class Farmer extends RealmObject implements Serializable{

    public String farmerId = null;
    public String farmId = null;

    @SerializedName("FullName__c")
    public String fullName;

    @SerializedName("First_Name__c")
    public String firstName;

    @SerializedName("Middle_Name__c")
    public String secondName;

    @SerializedName("Last_Name__c")
    public String surname;

    @SerializedName("Name")
    public String idNumber;

    @SerializedName("Gender__c")
    public String gender;

    @SerializedName("Leader__c")
    public boolean isLeader;

    @SerializedName("Location__c")
    public String location;

    @SerializedName("Sub_Location__c")
    public String subLocation;

    @SerializedName("Mobile_Number__c")
    public String mobileNumber;

    public String villageName;
    public String treeSpecies;
    public boolean farmerHome;

    public String thumbAttachmentId;
    public String nationalCardAttachmentId;

    public String thumbUrl;
    public String nationalCardUrl;

    public boolean isDataDirty = false;
    public boolean isThumbDirty = false;
    public boolean isNationalCardDirty = false;

    public String address;
    public String signupStatus;
    public boolean isHeader = false;

    public Farmer() {}

    public Farmer(Farmer other) {
        this.farmerId = other.farmerId;
        this.farmId = other.farmId;
        this.fullName = other.fullName;
        this.firstName = other.firstName;
        this.secondName = other.secondName;
        this.surname = other.surname;
        this.idNumber = other.idNumber;
        this.gender = other.gender;
        this.isLeader = other.isLeader;
        this.location = other.location;
        this.subLocation = other.subLocation;
        this.villageName = other.villageName;
        this.treeSpecies = other.treeSpecies;
        this.farmerHome = other.farmerHome;
        this.mobileNumber = other.mobileNumber;
        this.thumbAttachmentId = other.thumbAttachmentId;
        this.nationalCardAttachmentId = other.nationalCardAttachmentId;
        this.address = other.address;
        this.signupStatus = other.signupStatus;
        this.isHeader = other.isHeader;
    }

    public Farmer(String farmerId, String farmId, String fullName, String firstName, String secondName, String surname,
                  String idNumber, String gender, boolean isLeader, String location,
                  String subLocation, String villageName, String treeSpecies, boolean farmerHome,
                  String mobileNumber, String nationalCardAttachmentId, String thumbAttachmentId, String address,
                  String signupStatus, boolean isHeader) {
        this.farmerId = farmerId;
        this.farmId = farmId;
        this.fullName = fullName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.idNumber = idNumber;
        this.gender = gender;
        this.isLeader = isLeader;
        this.location = location;
        this.subLocation = subLocation;
        this.villageName = villageName;
        this.treeSpecies = treeSpecies;
        this.farmerHome = farmerHome;
        this.mobileNumber = mobileNumber;
        this.thumbAttachmentId = thumbAttachmentId;
        this.nationalCardAttachmentId = nationalCardAttachmentId;
        this.address = address;
        this.signupStatus = signupStatus;
        this.isHeader = isHeader;
    }

    public void setAllValues(String farmerId, String farmId, String fullName, String firstName, String secondName, String surname,
                       String idNumber, String gender, boolean isLeader, String location,
                       String subLocation, String villageName, String treeSpecies, boolean farmerHome,
                       String mobileNumber, String nationalCardAttachmentId, String thumbAttachmentId, String address,
                       String signupStatus, boolean isHeader) {
        this.farmerId = farmerId;
        this.farmId = farmId;
        this.fullName = fullName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.idNumber = idNumber;
        this.gender = gender;
        this.isLeader = isLeader;
        this.location = location;
        this.subLocation = subLocation;
        this.villageName = villageName;
        this.treeSpecies = treeSpecies;
        this.farmerHome = farmerHome;
        this.mobileNumber = mobileNumber;
        this.thumbAttachmentId = thumbAttachmentId;
        this.nationalCardAttachmentId = nationalCardAttachmentId;
        this.address = address;
        this.signupStatus = signupStatus;
        this.isHeader = isHeader;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubLocation() {
        return subLocation;
    }

    public void setSubLocation(String subLocation) {
        this.subLocation = subLocation;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getVillageName() {
        return villageName;
    }

    public String getTreeSpecies() {
        return treeSpecies;
    }

    public void setTreeSpecies(String treeSpecies) {
        this.treeSpecies = treeSpecies;
    }

    public boolean isFarmerHome() {
        return farmerHome;
    }

    public void setFarmerHome(boolean farmerHome) {
        this.farmerHome = farmerHome;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getThumbAttachmentId() {
        return thumbAttachmentId;
    }

    public void setThumbAttachmentId(String thumbAttachmentId) {
        this.thumbAttachmentId = thumbAttachmentId;
    }

    public String getNationalCardAttachmentId() {
        return nationalCardAttachmentId;
    }

    public void setNationalCardAttachmentId(String nationalCardAttachmentId) {
        this.nationalCardAttachmentId = nationalCardAttachmentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSignupStatus() {
        return signupStatus;
    }

    public void setSignupStatus(String signupStatus) {
        this.signupStatus = signupStatus;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getNationalCardUrl() {
        return nationalCardUrl;
    }

    public void setNationalCardUrl(String nationalCardUrl) {
        this.nationalCardUrl = nationalCardUrl;
    }

    public boolean isDataDirty() {
        return isDataDirty;
    }

    public void setDataDirty(boolean dataDirty) {
        isDataDirty = dataDirty;
    }

    public boolean isThumbDirty() {
        return isThumbDirty;
    }

    public void setThumbDirty(boolean thumbDirty) {
        isThumbDirty = thumbDirty;
    }

    public boolean isNationalCardDirty() {
        return isNationalCardDirty;
    }

    public void setNationalCardDirty(boolean nationalCardDirty) {
        isNationalCardDirty = nationalCardDirty;
    }
}
