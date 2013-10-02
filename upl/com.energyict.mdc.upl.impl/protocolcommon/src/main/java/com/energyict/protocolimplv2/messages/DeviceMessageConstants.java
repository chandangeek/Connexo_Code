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
    public static final String meterTimeAttributeName = "ClockDeviceMessage.meterTime";
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
    public static final String displayMessageAttributeName = "DisplayDeviceMessage.displayMessage";
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
    public static final String powerQualityThresholdAttributeName = "ConfigurationChangeDeviceMessage.powerQualityThreshold";
    public static final String WriteExchangeStatus = "WriteWavecardParameters.writeExchangeStatus";
    public static final String WriteRadioAcknowledge = "WriteWavecardParameters.writeRadioAcknowledge";
    public static final String WriteRadioUserTimeout = "WriteWavecardParameters.writeRadioUserTimeout";

    public static final String SetDescriptionAttributeName = "ConfigurationChangeDeviceMessage.SetDescriptionAttributeName";
    public static final String SetIntervalInSecondsAttributeName = "ConfigurationChangeDeviceMessage.SetIntervalInSecondsAttributeName";
    public static final String SetUpgradeUrlAttributeName = "ConfigurationChangeDeviceMessage.SetUpgradeUrlAttributeName";
    public static final String SetUpgradeOptionsAttributeName = "ConfigurationChangeDeviceMessage.SetUpgradeOptionsAttributeName";
    public static final String SetDebounceTresholdAttributeName = "ConfigurationChangeDeviceMessage.SetDebounceTresholdAttributeName";
    public static final String SetTariffMomentAttributeName = "ConfigurationChangeDeviceMessage.SetTariffMomentAttributeName";
    public static final String SetCommOffsetAttributeName = "ConfigurationChangeDeviceMessage.SetCommOffsetAttributeName";
    public static final String SetAggIntvAttributeName = "ConfigurationChangeDeviceMessage.SetAggIntvAttributeName";
    public static final String SetPulseTimeTrueAttributeName = "ConfigurationChangeDeviceMessage.SetPulseTimeTrueAttributeName";

    public static final String SetProxyServerAttributeName = "NetworkConnectivityMessage.SetProxyServerAttributeName";
    public static final String SetProxyUsernameAttributeName = "NetworkConnectivityMessage.SetProxyUsernameAttributeName";
    public static final String SetProxyPasswordAttributeName = "NetworkConnectivityMessage.SetProxyPasswordAttributeName";
    public static final String SetDHCPAttributeName = "NetworkConnectivityMessage.SetDHCPAttributeName";
    public static final String SetDHCPTimeoutAttributeName = "NetworkConnectivityMessage.SetDHCPTimeoutAttributeName";
    public static final String SetIPAddressAttributeName = "NetworkConnectivityMessage.SetIPAddressAttributeName";
    public static final String SetSubnetMaskAttributeName = "NetworkConnectivityMessage.SetSubnetMaskAttributeName";
    public static final String SetGatewayAttributeName = "NetworkConnectivityMessage.SetGatewayAttributeName";
    public static final String SetNameServerAttributeName = "NetworkConnectivityMessage.SetNameServerAttributeName";
    public static final String SetHttpPortAttributeName = "NetworkConnectivityMessage.SetHttpPortAttributeName";

    public static final String SetDSTAttributeName = "ClockDeviceMessage.SetDSTAttributeName";
    public static final String SetTimezoneAttributeName = "ClockDeviceMessage.SetTimezoneAttributeName";
    public static final String SetTimeAdjustmentAttributeName = "ClockDeviceMessage.SetTimeAdjustmentAttributeName";
    public static final String SetNTPServerAttributeName = "ClockDeviceMessage.SetNTPServerAttributeName";
    public static final String SetRefreshClockEveryAttributeName = "ClockDeviceMessage.SetRefreshClockEveryAttributeName";
    public static final String SetNTPOptionsAttributeName = "ClockDeviceMessage.SetNTPOptionsAttributeName";

    public static final String SetEIWebPasswordAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebPasswordAttributeName";
    public static final String SetEIWebPageAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebPageAttributeName";
    public static final String SetEIWebFallbackPageAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebFallbackPageAttributeName";
    public static final String SetEIWebSendEveryAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebSendEveryAttributeName";
    public static final String SetEIWebCurrentIntervalAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebCurrentIntervalAttributeName";
    public static final String SetEIWebDatabaseIDAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebDatabaseIDAttributeName";
    public static final String SetEIWebOptionsAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebOptionsAttributeName";


    public static final String SetPOPUsernameAttributeName = "MailConfigurationDeviceMessage.SetPOPUsernameAttributeName";
    public static final String SetPOPPasswordAttributeName = "MailConfigurationDeviceMessage.SetPOPPasswordAttributeName";
    public static final String SetPOPHostAttributeName = "MailConfigurationDeviceMessage.SetPOPHostAttributeName";
    public static final String SetPOPReadMailEveryAttributeName = "MailConfigurationDeviceMessage.SetPOPReadMailEveryAttributeName";
    public static final String SetPOP3OptionsAttributeName = "MailConfigurationDeviceMessage.SetPOP3OptionsAttributeName";
    public static final String SetSMTPFromAttributeName = "MailConfigurationDeviceMessage.SetSMTPFromAttributeName";
    public static final String SetSMTPToAttributeName = "MailConfigurationDeviceMessage.SetSMTPToAttributeName";
    public static final String SetSMTPConfigurationToAttributeName = "MailConfigurationDeviceMessage.SetSMTPConfigurationToAttributeName";
    public static final String SetSMTPServerAttributeName = "MailConfigurationDeviceMessage.SetSMTPServerAttributeName";
    public static final String SetSMTPDomainAttributeName = "MailConfigurationDeviceMessage.SetSMTPDomainAttributeName";
    public static final String SetSMTPSendMailEveryAttributeName = "MailConfigurationDeviceMessage.SetSMTPSendMailEveryAttributeName";
    public static final String SetSMTPCurrentIntervalAttributeName = "MailConfigurationDeviceMessage.SetSMTPCurrentIntervalAttributeName";
    public static final String SetSMTPDatabaseIDAttributeName = "MailConfigurationDeviceMessage.SetSMTPDatabaseIDAttributeName";
    public static final String SetSMTPOptionsAttributeName = "MailConfigurationDeviceMessage.SetSMTPOptionsAttributeName";

    public static final String SetSmsDataNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsDataNbrAttributeName";
    public static final String SetSmsAlarmNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsAlarmNbrAttributeName";
    public static final String SetSmsEveryAttributeName = "SMSConfigurationDeviceMessage.SetSmsEveryAttributeName";
    public static final String SetSmsNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsNbrAttributeName";
    public static final String SetSmsCorrectionAttributeName = "SMSConfigurationDeviceMessage.SetSmsCorrectionAttributeName";
    public static final String SetSmsConfigAttributeName = "SMSConfigurationDeviceMessage.SetSmsConfigAttributeName";
    public static final String SetDLMSDeviceIDAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSDeviceIDAttributeName";
    public static final String SetDLMSMeterIDAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSMeterIDAttributeName";
    public static final String SetDLMSPasswordAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSPasswordAttributeName";
    public static final String SetDLMSIdleTimeAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSIdleTimeAttributeName";

    public static final String SetDukePowerIDAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerIDAttributeName";
    public static final String SetDukePowerPasswordAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerPasswordAttributeName";
    public static final String SetDukePowerIdleTimeAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerIdleTimeAttributeName";

    public static final String SetDialCommandAttributeName = "ModemConfigurationDeviceMessage.SetDialCommandAttributeName";
    public static final String SetModemInit1AttributeName = "ModemConfigurationDeviceMessage.SetModemInit1AttributeName";
    public static final String SetModemInit2AttributeName = "ModemConfigurationDeviceMessage.SetModemInit2AttributeName";
    public static final String SetModemInit3AttributeName = "ModemConfigurationDeviceMessage.SetModemInit3AttributeName";
    public static final String SetPPPBaudRateAttributeName = "ModemConfigurationDeviceMessage.SetPPPBaudRateAttributeName";
    public static final String SetModemtypeAttributeName = "ModemConfigurationDeviceMessage.SetModemtypeAttributeName";
    public static final String SetResetCycleAttributeName = "ModemConfigurationDeviceMessage.SetResetCycleAttributeName";

    public static final String SetISP1PhoneAttributeName = "PPPConfigurationDeviceMessage.SetISP1PhoneAttributeName";
    public static final String SetISP1UsernameAttributeName = "PPPConfigurationDeviceMessage.SetISP1UsernameAttributeName";
    public static final String SetISP1PasswordAttributeName = "PPPConfigurationDeviceMessage.SetISP1PasswordAttributeName";
    public static final String SetISP1TriesAttributeName = "PPPConfigurationDeviceMessage.SetISP1TriesAttributeName";
    public static final String SetISP2PhoneAttributeName = "PPPConfigurationDeviceMessage.SetISP2PhoneAttributeName";
    public static final String SetISP2UsernameAttributeName = "PPPConfigurationDeviceMessage.SetISP2UsernameAttributeName";
    public static final String SetISP2PasswordAttributeName = "PPPConfigurationDeviceMessage.SetISP2PasswordAttributeName";
    public static final String SetISP2TriesAttributeName = "PPPConfigurationDeviceMessage.SetISP2TriesAttributeName";
    public static final String SetPPPIdleTimeoutAttributeName = "PPPConfigurationDeviceMessage.SetPPPIdleTimeoutAttributeName";
    public static final String SetPPPRetryIntervalAttributeName = "PPPConfigurationDeviceMessage.SetPPPRetryIntervalAttributeName";
    public static final String SetPPPOptionsAttributeName = "PPPConfigurationDeviceMessage.SetPPPOptionsAttributeName";

    public static final String SetFunctionAttributeName = "ChannelConfigurationDeviceMessage.SetFunctionAttributeName";
    public static final String SetParametersAttributeName = "ChannelConfigurationDeviceMessage.SetParametersAttributeName";
    public static final String SetNameAttributeName = "ChannelConfigurationDeviceMessage.SetNameAttributeName";
    public static final String SetUnitAttributeName = "ChannelConfigurationDeviceMessage.SetUnitAttributeName";

    public static final String SetSumMaskAttributeName = "TotalizersConfigurationDeviceMessage.SetSumMaskAttributeName";
    public static final String SetSubstractMaskAttributeName = "TotalizersConfigurationDeviceMessage.SetSubstractMaskAttributeName";

    public static final String SetActiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.SetActiveChannelAttributeName";
    public static final String SetReactiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.SetReactiveChannelAttributeName";
    public static final String SetTimeBaseAttributeName = "PeakShaverConfigurationDeviceMessage.SetTimeBaseAttributeName";
    public static final String SetPOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetPOutAttributeName";
    public static final String SetPInAttributeName = "PeakShaverConfigurationDeviceMessage.SetPInAttributeName";
    public static final String SetDeadTimeAttributeName = "PeakShaverConfigurationDeviceMessage.SetDeadTimeAttributeName";
    public static final String SetAutomaticAttributeName = "PeakShaverConfigurationDeviceMessage.SetAutomaticAttributeName";
    public static final String SetCyclicAttributeName = "PeakShaverConfigurationDeviceMessage.SetCyclicAttributeName";
    public static final String SetInvertAttributeName = "PeakShaverConfigurationDeviceMessage.SetInvertAttributeName";
    public static final String SetAdaptSetpointAttributeName = "PeakShaverConfigurationDeviceMessage.SetAdaptSetpointAttributeName";
    public static final String SetInstantAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetInstantAnalogOutAttributeName";
    public static final String SetPredictedAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetPredictedAnalogOutAttributeName";
    public static final String SetpointAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetpointAnalogOutAttributeName";
    public static final String SetDifferenceAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetDifferenceAnalogOutAttributeName";
    public static final String SetTariffAttributeName = "PeakShaverConfigurationDeviceMessage.SetTariffAttributeName";
    public static final String SetResetLoadsAttributeName = "PeakShaverConfigurationDeviceMessage.SetResetLoadsAttributeName";
    public static final String CurrentValueAttributeName = "PeakShaverConfigurationDeviceMessage.CurrentValueAttributeName";
    public static final String NewValueAttributeName = "PeakShaverConfigurationDeviceMessage.NewValueAttributeName";

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
    public static final String month = "month";
    public static final String year = "year";
    public static final String dayOfMonth = "dayOfMonth";
    public static final String day = "day";
    public static final String dayOfWeek = "dayOfWeek";
    public static final String hour = "hour";
    public static final String minute = "minute";
    public static final String second = "second";
    public static final String id = "id";
    public static final String tariff = "tariff";

    public static final String loadIdAttributeName = "PeakShaverConfigurationDeviceMessage.loadIdAttributeName";
    public static final String MaxOffAttributeName = "PeakShaverConfigurationDeviceMessage.MaxOffAttributeName";
    public static final String DelayAttributeName = "PeakShaverConfigurationDeviceMessage.DelayAttributeName";
    public static final String ManualAttributeName = "PeakShaverConfigurationDeviceMessage.ManualAttributeName";
    public static final String StatusAttributeName = "PeakShaverConfigurationDeviceMessage.StatusAttributeName";
    public static final String IPAddressAttributeName = "PeakShaverConfigurationDeviceMessage.IPAddressAttributeName";
    public static final String ChnNbrAttributeName = "PeakShaverConfigurationDeviceMessage.ChnNbrAttributeName";

    public static final String SetInputChannelAttributeName = "EventsConfigurationDeviceMessage.SetInputChannelAttributeName";
    public static final String SetConditionAttributeName = "EventsConfigurationDeviceMessage.SetConditionAttributeName";
    public static final String SetConditionValueAttributeName = "EventsConfigurationDeviceMessage.SetConditionValueAttributeName";
    public static final String SetTimeTrueAttributeName = "EventsConfigurationDeviceMessage.SetTimeTrueAttributeName";
    public static final String SetTimeFalseAttributeName = "EventsConfigurationDeviceMessage.SetTimeFalseAttributeName";
    public static final String SetOutputChannelAttributeName = "EventsConfigurationDeviceMessage.SetOutputChannelAttributeName";
    public static final String SetAlarmAttributeName = "EventsConfigurationDeviceMessage.SetAlarmAttributeName";
    public static final String SetTagAttributeName = "EventsConfigurationDeviceMessage.SetTagAttributeName";
    public static final String SetInverseAttributeName = "EventsConfigurationDeviceMessage.SetInverseAttributeName";
    public static final String SetImmediateAttributeName = "EventsConfigurationDeviceMessage.SetImmediateAttributeName";

    public static final String SetOpusOSNbrAttributeName = "OpusConfigurationDeviceMessage.SetOpusOSNbrAttributeName";
    public static final String SetOpusPasswordAttributeName = "OpusConfigurationDeviceMessage.SetOpusPasswordAttributeName";
    public static final String SetOpusTimeoutAttributeName = "OpusConfigurationDeviceMessage.SetOpusTimeoutAttributeName";
    public static final String SetOpusConfigAttributeName = "OpusConfigurationDeviceMessage.SetOpusConfigAttributeName";

    public static final String SetMmEveryAttributeName = "ModbusConfigurationDeviceMessage.SetMmEveryAttributeName";
    public static final String SetMmTimeoutAttributeName = "ModbusConfigurationDeviceMessage.SetMmTimeoutAttributeName";
    public static final String SetMmInstantAttributeName = "ModbusConfigurationDeviceMessage.SetMmInstantAttributeName";
    public static final String SetMmOverflowAttributeName = "ModbusConfigurationDeviceMessage.SetMmOverflowAttributeName";
    public static final String SetMmConfigAttributeName = "ModbusConfigurationDeviceMessage.SetMmConfigAttributeName";

    public static final String SetMBusEveryAttributeName = "MBusConfigurationDeviceMessage.SetMBusEveryAttributeName";
    public static final String SetMBusInterFrameTimeAttributeName = "MBusConfigurationDeviceMessage.SetMBusInterFrameTimeAttributeName";
    public static final String SetMBusConfigAttributeName = "MBusConfigurationDeviceMessage.SetMBusConfigAttributeName";

    public static final String AnalogOutValue = "DeviceActionMessage.AnalogOutValue";
    public static final String OutputOn = "DeviceActionMessage.OutputOn";
    public static final String OutputOff = "DeviceActionMessage.OutputOff";
    public static final String OutputToggle = "DeviceActionMessage.OutputToggle";
    public static final String OutputPulse = "DeviceActionMessage.OutputPulse";

    public static final String FTIONReboot = "DeviceActionMessage.FTIONReboot";
    public static final String FTIONInitialize = "DeviceActionMessage.FTIONInitialize";
    public static final String FTIONMailLog = "DeviceActionMessage.FTIONMailLog";
    public static final String FTIONSaveConfig = "DeviceActionMessage.FTIONSaveConfig";
    public static final String FTIONUpgrade = "DeviceActionMessage.FTIONUpgrade";
    public static final String FTIONClearMem = "DeviceActionMessage.FTIONClearMem";
    public static final String FTIONMailConfig = "DeviceActionMessage.FTIONMailConfig";
    public static final String FTIONModemReset = "DeviceActionMessage.FTIONModemReset";
    public static final String AdminOld = "DeviceActionMessage.AdminOld";
    public static final String AdminNew = "DeviceActionMessage.AdminNew";


}