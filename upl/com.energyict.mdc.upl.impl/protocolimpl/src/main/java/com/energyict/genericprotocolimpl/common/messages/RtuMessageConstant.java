package com.energyict.genericprotocolimpl.common.messages;


public class RtuMessageConstant {
    
    /** RtuMessage tag for connecting load */
    public final static String CONNECT_LOAD = "connectLoad";
    /** RtuMessage tag for disconnecting load */
    public final static String DISCONNECT_LOAD = "disconnectLoad";
    /** RtuMessage tag to indicate the digital output to apply the connectControl*/
    public final static String DIGITAL_OUTPUT = "Digital_output";
    /** RtuMessage tag to indicate the date of the connect/disconnect */
	public static final String DISCONNECT_CONTROL_ACTIVATE_DATE = "Activation_date";
	/** RtuMessage tag to indicate the connect control mode */
	public static final String CONNECT_CONTROL_MODE = "Connect_control_mode";
	public static final String CONNECT_MODE = "Mode";
    
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
    public final static String TOU_SPECIAL_DAYS_DELETE = "Special_Days_Delete";
    public final static String TOU_SPECIAL_DAYS_DELETE_ENTRY = "Delete_entry";
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
	public static final String GPRS_MODEM_SETUP = "GPRS_modem_setup";
	public static final String GPRS_APN = "APN";
	public static final String GPRS_USERNAME = "Username";
	public static final String GPRS_PASSWORD = "Password";
	
	/** RtuMessage tags for MBus setup */
	public static final String MBUS_DECOMMISSION = "Decommission";
	public static final String MBUS_ENCRYPTION_KEYS = "Set_Encryption_keys";
	public static final String MBUS_OPEN_KEY = "Open_Key_Value";
	public static final String MBUS_TRANSFER_KEY = "Transfer_Key_Value";
	public static final String MBUS_INSTALL = "Mbus_Install";
	public static final String MBUS_REMOVE = "Mbus_Remove";
	public static final String MBUS_SET_VIF = "Mbus_Set_VIF";
	public static final String MBUS_INSTALL_DATAREADOUT = "Mbus_DataReadout";
	
	/** RtuMessage tags for time set */
	public static final String SET_TIME = "Set_Time";
	public static final String SET_TIME_VALUE = "Time_Value";
	
	/** RtuMessage tags for Wakeup configuration */
	public static final String WAKEUP_INACT_TIMEOUT = "Inactivity_timeout";
	public static final String WAKEUP_ADD_WHITELIST = "Phonenumbers_to_add";
	public static final String WAKEUP_DELETE_WHITELIST = "Phonenumber_to_delete";
	public static final String WAKEUP_CLEAR_WHITELIST = "Clear_whiteList";
	public static final String WAKEUP_GENERAL_RESTRICTION = "General_restriction";
	public static final String WAKEUP_ACTIVATE = "Activate_the_wakeup_mechanism";
	public static final String WAKEUP_NR1 = "Phonenumber1";
	public static final String WAKEUP_NR2 = "Phonenumber2";
	public static final String WAKEUP_NR3 = "Phonenumber3";
	public static final String WAKEUP_NR4 = "Phonenumber4";
	public static final String WAKEUP_NR5 = "Phonenumber5";
	
	/** RtuMessage tags for Making entries */
	public static final String ME_MAKING_ENTRIES ="Make_database_entries";
	public static final String ME_START_DATE = "StartDate";
	public static final String ME_NUMBER_OF_ENTRIES = "Number_of_entries";
	public static final String ME_INTERVAL = "Interval";
	public static final String ME_SET_CLOCK_BACK = "Sync_clock_at_end";
	
	/** RtuMessage tags for ReadingTestFile */
	public static final String TEST_MESSAGE = "Test_Message";
	public static final String TEST_FILE = "Test_File";
	
	/** RtuMessage tags for GlobalMeterReset */
	public static final String GLOBAL_METER_RESET = "Global_Meter_Reset";
	
	/** RtuMessage tags for Corrected/UnCorrected values in Gas profile */
	public static final String MBUS_CORRECTED_SWITCH = "Correction_switch";
	public static final String MBUS_CORRECTED_VALUE = "Switch_State";
	
	/** RtuMessage tags for authentication and encryption */
	public static final String AEE_CHANGE_HLS_SECRET = "Change_HLS_Secret";
	public static final String AEE_HLS_SECRET = "HLSSecret";
	public static final String AEE_CHANGE_GLOBAL_KEY = "Change_Global_Key";
	public static final String AEE_GLOBAL_KEY = "GlobalKey";
	public static final String AEE_GLOBAL_KEY_TYPE = "GlobalKeyType";
}
