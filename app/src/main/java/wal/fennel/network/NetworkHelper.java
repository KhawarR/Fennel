package wal.fennel.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/30/2016.
 */
public class NetworkHelper {
    public static final String GRANT = "password";
    //TODO Test to Production
    // ------------------- Testing
    public static final String CLIENT_ID = "3MVG9eYfd1zvW1E6CGmr.WMml7GUxIj_4B4n2IGfk693m6sEsdV7Eni_5Kgts.uwmLbU03gHKPPrjYN5dcTxr";
    public static final String CLIENT_SECRET = "1367114753175275829";
    // ------------------- Production
//    public static final String CLIENT_ID = "3MVG9CVKiXR7Ri5oS6br.mcM16uAbGiOHbpwBR3WPEvOjvogmydDocsRwSJLL9ql4PTmUaCHxYB1H2lb.nxxr";
//    public static final String CLIENT_SECRET = "2913726762900040703";

    public static final String REDIRECT_URI = "https://success";
    public static final String API_VERSION = "v36.0";
    public static final String URL_ATTACHMENTS = "%s/services/data/" + API_VERSION + "/sobjects/Attachment/%s/body";

    public static final String QUERY_LOGIN_1 = "Select m.User_ID__r.Name, m.User_ID__c, m.Password__c, m.Last_Sync__c, m.Active__c From Mobile_Users__c m WHERE User_ID__r.Name = '%s' AND Password__c = '%s' AND Active__c = true";
    public static final String QUERY_ABOUT_ME_1 = "Select e.Id, e.Name, e.Active_Employee__c, First_Name__c, Last_Name__c, Middle_Name__c, (Select Id, Name, Phone__c, Field_Office__c, Field_Office_GPS__Latitude__s, Field_Office_GPS__Longitude__s, Field_Office_GPS__c, Account__c, Field_Manager__r.Employee__r.Full_Name__c From Field_Officers__r), (Select Id, Name, Contact__c From Field_Managers__r ), (Select Id,Phone__c, Field_Officer__r.Field_Manager__r.Employee__r.Full_Name__c, Field_Officer__r.Employee__r.Full_Name__c From Facilitators__r) From Employee__c e WHERE Name = '%s' AND Active_Employee__c = true";
    public static final String QUERY_ABOUT_ME_ATTACHMENT = "SELECT Id, Name, (SELECT Id, ParentId, Name FROM Attachments) FROM %s WHERE Id = '%s'";

    public static final String QUERY_MY_SIGNUPS_1 = "Select s.Status__c, s.Id, s.Field_Officer_Signup__c, s.Field_Manager_Signup__c, s.Farmer__r.Farmers_Home__c, s.Farmer__r.Mobile_Number__c, s.Farmer__r.Leader__c, s.Farmer__r.Status__c, s.Farmer__r.Middle_Name__c, s.Farmer__r.FullName__c, s.Farmer__r.Gender__c, s.Farmer__r.Last_Name__c, s.Farmer__r.First_Name__c, s.Farmer__r.Name, s.Farmer__c, s.Facilitator_Signup__c, s.Sign_Up_Status__c, s.LocationLookup__r.Id, s.LocationLookup__r.Name, s.Sub_LocationLookup__r.Id, s.Sub_LocationLookup__r.Name, Village__r.Id, Village__r.Name, Tree_Specie__r.Id, Tree_Specie__r.Name, Is_Farmer_Home__c From Shamba__c s WHERE Facilitator_Signup__c = '%s' OR Field_Manager_Signup__c = '%s'  OR Field_Officer_Signup__c = '%s'  Order By Status__c";
    public static final String QUERY_MY_SIGNUPS_ATTACHMENTS = "SELECT Id, Name, (SELECT Id, ParentId, Name, Description FROM Attachments) FROM Farmer__c WHERE Id IN (%s)";
    public static final String QUERY_TASK_ITEM_ATTACHMENTS = "SELECT Id, File_Type__c, (SELECT Id, ParentId, Name, Description FROM Attachments) FROM Task_Item__c";


