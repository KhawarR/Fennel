package tintash.fennel.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Khawar on 30/9/2016.
 */
public class Farmer implements Serializable{

    @SerializedName("FullName__c")
    public String fullName;

    @SerializedName("First_Name__c")
    public String firstName;

    @SerializedName("Second_Name__c")
    public String secondName;

    @SerializedName("Surname__c")
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

//    public String villageName;
//    public String treeSpecies;
//    public boolean farmerHome;
//    public String thumbUrl;
//    public String farmerIdPhotoUrl;
    public String address;
    public String signupStatus;
    public boolean isHeader = false;

    public Farmer() {}

    public Farmer(Farmer other) {

        this.fullName = other.fullName;
        this.firstName = other.firstName;
        this.secondName = other.secondName;
        this.surname = other.surname;
        this.idNumber = other.idNumber;
        this.gender = other.gender;
        this.isLeader = other.isLeader;
        this.location = other.location;
        this.subLocation = other.subLocation;
//        this.villageName = other.villageName;
//        this.treeSpecies = other.treeSpecies;
//        this.farmerHome = other.farmerHome;
        this.mobileNumber = other.mobileNumber;
//        this.thumbUrl = other.thumbUrl;
//        this.farmerIdPhotoUrl = other.farmerIdPhotoUrl;
        this.address = other.address;
        this.signupStatus = other.signupStatus;
        this.isHeader = other.isHeader;
    }

    public Farmer(String fullName, String firstName, String secondName, String surname, String thumbUrl, String address, String signupStatus, boolean isHeader) {
        this.fullName = fullName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
//        this.thumbUrl = thumbUrl;
        this.address = address;
        this.signupStatus = signupStatus;
        this.isHeader = isHeader;
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

    public void setIdNumber(java.lang.String idNumber) {
        this.idNumber = idNumber;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setIsLeader(boolean leader) {
        this.isLeader = leader;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSubLocation(String subLocation) {
        this.subLocation = subLocation;
    }

//    public void setVillageName(String villageName) {
//        this.villageName = villageName;
//    }
//
//    public void setTreeSpecies(String treeSpecies) {
//        this.treeSpecies = treeSpecies;
//    }
//
//    public void setFarmerHome(boolean farmerHome) {
//        this.farmerHome = farmerHome;
//    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

//    public void setFarmerIdPhotoUrl(String farmerIdPhotoUrl) {
//        this.farmerIdPhotoUrl = farmerIdPhotoUrl;
//    }


    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public String getSignupStatus() {
        return signupStatus;
    }

    public void setSignupStatus(String signupStatus) {
        this.signupStatus = signupStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

//    public String getThumbUrl() {
//        return thumbUrl;
//    }

//    public void setThumbUrl(String thumbUrl) {
//        this.thumbUrl = thumbUrl;
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
