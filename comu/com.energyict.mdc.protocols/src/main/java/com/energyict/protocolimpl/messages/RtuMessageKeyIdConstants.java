package com.energyict.protocolimpl.messages;

/**
 * Constants class for naming the Device message KEYIDs
 * <p/>
 * <b>Note:</b> Do not change the already existing tags, just add new ones if necessary.
 */
public class RtuMessageKeyIdConstants {

    public static final String XLMCONFIG = "XMLConfig";
    public static final String FIRMWARE = "Upgrade Firmware";
    public static final String RFFIRMWARE = "Upgrade RF-Firmware";
    public static final String CONSUMERTEXTP1 = "Consumer message Text to port P1";
    public static final String CONSUMERCODEP1 = "Consumer message Code to port P1";
    public static final String DISCONNECT = "Disconnect";
    public static final String CONNECT = "Connect";
    public static final String CONNECTCONTROLMODE = "ConnectControl mode";
    public static final String LOADLIMITCONFIG = "Configure Loadlimiting parameters";
    public static final String LOADLIMITCLEAR = "Clear the Loadlimit configuration";
    public static final String LOADLIMITGROUPID = "Set emergency profile group id's";
    public static final String ACTIVITYCALENDAR = "Select the Activity Calendar";
    public static final String ACTIVATEACTIVITYCALENDAR = "Activate the passive Calendar";
    public static final String SPECIALDAYS = "Select the Special days Calendar";
    public static final String SETTIME = "Set the meterTime to a specific time";
    public static final String CREATEDATABASEENTRIES = "Create entries in the meters database";
    public static final String GPRSMODEMSETUP = "Change GPRS modem setup parameters";
    public static final String GPRSCREDENTIALS = "Change GPRS Modem credentials";
    public static final String TESTMESSAGE = "Test Message";
    public static final String TESTSECURITYMESSAGE = "Test Security Message";
    public static final String GLOBALRESET = "Global Meter Reset";
    public static final String FACTORYSETTINGS = "Restore factory settings";
    public static final String ENABLE_DISCOVERY_ON_POWER_UP = "Enable discovery on power up";
    public static final String DISABLE_DISCOVERY_ON_POWER_UP = "Disable discovery on power up";
    public static final String SETWHITELIST = "Set phone numbers to whitelist";
    public static final String CHANGEHLSSECRET = "Change the HLS secret";
    public static final String CHANGELLSSECRET = "Change the LLS secret";
    public static final String CHANGEGLOBALKEY = "Change global key";
    public static final String CHANGEAUTHENTICATIONKEY = "Change authentication key";
    public static final String READONDEMAND = "ReadOnDemand";
    public static final String MBUSINSTALL = "Install Mbus device";
    public static final String MBUSDECOMMISSION = "Decommission";
    public static final String MBUSENCRYPTIONKEY = "Set Encryption keys";
    public static final String CRYPTO_MBUSENCRYPTIONKEY = "Set Encryption keys using Cryptoserver";
    //	public static final String MBUSGASCORRECTION = "Correction switch";
    public static final String MBUSVALUESCORRECTED = "Use Corrected Mbus values";
    public static final String MBUSVALUESUNCORRECTED = "Use UnCorrected Mbus values";
    public static final String ACTIVATESMSWAKEUP = "Activate SMS wakeup";
    public static final String DEACTIVATESMSWAKEUP = "Deactive SMS wakeup";
    public static final String ACTIVATESCDWAKEUP = "Activate CSD wakeup";
    public static final String ACTIVATE_SECURITY = "Activate dataTransport security";
    public static final String CHANGE_AUTHENTICATION_LEVEL = "Change the authentication Level";
    public static final String ENABLE_AUTHENTICATION_LEVEL_P3 = "Enable the authentication Level for P3";
    public static final String DISABLE_AUTHENTICATION_LEVEL_P3 = "Disable the authentication Level for P3";
    public static final String ENABLE_AUTHENTICATION_LEVEL_P0 = "Enable the authentication Level for P0";
    public static final String DISABLE_AUTHENTICATION_LEVEL_P0 = "Disable the authentication Level for P0";
    public static final String SMS_CHANGE_SMSC = "Change SMS Center number";
    public static final String CHANGE_DEVICE_PHONE_NUMBER = "Change device phone number";
    public static final String DEMANDRESET = "Demand Reset";

    // changed the naming of certain messages for Enexis
    public static final String NTA_CHANGEDATATRANSPORTENCRYPTIONKEY = "Change DataTransportEncryptionKey";
    public static final String NTA_CHANGEDATATRANSPORTAUTHENTICATIONKEY = "Change DataTransportAuthenticationKey";

    public static final String CHANGE_ZIGBEE_HAN_SAS = "Change ZigBee HAN Startup Attribute Setup (SAS)";
    public static final String CREATE_HAN = "Create Han Network";
    public static final String REMOVE_HAN = "Remove Han Network";
    public static final String REMOVE_MIRROR = "Remove mirror";
    public static final String JOIN_ZIGBEE_SLAVE = "Join ZigBee slave device";
    public static final String REMOVE_ZIGBEE_SLAVE = "Remove ZigBee slave device";
    public static final String REMOVE_ALL_ZIGBEE_SLAVES = "Remove all ZigBee slave devices";
    public static final String BACKUP_ZIGBEE_HAN_PARAMETERS = "Backup ZigBee HAN Parameters";
    public static final String RESTORE_ZIGBEE_HAN_PARAMETERS = "Restore ZigBee HAN Parameters";
    public static final String READ_ZIGBEE_STATUS = "Read ZigBee status";
    public static final String ZIGBEE_NCP_FIRMWARE_UPGRADE = "Zigbee NCP firmware update";
    public static final String GPRS_MODEM_PING_SETUP = "GPRS Modem Ping Setup";
    public static final String UPDATE_PRICING_INFORMATION = "Update Pricing Information";
    public static final String CHANGE_OF_TENANT = "Change Of Tenant";
    public static final String CHANGE_OF_SUPPLIER = "Change Of Supplier";
    public static final String WEBSERVER_ENABLE = "Enable webserver";
    public static final String WEBSERVER_DISABLE = "Disable webserver";
    public static final String REBOOT = "Reboot device";

    public static final String CHANGE_ADMINISTRATIVE_STATUS = "Change Administrative status";

    public static final String ALARMFILTER = "Set Alarm filter";
    public static final String RESETALARMREGISTER = "Reset Alarm register";
    public static final String RESETERRORREGISTER = "Reset Error register";
    public static final String CHANGEDEFAULTRESETWINDOW = "Change default reset window";
}
