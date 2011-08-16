package com.energyict.protocolimpl.messages;

/**
 * Keeps track of the RtuMessageConstant variables
 * <p/>
 * <b>Note:</b> Do not change the already existing tags, just add new ones if necessary.<br>
 * <b>Note_2:</b> Don't use spaces in the names, these are used in XML-tags
 */
public class RtuMessageConstant {

    /**
     * RtuMessage tag for connecting load
     */
    public static final String CONNECT_LOAD = "connectLoad";
    /**
     * RtuMessage tag for disconnecting load
     */
    public static final String DISCONNECT_LOAD = "disconnectLoad";
    /**
     * RtuMessage tag to indicate the digital output to apply the connectControl
     */
    public static final String DIGITAL_OUTPUT = "Digital_output";
    /**
     * RtuMessage tag to indicate the date of the connect/disconnect
     */
    public static final String DISCONNECT_CONTROL_ACTIVATE_DATE = "Activation_date";
    /**
     * RtuMessage tag to indicate the id of the disconnector object.
     * Use this field when there is more than 1 disconnect control object
     */
    public static final String DISCONNECTOR_OUTPUT_ID = "Output_ID";
    /**
     * RtuMessage tag to indicate the connect control mode
     */
    public static final String CONNECT_CONTROL_MODE = "Connect_control_mode";
    public static final String CONNECT_MODE = "Mode";

    /**
     * RtuMessage tag for reading profile data
     */
    public static final String READ_PROFILE = "readProfile";

    /**
     * RtuMessage tag for reading data on demand
     */
    public static final String READ_ON_DEMAND = "onDemand";

    /**
     * RtuMessage tag for tou schedule
     */
    public static final String TOU_SCHEDULE = "UserFile ID of tariff program";
    public static final String TOU_ACTIVITY_CAL = "Activity_Calendar";
    public static final String TOU_ACTIVITY_NAME = "Calendar_Name";
    public static final String TOU_ACTIVITY_CODE_TABLE = "Code_Table";
    public static final String TOU_ACTIVITY_USER_FILE = "Userfile";
    public static final String TOU_ACTIVATE_CALENDAR = "Activate_Calendar";
    public static final String TOU_ACTIVITY_DATE = "Activation_Date";
    public static final String TOU_SPECIAL_DAYS = "Special_Days";
    public static final String TOU_SPECIAL_DAYS_DELETE = "Special_Days_Delete";
    public static final String TOU_SPECIAL_DAYS_DELETE_ENTRY = "Delete_entry";
    public static final String TOU_SPECIAL_DAYS_CODE_TABLE = "Code_Table";

    public static final String TimeOfUse = "TimeOfUse";

    /**
     * RtuMessage tag for threshold parameters
     */
    public static final String THRESHOLD_PARAMETERS = "thresholdParameters";
    public static final String THRESHOLD_GROUPID = "Threshold GroupId *";
    public static final String PARAMETER_GROUPID = "Parameter GroupId *";
    public static final String THRESHOLD_POWERLIMIT = "Threshold PowerLimit (W)";
    public static final String CONTRACT_POWERLIMIT = "Contractual PowerLimit (W)";
    public static final String APPLY_THRESHOLD = "Apply threshold";
    public static final String CLEAR_THRESHOLD = "Clear threshold - groupID";
    public static final String THRESHOLD_STARTDT = "StartDate (dd/mm/yyyy HH:MM:SS)";
    public static final String THRESHOLD_STOPDT = "EndDate (dd/mm/yyyy HH:MM:SS)";

    /**
     * RtuMessage tag for connecting load
     */
    public static final String LOAD_CONTROL_ON = "loadControlOn";
    /**
     * RtuMessage tag for disconnecting load
     */
    public static final String LOAD_CONTROL_OFF = "loadControlOff";

    /**
     * RtuMessage tag for changing the repeater mode of a PLC meter
     */
    public static final String REPEATER_MODE = "repeaterMode";

    /**
     * RtuMessage tag for changing the PLC frequency of the meter
     */
    public static final String CHANGE_PLC_FREQUENCY = "changePLCFreq";
    public static final String FREQUENCY_MARK = "Frequency mark";
    public static final String FREQUENCY_SPACE = "Frequency space";

    /**
     * RtuMessage tag for upgrading the meters firmware
     */
    public static final String FIRMWARE_UPGRADE = "FirmwareUpgrade";
    public static final String RF_FIRMWARE_UPGRADE = "RFFirmwareUpgrade";
    public static final String FIRMWARE = "UserFileID";
    public static final String FIRMWARE_METERS = "GroupID of meters to receive new firmware";
    public static final String FIRMWARE_ACTIVATE_NOW = "Activate_now";
    public static final String FIRMWARE_ACTIVATE_DATE = "Activation_date";

