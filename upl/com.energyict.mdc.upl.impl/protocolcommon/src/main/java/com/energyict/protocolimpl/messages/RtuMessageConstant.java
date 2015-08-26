package com.energyict.protocolimpl.messages;

/**
 * Keeps track of the RtuMessageConstant variables
 * <p/>
 * <b>Note:</b> Do not change the already existing tags, just add new ones if necessary.<br>
 * <b>Note_2:</b> Don't use spaces in the names, these are used in XML-tags
 */
public class RtuMessageConstant {

    /**
     * DeviceMessage tag for connecting load
     */
    public static final String CONNECT_LOAD = "connectLoad";
    public static final String REMOTE_CONNECT = "RemoteConnect";
    /**
     * DeviceMessage tag for disconnecting load
     */
    public static final String DISCONNECT_LOAD = "disconnectLoad";
    public static final String REMOTE_DISCONNECT = "RemoteDisconnect";
    /**
     * DeviceMessage tag to indicate the digital output to apply the connectControl
     */
    public static final String DIGITAL_OUTPUT = "Digital_output";
    /**
     * DeviceMessage tag to indicate the date of the connect/disconnect
     */
    public static final String DISCONNECT_CONTROL_ACTIVATE_DATE = "Activation_date";
    /**
     * DeviceMessage tag to indicate the id of the disconnector object.
     * Use this field when there is more than 1 disconnect control object
     */
    public static final String DISCONNECTOR_OUTPUT_ID = "Output_ID";
    /**
     * DeviceMessage tag to indicate the connect control mode
     */
    public static final String CONNECT_CONTROL_MODE = "Connect_control_mode";
    public static final String CONNECT_MODE = "Mode";

    /**
     * DeviceMessage tag for reading profile data
     */
    public static final String READ_PROFILE = "readProfile";

    /**
     * DeviceMessage tag for reading data on demand
     */
    public static final String READ_ON_DEMAND = "onDemand";

    /**
     * DeviceMessage tag for tou schedule
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
    public static final String TOU_SEND_NEW_TARIFF = "SendNewTariff";
    public static final String TOU_TARIFF_USER_FILE = "TariffUserFile";

    /**
     * DeviceMessage tag for threshold parameters
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
     * DeviceMessage tag for connecting load
     */
    public static final String LOAD_CONTROL_ON = "loadControlOn";
    /**
     * DeviceMessage tag for disconnecting load
     */
    public static final String LOAD_CONTROL_OFF = "loadControlOff";

    /**
     * DeviceMessage tag for changing the repeater mode of a PLC meter
     */
    public static final String REPEATER_MODE = "repeaterMode";

    /**
     * DeviceMessage tag for changing the PLC frequency of the meter
     */
    public static final String CHANGE_PLC_FREQUENCY = "changePLCFreq";
    public static final String FREQUENCY_MARK = "Frequency mark";
    public static final String FREQUENCY_SPACE = "Frequency space";

    /**
     * DeviceMessage tag for upgrading the meters firmware
     */
    public static final String FIRMWARE_UPGRADE = "FirmwareUpgrade";
    public static final String RF_FIRMWARE_UPGRADE = "RFFirmwareUpgrade";
    public static final String FIRMWARE = "UserFileID";
    public static final String FIRMWARE_USER_FILE = "FirmwareUserFile";
    public static final String FIRMWARE_METERS = "GroupID of meters to receive new firmware";
    public static final String FIRMWARE_ACTIVATE_NOW = "Activate_now";
    public static final String FIRMWARE_ACTIVATE_DATE = "Activation_date";
    public static final String ACTIVATE_DATE = "ActivationDate";
    public static final String FIRMWARE_IMAGE_IDENTIFIER = "Image_identifier";

    public static final String FIRMWARE_UPDATE = "FirmwareUpdate";
    public static final String FIRMWARE_UPDATE_INCLUDED_FILE = "IncludedFile";
    public static final String FIRMWARE_UPDATE_ACTIVATION_DATE = "ActivationDate";

    /**
     * DeviceMessage tag for prepaid functionality
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
     * DeviceMessage tags for load limiting
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
     * DeviceMessage tags for XMLConfig
     */
    public static final String XMLCONFIG = "XMLConfig";

    /**
     * DeviceMessage tags for P1 port messages
     */
    public static final String P1TEXTMESSAGE = "Message_text_to_P1_port";
    public static final String P1CODEMESSAGE = "Message_code_to_P1_port";
    public static final String P1TEXT = "Text";
    public static final String P1CODE = "Code";

