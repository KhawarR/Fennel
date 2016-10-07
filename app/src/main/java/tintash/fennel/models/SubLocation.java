package tintash.fennel.models;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class SubLocation {

    public String id;
    public String name;
    public String locationId;

    public SubLocation(String id, String name, String locationId) {
        this.id = id;
        this.name = name;
        this.locationId = locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
