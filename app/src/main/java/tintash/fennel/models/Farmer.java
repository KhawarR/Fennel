package tintash.fennel.models;

/**
 * Created by Khawar on 30/9/2016.
 */
public class Farmer {

    private String name;
    private String thumbUrl;
    private String address;
    private String signupStatus;
    private boolean isHeader = false;

    public Farmer(Farmer other) {
        this.name = other.name;
        this.thumbUrl = other.thumbUrl;
        this.address = other.address;
        this.signupStatus = other.signupStatus;
        this.isHeader = other.isHeader;
    }

    public Farmer(String name, String thumbUrl, String address, String signupStatus, boolean isHeader) {
        this.name = name;
        this.thumbUrl = thumbUrl;
        this.address = address;
        this.signupStatus = signupStatus;
        this.isHeader = isHeader;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