    /**
     * DeviceMessage tags for the GPRS modem setup message
     */
    public static final String GPRS_MODEM_SETUP = "GPRS_modem_setup";
    public static final String GPRS_MODEM_CREDENTIALS = "GPRS_modem_credentials";
    public static final String GPRS_APN = "APN";
    public static final String GPRS_USERNAME = "Username";
    public static final String GPRS_PASSWORD = "Password";

    /**
     * DeviceMessage tags for MBus setup
     */
    public static final String MBUS_DECOMMISSION = "Decommission";
    public static final String MBUS_ENCRYPTION_KEYS = "Set_Encryption_keys";
    public static final String CRYPTOSERVER_MBUS_ENCRYPTION_KEYS = "Set_Encryption_keys_using_Cryptoserver";
    public static final String MBUS_DEFAULT_KEY = "MBus_Default_Key";
    public static final String MBUS_OPEN_KEY = "Open_Key_Value";
    public static final String MBUS_TRANSFER_KEY = "Transfer_Key_Value";
    public static final String MBUS_DEFAULT_ENCRYPTION_KEY = "Default_Encryption_Key";
    public static final String MBUS_INSTALL = "Mbus_Install";
    public static final String RESET_MBUS_CLIENT = "Reset_MBus_client";
    public static final String MBUS_SERIAL_NUMBER = "Mbus_Serial_Number";
    public static final String MBUS_EQUIPMENT_ID = "Mbus_Equipment_Identifier";
    public static final String MBUS_INSTALL_CHANNEL = "Mbus_channel";
    public static final String MBUS_REMOVE = "Mbus_Remove";
    public static final String MBUS_SET_VIF = "Mbus_Set_VIF";
    public static final String MBUS_INSTALL_DATAREADOUT = "Mbus_DataReadout";

    /**
     * DeviceMessage tags for time set
     */
    public static final String SET_TIME = "Set_Time";
    public static final String SET_TIME_VALUE = "Time_Value";

    /**
     * DeviceMessage tags for Wakeup configuration
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
     * DeviceMessage tags for Making entries
     */
    public static final String ME_MAKING_ENTRIES = "Make_database_entries";
    public static final String ME_START_DATE = "StartDate";
    public static final String ME_NUMBER_OF_ENTRIES = "Number_of_entries";
    public static final String ME_INTERVAL = "Interval";
    public static final String ME_SET_CLOCK_BACK = "Sync_clock_at_end";

    /**
     * DeviceMessage tags for ReadingTestFile
     */
    public static final String TEST_MESSAGE = "Test_Message";
    public static final String TEST_SECURITY_MESSAGE = "Test_Security_Message";
    public static final String TEST_FILE = "Test_File";

    /**
     * DeviceMessage tags for GlobalMeterReset
     */
    public static final String GLOBAL_METER_RESET = "Global_Meter_Reset";
    public static final String RESTORE_FACTORY_SETTINGS = "Restore_Factory_Settings";
    public static final String ENABLE_DISCOVERY_ON_POWER_UP = "Enable_Discover_On_Power_Up";
    public static final String DISABLE_DISCOVERY_ON_POWER_UP = "Disable_Discover_On_Power_Up";

    /**
     * DeviceMessage tags for Webserver activate/deactivate
     */
    public static final String WEBSERVER_ENABLE = "Enable_Webserver";
    public static final String WEBSERVER_DISABLE = "Disable_Webserver";
    public static final String REBOOT = "Reboot";

    /**
     * DeviceMessage tags for Corrected/UnCorrected values in Gas profile
     */
//	public static final String MBUS_CORRECTED_SWITCH = "Correction_switch";
//	public static final String MBUS_CORRECTED_VALUE = "Switch_State";
    public static final String MBUS_CORRECTED_VALUES = "Corrected_values";
    public static final String MBUS_UNCORRECTED_VALUES = "UnCorrected_values";

    /**
     * DeviceMessage tags for authentication and encryption
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
    public static final String AEE_ENABLE_AUTHENTICATION_LEVEL_P0 = "Enable_authentication_level_P0";
    public static final String AEE_DISABLE_AUTHENTICATION_LEVEL_P0 = "Disable_authentication_level_P0";
    public static final String AEE_ENABLE_AUTHENTICATION_LEVEL_P3 = "Enable_authentication_level_P3";
    public static final String AEE_DISABLE_AUTHENTICATION_LEVEL_P3 = "Disable_authentication_level_P3";
    public static final String AEE_SECURITYLEVEL = "SecurityLevel";
    public static final String AEE_AUTHENTICATIONLEVEL = "AuthenticationLevel";
    public static final String AEE_NEW_AUTHENTICATION_KEY = "NewAuthenticationKey";
    public static final String AEE_NEW_ENCRYPTION_KEY = "NewEncryptionKey";
    public static final String AEE_PLAIN_NEW_AUTHENTICATION_KEY = "PlainNewAuthenticationKey";
    public static final String AEE_PLAIN_NEW_ENCRYPTION_KEY = "PlainNewEncryptionKey";

    //Cryptoserver messages
    public static final String SERVICEKEY_HLSSECRET = "ServiceKeyHLSSecret";
    public static final String SERVICEKEY_AK = "ServiceKeyAK";
    public static final String SERVICEKEY_EK = "ServiceKeyEK";

    public static final String SERVICEKEY_PREPAREDDATA = "PreparedData";
    public static final String SERVICEKEY_SIGNATURE = "Signature";
    public static final String SERVICEKEY_VERIFYKEY = "VerificationKey";

    /**
     * DeviceMessage tags for SMS
     */
    public static final String SMS_CHANGE_SMSC = "Change_SMSC";
    public static final String SMS_SMSC_NUMBER = "SMSC_Number";

