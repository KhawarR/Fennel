package wal.fennel.models;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class Location implements Comparable<Location> {

    private String id;
    private String name;

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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Location another) {
        return name.compareTo(another.name);
    }
}
