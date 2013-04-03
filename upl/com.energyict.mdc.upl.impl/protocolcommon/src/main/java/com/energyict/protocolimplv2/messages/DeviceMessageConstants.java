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
    public static final String newEncryptionKeyAttributeName = "SecurityMessage.new.encryptionkey";
    public static final String newAuthenticationKeyAttributeName = "SecurityMessage.new.authenticationkey";
    public static final String newPasswordAttributeName = "SecurityMessage.new.password";
    public static final String usernameAttributeName = "username";   // commonly used translation key
    public static final String passwordAttributeName = "password";   // commonly used translation key
    public static final String apnAttributeName = "NetworkConnectivityMessage.apn";
    public static final String whiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.whitelist.phonenumbers";
    public static final String p1InformationAttributeName = "DisplayDeviceMessage.consumer.p1";
    public static final String normalThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.normalthreshold";
    public static final String emergencyThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencythreshold";
    public static final String overThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.overthresholdduration";
    public static final String emergencyProfileIdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileid";
    public static final String emergencyProfileActivationDateAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileActivationDate";
    public static final String emergencyProfileDurationAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileDuration";
    public static final String emergencyProfileIdLookupAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileidlookup";
}
