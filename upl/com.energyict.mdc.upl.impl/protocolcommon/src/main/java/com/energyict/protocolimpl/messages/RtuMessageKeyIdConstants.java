package com.energyict.protocolimpl.messages;

/**
 * Constants class for naming the Rtu message KEYIDs
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
    public static final String SPECIALDAYS = "Select the Special days Calendar";
    public static final String SETTIME = "Set the meterTime to a specific time";
    public static final String CREATEDATABASEENTRIES = "Create entries in the meters database";
    public static final String GPRSMODEMSETUP = "Change GPRS modem setup parameters";
    public static final String GPRSCREDENTIALS = "Change GPRS Modem credentials";
    public static final String TESTMESSAGE = "Test Message";
    public static final String GLOBALRESET = "Global Meter Reset";
    public static final String SETWHITELIST = "Set phonenumbers to whitelist";
    public static final String CHANGEHLSSECRET = "Change the HLS secret";
    public static final String CHANGELLSSECRET = "Change the LLS secret";
    public static final String CHANGEGLOBALKEY = "Change global key";
    public static final String CHANGEAUTHENTICATIONKEY = "Change authentication key";
    public static final String READONDEMAND = "ReadOnDemand";
    public static final String MBUSINSTALL = "Install Mbus device";
    public static final String MBUSDECOMMISSION = "Decommission";
    public static final String MBUSENCRYPTIONKEY = "Set Encryption keys";
//	public static final String MBUSGASCORRECTION = "Correction switch";
    public static final String MBUSVALUESCORRECTED = "Use Corrected Mbus values";
    public static final String MBUSVALUESUNCORRECTED = "Use UnCorrected Mbus values";
    public static final String ACTIVATESMSWAKEUP = "Activate SMS wakeup";
    public static final String DEACTIVATESMSWAKEUP = "Deactive SMS wakeup";
    public static final String ACTIVATESCDWAKEUP = "Activate CSD wakeup";
    public static final String ACTIVATE_SECURITY = "Activate dataTransport security";
    public static final String CHANGE_AUTHENTICATION_LEVEL = "Change the authentication Level";
    public static final String SMS_CHANGE_SMSC = "Change SMS Center number";
    public static final String CHANGE_DEVICE_PHONE_NUMBER = "Change device phone number";
}
