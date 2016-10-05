package tintash.fennel.models;

import java.io.Serializable;

/**
 * Created by Khawar on 30/9/2016.
 */
public class Farmer implements Serializable{

    public String fullName;
    public String firstName;
    public String secondName;
    public String surname;
    public String idNumber;
    public int gender;
    public boolean isLeader;
    public String location;
    public String subLocation;
    public String villageName;
    public String treeSpecies;
    public boolean farmerHome;
    public String mobileNumber;
    public String thumbUrl;
    public String farmerIdPhotoUrl;
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
        this.villageName = other.villageName;
        this.treeSpecies = other.treeSpecies;
        this.farmerHome = other.farmerHome;
        this.mobileNumber = other.mobileNumber;
        this.thumbUrl = other.thumbUrl;
        this.farmerIdPhotoUrl = other.farmerIdPhotoUrl;
        this.address = other.address;
        this.signupStatus = other.signupStatus;
        this.isHeader = other.isHeader;
    }

    public Farmer(String fullName, String firstName, String secondName, String surname, String thumbUrl, String address, String signupStatus, boolean isHeader) {
        this.fullName = fullName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.thumbUrl = thumbUrl;
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

    public void setGender(int gender) {
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

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public void setTreeSpecies(String treeSpecies) {
        this.treeSpecies = treeSpecies;
    }

    public void setFarmerHome(boolean farmerHome) {
        this.farmerHome = farmerHome;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setFarmerIdPhotoUrl(String farmerIdPhotoUrl) {
        this.farmerIdPhotoUrl = farmerIdPhotoUrl;
    }


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

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
