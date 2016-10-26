package wal.fennel.models;

/**
 * Created by irfanayaz on 10/7/16.
 */
public class Tree {

    public String id;
    public String name;
    public String subLocationId;

    public Tree(String id, String name, String subLocationId) {
        this.id = id;
        this.name = name;
        this.subLocationId = subLocationId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubLocationId(String subLocationId) {
        this.subLocationId = subLocationId;
    }
}