    /**
     * DeviceMessage tags for other communication parameters
     */
    public static final String CHANGE_DEVICE_PHONE_NUMBER = "Change_Device_Phone_Number";
    public static final String DEVICE_PHONE_NUMBER = "PhoneNumber";

    public static final String DEMAND_RESET = "DemandReset";

    // changed the naming of certain messages for Enexis
    public static final String NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY = "Change_DataTransportEncryptionKey";
    public static final String NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY = "Change_DataTransportAuthenticationKey";

    // ZigBee related HAN Startup Attributes Setup (SAS) message constants
    public static final String CHANGE_HAN_SAS = "Change_HAN_SAS";
    public static final String HAN_SAS_EXTENDED_PAN_ID = "HAN_SAS_EXTENDED_PAN_ID";
    public static final String HAN_SAS_PAN_ID = "HAN_SAS_PAN_ID";
    public static final String HAN_SAS_CHANNEL = "HAN_SAS_PAN_Channel_Mask";
    public static final String HAN_SAS_INSECURE_JOIN = "HAN_SAS_Insecure_Join";

    // ZigBee related Han management messages
    public static final String CREATE_HAN_NETWORK = "Create_Han_Network";
    public static final String REMOVE_HAN_NETWORK = "Remove_Han_Network";

    // ZigBee related massages for mirror object
    public static final String REMOVE_ZIGBEE_MIRROR = "Remove_Mirror";
    public static final String REMOVE_ZIGBEE_MIRROR_IEEE_ADDRESS = "Mirror_IEEE_Address";
    public static final String REMOVE_ZIGBEE_MIRROR_FORCE = "Force_Removal";

    // Join ZigBee slave device related message constants
    public static final String JOIN_ZIGBEE_SLAVE = "Join_ZigBee_Slave";
    public static final String JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE = "Join_ZigBee_Slave_Device";
    public static final String JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS = "ZigBee_IEEE_Address";
    public static final String JOIN_ZIGBEE_SLAVE_LINK_KEY = "ZigBee_Link_Key";
    public static final String JOIN_ZIGBEE_SLAVE_DEVICE_TYPE = "ZigBee_Device_Type";

    // Remove ZigBee slave device related message constants
    public static final String REMOVE_ZIGBEE_SLAVE = "Remove_ZigBee_Slave";
    public static final String REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS = "ZigBee_IEEE_Address";

     // Update ZigBee HAN link key related message constants
    public static final String UPDATE_HAN_LINK_KEY = "Update_HAN_Link_Key";
    public static final String UPDATE_HAN_LINK_KEY_SLAVE_IEEE_ADDRESS = "ZigBee_IEEE_Address";

    // Remove all ZigBee slave devices related message constants
    public static final String REMOVE_ALL_ZIGBEE_SLAVES = "Remove_All_ZigBee_Slaves";

    // Backup ZigBee HAN parameters
    public static final String BACKUP_ZIGBEE_HAN_PARAMETERS = "Backup_ZigBee_Han_Parameters";
    // Restore ZigBee HAN parameters
    public static final String RESTORE_ZIGBEE_HAN_PARAMETERS = "Restore_ZigBee_Han_Parameters";
    public static final String RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID = "Restore_UserFile_ID";

    public static final String READ_ZIGBEE_STATUS = "Read_ZigBee_Status";

    public static final String ZIGBEE_NCP_FIRMWARE_UPDATE = "ZIGBEE_NCP_FIRMWARE_UPDATE";
    public static final String ZIGBEE_NCP_FIRMWARE_UPGRADE = "ZigBeeNCPFirmwareUpgrade";
    public static final String ZIGBEE_NCP_FIRMWARE_USERFILE_ID = "UserFile_ID";
    public static final String ZIGBEE_NCP_FIRMWARE_FILE_ID = "UserFileID";

