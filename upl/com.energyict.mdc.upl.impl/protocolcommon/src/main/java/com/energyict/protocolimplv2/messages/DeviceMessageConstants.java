package com.energyict.protocolimplv2.messages;

/**
 * Copyrights EnergyICT
 * Date: 19/03/13
 * Time: 8:45
 */
public class DeviceMessageConstants {

    public static final String contactorActivationDateAttributeName = "ContactorDeviceMessage.activationdate";
    public static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    public static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    public static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";
    public static final String firmwareUpdateURLAttributeName = "FirmwareDeviceMessage.upgrade.url";
    public static final String activityCalendarNameAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.name";
    public static final String activityCalendarCodeTableAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.codetable";
    public static final String activityCalendarActivationDateAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.activationdate";
    public static final String encryptionLevelAttributeName = "SecurityMessage.dlmsencryption.encryptionlevel";
    public static final String authenticationLevelAttributeName = "SecurityMessage.dlmsauthentication.authenticationlevel";
    public static final String newEncryptionKeyName = "SecurityMessage.new.encryptionkey";
    public static final String newAuthenticationKeyName = "SecurityMessage.new.authenticationkey";
    public static final String newPasswordName = "SecurityMessage.new.password";
    public static final String username = "username";   // commonly used translation key
    public static final String password = "password";   // commonly used translation key
    public static final String apn = "NetworkConnectivityMessage.apn";
}
