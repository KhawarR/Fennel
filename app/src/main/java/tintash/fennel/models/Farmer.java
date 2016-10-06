package tintash.fennel.models;

import java.io.Serializable;

/**
 * Created by Khawar on 30/9/2016.
 */
public class Farmer implements Serializable{

    private String fullName;
    private String firstName;
    private String secondName;
    private String surname;
    private String idNumber;
    private String gender;
    private boolean isLeader;
    private String location;
    private String subLocation;
    private String villageName;
    private String treeSpecies;
    private boolean farmerHome;
    private String mobileNumber;
    private String thumbUrl;
    private String farmerIdPhotoUrl;
    private String address;
    private String signupStatus;
    private boolean isHeader = false;

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

    public Farmer(String fullName, String firstName, String secondName, String surname,
                  String idNumber, String gender, boolean isLeader, String location,
                  String subLocation, String villageName, String treeSpecies, boolean farmerHome,
                  String mobileNumber, String farmerIdPhotoUrl, String thumbUrl, String address,
                  String signupStatus, boolean isHeader) {
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
        this.thumbUrl = thumbUrl;
        this.farmerIdPhotoUrl = farmerIdPhotoUrl;
        this.address = address;
        this.signupStatus = signupStatus;
        this.isHeader = isHeader;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
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

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getFarmerIdPhotoUrl() {
        return farmerIdPhotoUrl;
    }

    public void setFarmerIdPhotoUrl(String farmerIdPhotoUrl) {
        this.farmerIdPhotoUrl = farmerIdPhotoUrl;
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
}
