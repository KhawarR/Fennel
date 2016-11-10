package wal.fennel.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

import io.realm.RealmList;
import wal.fennel.models.Farmer;

/**
 * Created by Khawar on 18/10/2016.
 */
public class Singleton {

    public String farmerIdtoInvalidate = "";

    public ArrayList<Farmer> mySignupsList = new ArrayList<>();

    private static Singleton instance;

    private Singleton(){
        mySignupsList = new ArrayList<>();
    }

    public synchronized static Singleton getInstance()
    {
        if(instance == null)
            instance = new Singleton();
        return instance;
    }
}
