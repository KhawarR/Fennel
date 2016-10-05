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

    public static final String QUERY_LOGIN = "SELECT Id, Name, Facilitator__r.Id FROM Mobile_Users__c WHERE Name = '%s' AND Password__c = '%s'";
    public static final String QUERY_ABOUT_ME = "SELECT Id, Facilitator__r.Id, Facilitator__r.Name, Facilitator__r.Second_Name__c, Facilitator__r.Surname__c, Facilitator__r.Field_Officer__r.Id, Facilitator__r.Field_Officer__r.Name, Facilitator__r.Field_Officer__r.Field_Manager__r.Id, Facilitator__r.Field_Officer__r.Field_Manager__r.Name FROM Mobile_Users__c WHERE Name = '%s' AND Password__c = '%s'";
    public static final String QUERY_MY_SIGNUPS = "SELECT Farmers__r.Name , Facilitator__r.Name , Farmer_Enrollment_Date__c, Location__r.Name, Planting_Date__c, Status__c, Sub_Location__r.Name , Tree__r.Name , Village__r.Name FROM Farm__c WHERE Facilitator__c = '%s' Order By Status__c";
}