    /**
     * RtuMessage tag for prepaid functionality
     */
    public static final String PREPAID_CONFIGURED = "Configure_Prepaid_functionality";
    public static final String PREPAID_ADD = "Add_Prepaid_credit";
    public static final String PREPAID_READ = "Read_Prepaid_credit";
    public static final String PREPAID_ENABLE = "Enable_Prepaid_functionality";
    public static final String PREPAID_DISABLE = "Disable_Prepaid_functionality";
    public static final String PREPAID_BUDGET = "Budget";
    public static final String PREPAID_THRESHOLD = "Threshold";
    public static final String PREPAID_MULTIPLIER = "Multiplier_tariff_";
    public static final String PREPAID_READ_FREQUENCY = "Read_frequency";

    /**
     * RtuMessage tags for load limiting
     */
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

    /**
     * RtuMessage tags for XMLConfig
     */
    public static final String XMLCONFIG = "XMLConfig";

    /**
     * RtuMessage tags for P1 port messages
     */
    public static final String P1TEXTMESSAGE = "Message_text_to_P1_port";
    public static final String P1CODEMESSAGE = "Message_code_to_P1_port";
    public static final String P1TEXT = "Text";
    public static final String P1CODE = "Code";

    /**
     * RtuMessage tags for the GPRS modem setup message
     */
    public static final String GPRS_MODEM_SETUP = "GPRS_modem_setup";
    public static final String GPRS_MODEM_CREDENTIALS = "GPRS_modem_credentials";
    public static final String GPRS_APN = "APN";
    public static final String GPRS_USERNAME = "Username";
    public static final String GPRS_PASSWORD = "Password";

    /**
     * RtuMessage tags for MBus setup
     */
    public static final String MBUS_DECOMMISSION = "Decommission";
    public static final String MBUS_ENCRYPTION_KEYS = "Set_Encryption_keys";
    public static final String MBUS_OPEN_KEY = "Open_Key_Value";
    public static final String MBUS_TRANSFER_KEY = "Transfer_Key_Value";
    public static final String MBUS_DEFAULT_ENCRYPTION_KEY = "Default_Encryption_Key";
    public static final String MBUS_INSTALL = "Mbus_Install";
    public static final String MBUS_EQUIPMENT_ID = "Mbus_Equipment_Identifier";
    public static final String MBUS_INSTALL_CHANNEL = "Mbus_channel";
    public static final String MBUS_REMOVE = "Mbus_Remove";
    public static final String MBUS_SET_VIF = "Mbus_Set_VIF";
    public static final String MBUS_INSTALL_DATAREADOUT = "Mbus_DataReadout";

    /**
     * RtuMessage tags for time set
     */
    public static final String SET_TIME = "Set_Time";
    public static final String SET_TIME_VALUE = "Time_Value";

    /**
     * RtuMessage tags for Wakeup configuration
     */
    public static final String WAKEUP_INACT_TIMEOUT = "Inactivity_timeout";
    public static final String WAKEUP_ADD_WHITELIST = "Phonenumbers_to_add";
    public static final String WAKEUP_DELETE_WHITELIST = "Phonenumber_to_delete";
    public static final String WAKEUP_CLEAR_WHITELIST = "Clear_whiteList";
    public static final String WAKEUP_GENERAL_RESTRICTION = "General_restriction";
    public static final String WAKEUP_ACTIVATE = "Activate_the_wakeup_mechanism";
    public static final String WAKEUP_DEACTIVATE = "Deactive_the_wakeup_mechanism";
    public static final String WAKEUP_NR = "Phonenumber";
    public static final String WAKEUP_MANAGED_NR = "ManagedPhonenumber";
    public static final String WAKEUP_NR1 = "Phonenumber1";
    public static final String WAKEUP_NR2 = "Phonenumber2";
    public static final String WAKEUP_NR3 = "Phonenumber3";
    public static final String WAKEUP_NR4 = "Phonenumber4";
    public static final String WAKEUP_NR5 = "Phonenumber5";

    /**
     * RtuMessage tags for Making entries
     */
    public static final String ME_MAKING_ENTRIES = "Make_database_entries";
    public static final String ME_START_DATE = "StartDate";
    public static final String ME_NUMBER_OF_ENTRIES = "Number_of_entries";
    public static final String ME_INTERVAL = "Interval";
    public static final String ME_SET_CLOCK_BACK = "Sync_clock_at_end";

    /**
     * RtuMessage tags for ReadingTestFile
     */
    public static final String TEST_MESSAGE = "Test_Message";
    public static final String TEST_FILE = "Test_File";

    /**
     * RtuMessage tags for GlobalMeterReset
     */
    public static final String GLOBAL_METER_RESET = "Global_Meter_Reset";