    public static final String GET_LOCATIONS = "SELECT Id, Name FROM Location__c";
    public static final String GET_SUB_LOCATIONS = "SELECT Id, Name, Location__c FROM Sub_Location__c";
    public static final String GET_VILLAGES = "SELECT Id, Name, Sub_Location__c FROM Village__c";
//    public static final String GET_TREES = "Select t.Sub_Location__r.Name, t.Sub_Location__c, t.Name, t.Id From Tree_Species__c t";
    public static final String GET_TREES = "Select t.Sub_Location__r.Name, t.Sub_Location__c, t.Tree_Species__r.Name, t.Tree_Species__r.Id, t.Tree_Species__c From Tree_Specie_Locations__c t";

//    public static final String GET_MY_FARMERS_TASKS = "Select f.Status__c, f.Started_Date__c, f.Shamba__r.Facilitator_Signup__c, f.Shamba__r.Field_Officer_Signup__c, f.Shamba__r.Field_Manager_Signup__c, f.Shamba__r.Farmer__c, f.Shamba__r.Farmer__r.Name, f.Shamba__r.Farmer__r.FullName__c, f.Shamba__r.Sub_LocationLookup__r.Name, f.Shamba__r.Village__r.Name, f.Shamba__c, f.Name, f.Due_Date__c, f.Completion_Date__c From Farming_Task__c f WHERE (Shamba__r.Facilitator_Signup__c = '%s' OR f.Shamba__r.Field_Officer_Signup__c = '%s' OR f.Shamba__r.Field_Manager_Signup__c = '%s') AND f.Status__c = 'Not Started' Order By Due_Date__c , Status__c";
    public static final String GET_MY_FARMERS_TASKS = "Select f.Id, f.Status__c, f.Started_Date__c, f.Shamba__r.Facilitator_Signup__c, f.Shamba__r.Field_Officer_Signup__c, f.Shamba__r.Field_Manager_Signup__c, f.Shamba__r.Farmer__c, f.Shamba__r.Farmer__r.Name, f.Shamba__r.Farmer__r.FullName__c, f.Shamba__r.Farmer__r.Mobile_Number__c, f.Shamba__r.Sub_LocationLookup__r.Name, f.Shamba__r.Village__r.Name, f.Shamba__c, f.Name, f.Due_Date__c, f.Completion_Date__c From Farming_Task__c f WHERE (Shamba__r.Facilitator_Signup__c = '%s' OR f.Shamba__r.Field_Officer_Signup__c = '%s' OR f.Shamba__r.Field_Manager_Signup__c = '%s') Order By Due_Date__c , Status__c";
//    public static final String GET_FARMER_TASK_ITEMS = "Select t.Text_Value__c, t.Sequence__c, t.RecordType.Name, t.RecordTypeId, t.Name, t.Location__c, t.Location__Longitude__s, t.Location__Latitude__s, t.GPS_Taken_Time__c, t.File_Type__c, t.Farming_Task__c, t.Description__c, (Select Name, Value__c From Task_Item_Options__r) From Task_Item__c t WHERE Farming_Task__c = '%s' order by Sequence__c";
    public static final String GET_FARMER_TASK_ITEMS = "Select t.Id, t.Text_Value__c, t.Sequence__c, t.RecordType.Name, t.RecordTypeId, t.Name, t.Location__c, t.Location__Longitude__s, t.Location__Latitude__s, t.GPS_Taken_Time__c, t.File_Type__c, t.Farming_Task__c, t.Description__c, (Select Id, Name, Value__c From Task_Item_Options__r) From Task_Item__c t WHERE Farming_Task__c IN (%s) order by Sequence__c";

