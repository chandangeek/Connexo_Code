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
    public static final String firmwareUpdateVersionNumberAttributeName = "FirmwareDeviceMessage.upgrade.version";
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
    public static final String keyTActivationStatusAttributeName = "SecurityMessage.keyT.activationstatusT";
    public static final String timeDurationAttributeName = "SecurityMessage.timeduration";
    public static final String executionKeyAttributeName = "SecurityMessage.executionkey";
    public static final String temporaryKeyAttributeName = "SecurityMessage.temporarykey";
    public static final String eventLogResetSealAttributeName = "SecurityMessage.eventlogresetseal";
    public static final String restoreFactorySettingsSealAttributeName = "SecurityMessage.restorefactorysettingsseal";
    public static final String restoreDefaultSettingsSealAttributeName = "SecurityMessage.restoredefaultsettingsseal";
    public static final String statusChangeSealAttributeName = "SecurityMessage.statuschangeseal";
    public static final String remoteConversionParametersConfigSealAttributeName = "SecurityMessage.remoteconversionparametersconfigseal";
    public static final String remoteAnalysisParametersConfigSealAttributeName = "SecurityMessage.remoteanalysisparametersconfigseal";
    public static final String downloadProgramSealAttributeName = "SecurityMessage.downloadprogramseal";
    public static final String restoreDefaultPasswordSealAttributeName = "SecurityMessage.restoredefaultpasswordseal";

    public static final String eventLogResetSealBreakTimeAttributeName = "SecurityMessage.eventlogresetseal.breaktime";
    public static final String restoreFactorySettingsSealBreakTimeAttributeName = "SecurityMessage.restorefactorysettingsseal.breaktime";
    public static final String restoreDefaultSettingsSealBreakTimeAttributeName = "SecurityMessage.restoredefaultsettingsseal.breaktime";
    public static final String statusChangeSealBreakTimeAttributeName = "SecurityMessage.statuschangeseal.breaktime";
    public static final String remoteConversionParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteconversionparametersconfigseal.breaktime";
    public static final String remoteAnalysisParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteanalysisparametersconfigseal.breaktime";
    public static final String downloadProgramSealBreakTimeAttributeName = "SecurityMessage.downloadprogramseal.breaktime";
    public static final String restoreDefaultPasswordSealBreakTimeAttributeName = "SecurityMessage.restoredefaultpasswordseal.breaktime";

    public static final String usernameAttributeName = "username";   // commonly used translation key
    public static final String passwordAttributeName = "password";   // commonly used translation key
    public static final String apnAttributeName = "NetworkConnectivityMessage.apn";
    public static final String whiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.whitelist.phonenumbers";
    public static final String smsCenterPhoneNumberAttributeName = "NetworkConnectivityMessage.smscenter.phonenumber";
    public static final String devicePhoneNumberAttributeName = "NetworkConnectivityMessage.device.phonenumber";
    public static final String ipAddressAttributeName = "NetworkConnectivityMessage.ipaddress";
    public static final String portNumberAttributeName = "NetworkConnectivityMessage.portnumber";
    public static final String wakeupPeriodAttributeName = "NetworkConnectivityMessage.wakeup.period";
    public static final String p1InformationAttributeName = "DisplayDeviceMessage.consumer.p1";
    public static final String normalThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.normalthreshold";
    public static final String emergencyThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencythreshold";
    public static final String overThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.overthresholdduration";
    public static final String emergencyProfileIdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileid";
    public static final String emergencyProfileActivationDateAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileActivationDate";
    public static final String emergencyProfileDurationAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileDuration";
    public static final String emergencyProfileIdLookupAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileidlookup";
    public static final String xmlConfigAttributeName = "AdvancedTestMessage.xmlconfig";
    public static final String loadProfileAttributeName = "loadProfile";
    public static final String fromDateAttributeName = "from";
    public static final String toDateAttributeName = "to";
    public static final String WriteExchangeStatus = "WriteWavecardParameters.writeExchangeStatus";
    public static final String WriteRadioAcknowledge = "WriteWavecardParameters.writeRadioAcknowledge";
    public static final String WriteRadioUserTimeout = "WriteWavecardParameters.writeRadioUserTimeout";

    public static final String enableDSTAttributeName = "ConfigurationChangeDeviceMessage.enable.dst";
    public static final String newPDRAttributeName = "ConfigurationChangeDeviceMessage.pdr";
    public static final String converterTypeAttributeName = "ConfigurationChangeDeviceMessage.convertertype";
    public static final String converterSerialNumberAttributeName = "ConfigurationChangeDeviceMessage.converter.serialnumber";
    public static final String meterTypeAttributeName = "ConfigurationChangeDeviceMessage.metertype";
    public static final String meterCaliberAttributeName = "ConfigurationChangeDeviceMessage.metercaliber";
    public static final String meterSerialNumberAttributeName = "ConfigurationChangeDeviceMessage.meter.serialnumber";
    public static final String gasDensityAttributeName = "ConfigurationChangeDeviceMessage.gas.density";
    public static final String airDensityAttributeName = "ConfigurationChangeDeviceMessage.air.density";
    public static final String relativeDensityAttributeName = "ConfigurationChangeDeviceMessage.relative.density";
    public static final String molecularNitrogenAttributeName = "ConfigurationChangeDeviceMessage.molecularnitrogen.percentage";
    public static final String carbonDioxideAttributeName = "ConfigurationChangeDeviceMessage.carbondioxide.percentage";
    public static final String molecularHydrogenAttributeName = "ConfigurationChangeDeviceMessage.molecularhydrogen.percentage";
    public static final String higherCalorificValueAttributeName = "ConfigurationChangeDeviceMessage.highercalorificvalue";
}