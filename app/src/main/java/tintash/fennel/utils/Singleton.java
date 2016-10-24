package tintash.fennel.utils;

/**
 * Created by Khawar on 18/10/2016.
 */
public class Singleton {

    public String farmerIdtoInvalidate = "";

    private static Singleton instance;

    private Singleton(){}

    public synchronized static Singleton getInstance()
    {
        if(instance == null)
            instance = new Singleton();
        return instance;
    }
}