    // GPRS Modem Ping Setup
    public static final String GPRS_MODEM_PING_SETUP = "GPRS_Modem_Ping_Setup";
    public static final String PING_INTERVAl = "Ping_Interval";
    public static final String PING_IP = "Ping_IP";

    public static final String UPDATE_PRICING_INFORMATION = "Update_Pricing_Information";
    public static final String UPDATE_PRICING_INFORMATION_USERFILE_ID = "Update_Pricing_Information_UserFile_ID";

    public static final String CHANGE_OF_TENANT = "Change_Of_Tenant";
    public static final String CHANGE_OF_TENANT_VALUE = "Change_Of_Tenant_Value";
    public static final String CHANGE_OF_TENANT_ACTIATION_DATE = "Change_Of_Tenant_ActivationDate";

    public static final String CHANGE_OF_SUPPLIER = "Change_Of_Supplier";
    public static final String CHANGE_OF_SUPPLIER_NAME = "Change_Of_Supplier_Name";
    public static final String CHANGE_OF_SUPPLIER_ID = "Change_Of_Supplier_ID";
    public static final String CHANGE_OF_SUPPLIER_ACTIATION_DATE = "Change_Of_Supplier_ActivationDate";
    public static final String CHANGE_ADMINISTRATIVE_STATUS = "Change_Administrative_Status";
    public static final String ADMINISTRATIVE_STATUS = "Status";

    public static final String ALARM_FILTER = "Alarm_Filter";
    public static final String RESET_ALARM_REGISTER = "Reset_Alarm_Register";
    public static final String RESET_ERROR_REGISTER = "Reset_Error_Register";
    public static final String CHANGE_DEFAULT_RESET_WINDOW = "Default_Reset_Window";

    public static final String DEBUG_LOGBOOK = "Debug_Logbook";
    public static final String ELSTER_SPECIFIC_LOGBOOK = "Elster_Specific_Logbook";
    public static final String LOGBOOK_FROM = "From_date";
    public static final String LOGBOOK_TO = "To_date";

    public static String BILLINGRESET = "BillingReset";

    public static final String CONNECTION_MODE = "Connection_Mode";
    public static final String WAKEUP_PARAMETERS = "Wakeup_Parameters";
    public static final String WAKEUP_CALLING_WINDOW_LENGTH = "Calling_Window_Length";
    public static final String WAKEUP_IDLE_TIMEOUT = "Idle_Timeout";

    public static final String PREFERRED_NETWORK_OPERATORS_LIST = "PreferredNetworkOperatorsList";
    public static final String NETWORK_OPERATOR = "Operator";

    public static final String SEND_NEW_PRICE_MATRIX = "SendNewPriceMatrix";
    public static final String PRICE_MATRIX_USER_FILE = "PriceMatrixUserFile";
    public static final String SET_STANDING_CHARGE = "SetStandingChargeAndActivationDate";
    public static final String STANDING_CHARGE = "StandingCharge";
    public static final String SET_CURRENCY = "SetCurrency";
    public static final String CURRENCY = "Currency";
    public static final String ACTIVATION_DATE = "ActivationDate";

    public static final String TENANT_REFERENCE = "Tenant_Reference";
    public static final String SUPPLIER_REFERENCE = "Supplier_Reference";
    public static final String SUPPLIER_ID = "Supplier_Id";
    public static final String SCRIPT_EXECUTED = "Script_executed";


    public static final String SET_CONVERSION_FACTOR = "SetConversionFactorAndActivationDate";
    public static final String CONVERSION_FACTOR = "ConversionFactor";
    public static final String SET_CALORIFIC_VALUE = "SetCalorificValueAndActivationDate";
    public static final String CALORIFIC_VALUE = "CalorificValue";

    public static final String CHANGE_OF_SUPPLIER_IMPORT_ENERGY = "Change_Of_Supplier_ImportEnergy";
    public static final String CHANGE_OF_SUPPLIER_EXPORT_ENERGY = "Change_Of_Supplier_ExportEnergy";
    public static final String DISCONNECT_CONTROL_RECONNECT = "DisconnectControlReconnect";
    public static final String DISCONNECT_CONTROL_DISCONNECT = "DisconnectControlDisconnect";
    public static final String SET_DISCONNECT_CONTROL_MODE = "SetControlMode";
    public static final String CONTROL_MODE = "Control mode (range 0 - 6)";
    public static final String SET_ENGINEER_PIN = "Change_Engineer_PIN";
    public static final String ENGINEER_PIN = "PIN";
    public static final String ENGINEER_PIN_TIMEOUT = "Timeout";
}