    public static final String GET_MY_LOGBOOK_DATA_FIELD_MANAGER = "Select v.Farm_Visit__r.Visited_Date__c, v.Farm_Visit__r.Field_Manager__r.Id, v.Farm_Visit__r.Field_Manager__r.Name, v.Farm_Visit__r.Field_Manager__r.Employee__r.Name, v.Farm_Visit__r.Field_Officer__r.Id, v.Farm_Visit__r.Field_Officer__r.Name, v.Farm_Visit__r.Field_Officer__r.Employee__r.Name, v.Farm_Visit__r.Field_Officer__r.Phone__c, v.Farm_Visit__r.Facilitator__r.Id, v.Farm_Visit__r.Facilitator__r.Name, v.Farm_Visit__r.Facilitator__r.Employee_ID__c, v.Farm_Visit__r.Facilitator__r.Phone__c, v.Farm_Visit__r.Farmer__c, Farm_Visit__r.Farmer__r.Name, Farm_Visit__r.Farmer__r.FullName__c, v.Farm_Visit__r.Shamba__c, Farm_Visit__r.Shamba__r.Name, v.Farm_Visit__c, Farming_Task__c  From Visit_Log__c v WHERE Farm_Visit__r.Field_Manager__c = '%s' OR Farm_Visit__r.Field_Officer__c IN (%s) OR Farm_Visit__r.Facilitator__c IN (%s)";
    public static final String GET_MY_LOGBOOK_DATA_FIELD_OFFICER = "Select v.Farm_Visit__r.Visited_Date__c, v.Farm_Visit__r.Field_Manager__c, v.Farm_Visit__r.Field_Officer__r.Name, v.Farm_Visit__r.Field_Officer__r.Phone__c, v.Farm_Visit__r.Field_Officer__r.Employee__r.Name,, v.Farm_Visit__r.Facilitator__r.Name, v.Farm_Visit__r.Facilitator__r.Phone__c, v.Farm_Visit__r.Facilitator__r.Employee_ID__c, v.Farm_Visit__r.Farmer__c, Farm_Visit__r.Farmer__r.Name, Farm_Visit__r.Farmer__r.First_Name__c, v.Farm_Visit__r.Shamba__c, Farm_Visit__r.Shamba__r.Name, v.Farm_Visit__c, Farming_Task__c  From Visit_Log__c v WHERE Farm_Visit__r.Field_Officer__c = '%s' OR Farm_Visit__r.Facilitator__c IN (%s)";
    public static final String GET_MY_LOGBOOK_DATA_FACILITATOR = "Select v.Farm_Visit__r.Visited_Date__c, v.Farm_Visit__r.Field_Manager__c, v.Farm_Visit__r.Field_Officer____r.Name, v.Farm_Visit__r.Field_Officer__r.Phone__c, v.Farm_Visit__r.Facilitator__r.Name, v.Farm_Visit__r.Facilitator__r.Phone__c, v.Farm_Visit__r.Facilitator__r.Employee_ID__c, v.Farm_Visit__r.Farmer__c, Farm_Visit__r.Farmer__r.Name, Farm_Visit__r.Farmer__r.First_Name__c, v.Farm_Visit__r.Shamba__c, Farm_Visit__r.Shamba__r.Name, v.Farm_Visit__c, Farming_Task__c  From Visit_Log__c v WHERE Farm_Visit__r.Facilitator__c = '%s'";


    public static final String GET_FO_FAC_FOR_FM = "Select f.Id, f.Employee__r.Name, f.Field_Manager__c, (Select Id, Employee_ID__c From Facilitators__r) From Field_Officer__c f WHERE Field_Manager__c = '%s'";
    public static final String GET_FAC_FOR_FO = "Select f.Id, f.Employee_ID__c From Facilitator__c f WHERE Field_Officer__c = '%s'";

    public static final String GET_ALL_LOGBOOK_DATA = "Select f.Shamba__r.Id, f.Shamba__r.Village_Name__c, f.Shamba__r.Farmer__r.FullName__c, f.Status__c, f.Started_Date__c, f.Name, f.Id, f.Due_Date__c, f.Completion_Date__c, (Select Id, Name, LastModifiedDate, RecordTypeId, RecordType.Name, GPS_Taken_Time__c, Sequence__c, Location__Latitude__s, Location__Longitude__s, Location__c, Text_Value__c, File_Type__c, Description__c, File_Action__c From Task_Items__r WHERE Is_Modified__c = true) From Farming_Task__c f  WHERE Id IN (%s)";

    private static final String STR_FILE_PREFIX = "file://";

    public static final String QUERY_MY_LOGBOOK_ATTACHMENTS = "SELECT Id, Name, (SELECT Id, ParentId, Name, Description FROM Attachments) FROM Employee__c WHERE Name IN (%s)";


    public static boolean isNetAvailable(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static boolean isCommunicationAllowed()
    {
        return !PreferenceHelper.getInstance().readIsSyncInProgress();
    }

    public static String makeAttachmentUrlFromId(String attId) {
        return String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), attId);
    }

    public static String getUploadPathFromUri(String uri) {
        if(uri == null || uri.isEmpty())
            return "";

        return uri.replace(STR_FILE_PREFIX, "");
    }

    public static String getUriFromPath(String path) {
        if(path == null || path.isEmpty())
            return "";

        return Uri.parse("file://" + path).toString();
    }
}
