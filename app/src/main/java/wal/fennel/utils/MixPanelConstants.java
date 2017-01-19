package wal.fennel.utils;

/**
 * Created by Khawar on 2/12/2016.
 */

public class MixPanelConstants {

    // Khawar Test Fennel project
//    public static final String MIXPANEL_TOKEN = "d484e812f6310fedd9c201f8ff9f46b6";

    // Tiffany Fennel Project
    public static final String MIXPANEL_TOKEN = "06ca76ee304364b63b5b6e1df4dfaf2e";

    public static class PageView{
        public static final String LOGIN = "Login-PageView";
        public static final String ABOUT_ME = "AboutMe-PageView";
        public static final String MY_SIGNUPS = "MySignups-PageView";
        public static final String ENROLL_FARMER = "Enroll Farmer-PageView";
        public static final String EDIT_FARMER = "Edit Farmer-PageView";
    }

    public static class Event{

        public static final String TAB_MY_SIGNUPS = "Signups-Tab";
        public static final String TAB_MY_FARMERS = "Farmers-Tab";
        public static final String TAB_MY_DASHBOARD = "Dashboard-Tab";
        public static final String TAB_MY_LOGBOOK = "Logbook-Tab";

        public static final String TITLE_BAR_AVATAR_CLICK = "Title Bar Avatar-Click";

        public static final String LOGIN_BUTTON = "Login-Button";
        public static final String BACK_BUTTON = "Back-Button";
        public static final String CANCEL_BUTTON = "Cancel-Button";
        public static final String MANUAL_SYNC_ACTION = "Manual Sync-Action";
        public static final String CHANGE_AVATAR_BUTTON = "Change Avatar-Button";
        public static final String SIGNOUT_BUTTON = "Signout-Button";
        public static final String ENROLL_FARMER_BUTTON = "Enroll Farmer-Button";
        public static final String SEARCH_MYSIGNUP_ACTION = "Search MySignup-Action";
        public static final String MYSIGNUP_MENU_ITEM_ACTION = "MySignup Menu Item-Click";

        public static final String FARMER_LOG_UPLOADED = "Farmer Log Uploaded";
        public static final String DEBUG_LOG_UPLOADED = "Debug Log Uploaded";
        public static final String SYNC_COMPLETED = "Sync Completed";

        public static final String DROPDOWN_SHOW_LOCATION = "Dropdown location-Show";
        public static final String DROPDOWN_SHOW_SUBLOCATION = "Dropdown sublocation-Show";
        public static final String DROPDOWN_SHOW_VILLAGE = "Dropdown village-Show";
        public static final String DROPDOWN_SHOW_TREE = "Dropdown tree-Show";

        public static final String DROPDOWN_PICK_LOCATION = "Dropdown location-Pick";
        public static final String DROPDOWN_PICK_SUBLOCATION = "Dropdown sublocation-Pick";
        public static final String DROPDOWN_PICK_VILLAGE = "Dropdown village-Pick";
        public static final String DROPDOWN_PICK_TREE = "Dropdown tree-Pick";

        public static final String CREATE_FARMER_BUTTON = "Create Farmer-Button";
        public static final String SAVE_FARMER_BUTTON = "Save Farmer-Button";
        public static final String SUBMIT_FOR_APPROVAL_BUTTON = "Submit For Approval-Button";
        public static final String FARMER_PHOTO_BUTTON = "Farmer Photo-Button";
        public static final String FARMER_ID_PHOTO_BUTTON = "Farmer ID Photo-Button";
    }

    public static class Property{
        public static final String DEFAULT_NAME = "$name";
        public static final String EMPLOYEE_ID = "Employee ID";
        public static final String USERNAME = "Username";
        public static final String PASSWORD = "Password";
        public static final String SEARCH_KEY = "Search Key";
        public static final String MYSIGNUP_STATUS = "MySignup Status";
        public static final String MYSIGNUP_NAME = "MySignup Name";
        public static final String MYSIGNUP_ID = "MySignup Id";

        public static final String FIRST_NAME = "First Name";
        public static final String SECOND_NAME = "Second Name";
        public static final String SURNAME = "Surname";
        public static final String ID_NUMBER = "Id Number";
        public static final String GENDER = "Gender";
        public static final String IS_LEADER = "Is Leader";
        public static final String LOCATION = "Location";
        public static final String SUBLOCATION = "Sub location";
        public static final String VILLAGE = "Village";
        public static final String TREE = "Tree";
        public static final String IS_FARMER_HOME = "Is farmer home";
        public static final String MOBILE_NUMBER = "Mobile number";

    }
}