    /**
     * RtuMessage tags for Corrected/UnCorrected values in Gas profile
     */
//	public static final String MBUS_CORRECTED_SWITCH = "Correction_switch";
//	public static final String MBUS_CORRECTED_VALUE = "Switch_State";
    public static final String MBUS_CORRECTED_VALUES = "Corrected_values";
    public static final String MBUS_UNCORRECTED_VALUES = "UnCorrected_values";

    /**
     * RtuMessage tags for authentication and encryption
     */
    public static final String AEE_CHANGE_HLS_SECRET = "Change_HLS_Secret";
    public static final String AEE_CHANGE_LLS_SECRET = "Change_LLS_Secret";
    public static final String AEE_HLS_SECRET = "HLSSecret";
    public static final String AEE_LLS_SECRET = "LLSSecret";
    public static final String AEE_CHANGE_GLOBAL_KEY = "Change_Global_Key";
    public static final String AEE_GLOBAL_KEY = "GlobalKey";
    public static final String AEE_CHANGE_AUTHENTICATION_KEY = "AuthenticationKey";
    public static final String AEE_ACTIVATE_SECURITY = "Activate_dataTransport_Security";
    public static final String AEE_CHANGE_AUTHENTICATION_LEVEL = "Change_authentication_level";
    public static final String AEE_SECURITYLEVEL = "SecurityLevel";
    public static final String AEE_AUTHENTICATIONLEVEL = "AuthenticationLevel";

    /**
     * RtuMessage tags for SMS
     */
    public static final String SMS_CHANGE_SMSC = "Change_SMSC";
    public static final String SMS_SMSC_NUMBER = "SMSC_Number";

    /**
     * RtuMessage tags for other communication parameters
     */
    public static final String CHANGE_DEVICE_PHONE_NUMBER = "Change_Device_Phone_Number";
    public static final String DEVICE_PHONE_NUMBER = "PhoneNumber";

    public static final String DEMAND_RESET = "DemandReset";

    // changed the naming of certain messages for Enexis
    public static final String NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY = "Change_DataTransportEncryptionKey";
    public static final String NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY = "Change_DataTransportAuthenticationKey";

    // ZigBee related HAN Startup Attributes Setup (SAS) message constants
    public static final String CHANGE_HAN_SAS = "Change_HAN_SAS";
    public static final String HAN_SAS_PAN_ID = "HAN_SAS_PAN_ID";
    public static final String HAN_SAS_CHANNEL = "HAN_SAS_PAN_Channel";
    public static final String HAN_SAS_INSECURE_JOIN = "HAN_SAS_Insecure_Join";

    // ZigBee related Han management messages
    public static final String CREATE_HAN_NETWORK = "Create_Han_Network";
    public static final String REMOVE_HAN_NETWORK = "Remove_Han_Network";

    // Join ZigBee slave device related message constants
    public static final String JOIN_ZIGBEE_SLAVE = "Join_ZigBee_Slave";
    public static final String JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS = "ZigBee_IEEE_Address";
    public static final String JOIN_ZIGBEE_SLAVE_LINK_KEY = "ZigBee_Link_Key";

    // Remove ZigBee slave device related message constants
    public static final String REMOVE_ZIGBEE_SLAVE = "Remove_ZigBee_Slave";
    public static final String REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS = "ZigBee_IEEE_Address";

    // Remove all ZigBee slave devices related message constants
    public static final String REMOVE_ALL_ZIGBEE_SLAVES = "Remove_All_ZigBee_Slaves";

    // Backup ZigBee HAN parameters
    public static final String BACKUP_ZIGBEE_HAN_PARAMETERS = "Backup_ZigBee_Han_Parameters";
    // Restore ZigBee HAN parameters
    public static final String RESTORE_ZIGBEE_HAN_PARAMETERS = "Restore_ZigBee_Han_Parameters";
    public static final String RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID = "Restore_UserFile_ID";

    public static final String UPDATE_PRICING_INFORMATION = "Update_Pricing_Information";
    public static final String UPDATE_PRICING_INFORMATION_USERFILE_ID = "Update_Pricing_Information_UserFile_ID";

    public static final String CHANGE_OF_TENANT = "Change_Of_Tenant";
    public static final String CHANGE_OF_TENANT_VALUE = "Change_Of_Tenant_Value";
    public static final String CHANGE_OF_TENANT_ACTIATION_DATE = "Change_Of_Tenant_ActivationDate";

    public static final String CHANGE_OF_SUPPLIER = "Change_Of_Supplier";
    public static final String CHANGE_OF_SUPPLIER_NAME = "Change_Of_Supplier_Name";
    public static final String CHANGE_OF_SUPPLIER_ID = "Change_Of_Supplier_ID";
    public static final String CHANGE_OF_SUPPLIER_ACTIATION_DATE = "Change_Of_Supplier_ActivationDate";
}
