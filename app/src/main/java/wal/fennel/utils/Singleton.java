package wal.fennel.utils;

import java.util.ArrayList;

import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.models.Farmer;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.TaskItem;

/**
 * Created by Khawar on 18/10/2016.
 */
public class Singleton {

    public String farmerIdtoInvalidate = "";
    public String taskItemPicIdtoInvalidate = "";

    public ArrayList<Farmer> mySignupsList = new ArrayList<>();
    public ArrayList<Farmer> myFarmersList = new ArrayList<>();
    public ArrayList<TaskItem> taskItems = new ArrayList<>();
    public ArrayList<FieldAgent> fieldAgentsVisitLogs = new ArrayList<>();
    public ArrayList<DashboardFieldAgent> dashboardFieldAgents = new ArrayList<>();

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

    public void clearAll() {
        farmerIdtoInvalidate = "";
        taskItemPicIdtoInvalidate = "";
        mySignupsList.clear();
        myFarmersList.clear();
        taskItems.clear();
        fieldAgentsVisitLogs.clear();
        dashboardFieldAgents.clear();
    }
}
