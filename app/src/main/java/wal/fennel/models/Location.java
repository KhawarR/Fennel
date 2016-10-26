package wal.fennel.models;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class Location {

    public String id;
    public String name;

    public Location(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
