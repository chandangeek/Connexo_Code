package com.energyict.genericprotocolimpl.common;


public class RtuMessageConstant {
    
    /** RtuMessage tag for connecting load */
    public final static String CONNECT_LOAD = "connectLoad";
    /** RtuMessage tag for disconnecting load */
    public final static String DISCONNECT_LOAD = "disconnectLoad";
    /** RtuMessage tag to indicate the digital output to apply the connectControl*/
    public final static String DIGITAL_OUTPUT = "Digital_output";
    /** RtuMessage tag to indicate the date of the connect/disconnect */
	public static final String DISCONNECT_CONTROL_ACTIVATE_DATE = "Activation_date";
    
    /** RtuMessage tag for reading profile data */
    public final static String READ_PROFILE = "readProfile";

    /** RtuMessage tag for reading data on demand */
    public final static String READ_ON_DEMAND = "onDemand";
    
    /** RtuMessage tag for tou schedule */
    public final static String TOU_SCHEDULE = "UserFile ID of tariff program";
    public final static String TOU_ACTIVITY_CAL = "Activity_Calendar";
    public final static String TOU_ACTIVITY_NAME = "Calendar_Name";
    public final static String TOU_ACTIVITY_CODE_TABLE = "Code_Table";
    public final static String TOU_ACTIVITY_USER_FILE = "Userfile";
    public final static String TOU_ACTIVITY_DATE = "Activation_Date";
    public final static String TOU_SPECIAL_DAYS = "Special_Days";
    public final static String TOU_SPECIAL_DAYS_CODE_TABLE = "Code_Table";
    
    /** RtuMessage tag for threshold parameters */
    public final static String THRESHOLD_PARAMETERS = "thresholdParameters";
    public final static String THRESHOLD_GROUPID = "Threshold GroupId *";
    public final static String PARAMETER_GROUPID = "Parameter GroupId *";
    public final static String THRESHOLD_POWERLIMIT = "Threshold PowerLimit (W)";
    public final static String CONTRACT_POWERLIMIT = "Contractual PowerLimit (W)";
    public final static String APPLY_THRESHOLD	= "Apply threshold";
    public final static String CLEAR_THRESHOLD	= "Clear threshold - groupID";
    public final static String THRESHOLD_STARTDT = "StartDate (dd/mm/yyyy HH:MM:SS)";
    public final static String THRESHOLD_STOPDT = "EndDate (dd/mm/yyyy HH:MM:SS)";
    
    /** RtuMessage tag for connecting load */
    public final static String LOAD_CONTROL_ON = "loadControlOn";
    /** RtuMessage tag for disconnecting load */
    public final static String LOAD_CONTROL_OFF = "loadControlOff";
    
    /** RtuMessage tag for changing the repeater mode of a PLC meter */
    public final static String REPEATER_MODE = "repeaterMode";
    
    /** RtuMessage tag for changing the PLC frequency of the meter */
    public final static String CHANGE_PLC_FREQUENCY = "changePLCFreq";
    public final static String FREQUENCY_MARK = "Frequency mark";
    public final static String FREQUENCY_SPACE = "Frequency space";
    
    /** RtuMessage tag for upgrading the meters firmware */
    public final static String FIRMWARE_UPGRADE = "FirmwareUpgrade";
    public final static String FIRMWARE = "UserFileID";
    public final static String FIRMWARE_METERS = "GroupID of meters to receive new firmware";
	public static final String FIRMWARE_ACTIVATE_NOW = "Activate_now";
	public static final String FIRMWARE_ACTIVATE_DATE = "Activation_date";
    
    /** RtuMessage tag for prepaid functionality */
    public final static String PREPAID_CONFIGURED = "Configure_Prepaid_functionality";
    public final static String PREPAID_ADD = "Add_Prepaid_credit";
    public final static String PREPAID_READ = "Read_Prepaid_credit";
    public final static String PREPAID_ENABLE = "Enable_Prepaid_functionality";
    public final static String PREPAID_DISABLE = "Disable_Prepaid_functionality";
    public final static String PREPAID_BUDGET = "Budget";
    public final static String PREPAID_THRESHOLD = "Threshold";
    public final static String PREPAID_MULTIPLIER = "Multiplier_tariff_";
    public final static String PREPAID_READ_FREQUENCY = "Read_frequency";
    
    /** RtuMessage tags for load limiting */
	public static final String LOAD_LIMIT_ENABLE = "Enable_load_limiting";
	public static final String LOAD_LIMIT_DISABLE = "Disable_load_limitng";
	public static final String LOAD_LIMIT_CONFIGURE = "Configure_load_limiting";
	public static final String LOAD_LIMIT_READ_FREQUENCY = "Read_frequency";
	public static final String LOAD_LIMIT_THRESHOLD = "Threshold";
	public static final String LOAD_LIMIT_DURATION = "Duration";
	public static final String LOAD_LIMIT_D1_INVERT = "Digital_Output1_Invert";
	public static final String LOAD_LIMIT_D2_INVERT = "Digital_Output2_Invert";
	public static final String LOAD_LIMIT_ACTIVATE_NOW = "Activate_now";
	public static final String LOAD_LIMIT_NORMAL_THRESHOLD = "Normal_Threshold";
	public static final String LOAD_LIMIT_EMERGENCY_THRESHOLD = "Emergency_Threshold";
	public static final String LOAD_LIMIT_EMERGENCY_PROFILE = "Emergency_Profile";
	public static final String LOAD_LIMIT_EP_PROFILE_ID = "EP_Profile_Id";
	public static final String LOAD_LIMIT_EP_ACTIVATION_TIME = "EP_Activation_Time";
	public static final String LOAD_LIMIT_EP_DURATION = "EP_Duration";
	public static final String LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST = "EP_GroupIds";
	public static final String LOAD_LIMIT_EP_GRID_LOOKUP_ID = "Lookup_Table_ID";
	public static final String LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION = "Over_Threshold_Duration";
	
	/** RtuMessage tags for XMLConfig */
	public static final String XMLCONFIG = "XMLConfig";
	
	/** RtuMessage tags for P1 port messages */
	public static final String P1TEXTMESSAGE = "Message_text_to_P1_port";
	public static final String P1CODEMESSAGE = "Message_code_to_P1_port";
	public static final String P1TEXT = "Text";
	public static final String P1CODE = "Code";
	
	/** RtuMessage tags for the GPRS modem setup message */
	public static final String GPRS_MODEM_SETUP = "GPRS modem setup";
	public static final String GPRS_APN = "APN";
	public static final String GPRS_USERNAME = "Username";
	public static final String GPRS_PASSWORD = "Password";
	
}
