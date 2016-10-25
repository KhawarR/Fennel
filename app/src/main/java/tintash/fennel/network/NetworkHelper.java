package tintash.fennel.network;

/**
 * Created by Faizan on 9/30/2016.
 */
public class NetworkHelper {
    public static final String GRANT = "password";
    public static final String CLIENT_ID = "3MVG9eYfd1zvW1E6CGmr.WMml7GUxIj_4B4n2IGfk693m6sEsdV7Eni_5Kgts.uwmLbU03gHKPPrjYN5dcTxr";
    public static final String CLIENT_SECRET = "1367114753175275829";
    public static final String REDIRECT_URI = "https://success";
    public static final String API_VERSION = "v36.0";
    public static final String URL_ATTACHMENTS = "%s/services/data/" + API_VERSION + "/sobjects/Attachment/%s/body";

    public static final String QUERY_LOGIN_1 = "Select m.User_ID__r.Name, m.User_ID__c, m.Password__c, m.Last_Sync__c, m.Active__c From Mobile_Users__c m WHERE User_ID__r.Name = '%s' AND Password__c = '%s' AND Active__c = true";
//    public static final String QUERY_LOGIN_1 = "Select m.Password__c, m.Name, m.Last_Sync__c, m.Active__c From Mobile_Users__c m WHERE m.Name = '%s' AND Password__c = '%s' AND Active__c = true";
//    public static final String QUERY_ABOUT_ME_1 = "Select e.Id, e.Name, e.Active_Employee__c, (Select Id, Name, Phone__c, Field_Office__c, Field_Office_GPS__Latitude__s, Field_Office_GPS__Longitude__s, Field_Office_GPS__c, Account__c From Field_Officers__r), (Select Id, Name, Contact__c From Field_Managers__r ), (Select Id,Phone__c, Second_Name__c, Surname__c From Facilitators__r) From Employee__c e WHERE Name = '%s' AND Active_Employee__c = true";
public static final String QUERY_ABOUT_ME_1 = "Select e.Id, e.Name, e.Active_Employee__c, First_Name__c, Last_Name__c, Middle_Name__c, (Select Id, Name, Phone__c, Field_Office__c, Field_Office_GPS__Latitude__s, Field_Office_GPS__Longitude__s, Field_Office_GPS__c, Account__c, Field_Manager__r.Employee__r.Full_Name__c From Field_Officers__r), (Select Id, Name, Contact__c From Field_Managers__r ), (Select Id,Phone__c, Field_Officer__r.Field_Manager__r.Employee__r.Full_Name__c, Field_Officer__r.Employee__r.Full_Name__c From Facilitators__r) From Employee__c e WHERE Name = '%s' AND Active_Employee__c = true";
    public static final String QUERY_ABOUT_ME_ATTACHMENT = "SELECT Id, Name, (SELECT Id, ParentId, Name FROM Attachments) FROM %s WHERE Id = '%s'";

    public static final String QUERY_MY_SIGNUPS_1 = "Select s.Status__c, s.Id, s.Field_Officer_Signup__c, s.Field_Manager_Signup__c, s.Farmer__r.Farmers_Home__c, s.Farmer__r.Mobile_Number__c, s.Farmer__r.Leader__c, s.Farmer__r.Status__c, s.Farmer__r.Middle_Name__c, s.Farmer__r.FullName__c, s.Farmer__r.Gender__c, s.Farmer__r.Last_Name__c, s.Farmer__r.First_Name__c, s.Farmer__r.Name, s.Farmer__c, s.Facilitator_Signup__c From Shamba__c s WHERE Facilitator_Signup__c = '%s' OR Field_Manager_Signup__c = '%s'  OR Field_Officer_Signup__c = '%s'  Order By Status__c";
//    public static final String QUERY_MY_SIGNUPS_1 = "SELECT Id, Farmers__r.Id, Farmers__r.FullName__c, Farmers__r.First_Name__c, Farmers__r.Middle_Name__c, Farmers__r.Last_Name__c, Farmers__r.Name, Farmers__r.Gender__c, Farmers__r.Leader__c, Farmers__r.Mobile_Number__c, Status__c, Is_Farmer_Home__c, Location__r.Name, Sub_Location__r.Name, Tree__r.Name, Village__r.Name FROM Farm__c WHERE Signup_by_Facilitator__c = '%s' OR Signup_by_Field_Manager__c = '%s'  OR Signup_by_Field_Officer__c = '%s'  Order By Status__c";
    public static final String FARMER_QUERY = "SELECT Id, Name, (SELECT Id, ParentId, Name, Description FROM Attachments) FROM Farmer__c";

    public static final String GET_LOCATIONS = "SELECT Id, Name FROM Location__c";
    public static final String GET_SUB_LOCATIONS = "SELECT Id, Name, Location__c FROM Sub_Location__c";
    public static final String GET_VILLAGES = "SELECT Id, Name, Sub_Location__c FROM Village__c";
    public static final String GET_TREES = "Select t.Sub_Location__r.Name, t.Sub_Location__c, t.Name, t.Id From Tree_Species__c t";
}
