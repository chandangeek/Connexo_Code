package com.energyict.protocolimplv2.messages;

/**
 * Copyrights EnergyICT
 * Date: 19/03/13
 * Time: 8:45
 */
public class DeviceMessageConstants {

    public static final String contactorActivationDateAttributeName = "ContactorDeviceMessage.activationdate";
    public static final String digitalOutputAttributeName = "ContactorDeviceMessage.digitalOutput";
    public static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    public static final String relayNumberAttributeName = "ContactorDeviceMessage.relayNumber";
    public static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    public static final String meterTimeAttributeName = "ClockDeviceMessage.meterTime";
    public static final String dstStartAlgorithmAttributeName = "ClockDeviceMessage.dstStartAlgorithm";
    public static final String dstEndAlgorithmAttributeName = "ClockDeviceMessage.dstEndAlgorithm";
    public static final String firmwareUpdateVersionNumberAttributeName = "FirmwareDeviceMessage.upgrade.version";
    public static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";
    public static final String resumeFirmwareUpdateAttributeName = "FirmwareDeviceMessage.upgrade.resume";
    public static final String plcTypeFirmwareUpdateAttributeName = "FirmwareDeviceMessage.upgrade.plc";
    public static final String firmwareUpdateURLAttributeName = "FirmwareDeviceMessage.upgrade.url";
    public static final String activityCalendarTypeAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.type";
    public static final String activityCalendarNameAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.name";
    public static final String activityCalendarCodeTableAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.codetable";
    public static final String contractsXmlUserFileAttributeName = "ActivityCalendarDeviceMessage.contractsXmlUserFile";
    public static final String specialDaysCodeTableAttributeName = "ActivityCalendarDeviceMessage.specialdays.codetable";
    public static final String activityCalendarActivationDateAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.activationdate";
    public static final String encryptionLevelAttributeName = "SecurityMessage.dlmsencryption.encryptionlevel";
    public static final String authenticationLevelAttributeName = "SecurityMessage.dlmsauthentication.authenticationlevel";
    public static final String newEncryptionKeyAttributeName = "SecurityMessage.new.encryptionkey";
    public static final String newAuthenticationKeyAttributeName = "SecurityMessage.new.authenticationkey";
    public static final String pskAttributeName = "SecurityMessage.psk";
    public static final String newPasswordAttributeName = "SecurityMessage.new.password";
    public static final String newHexPasswordAttributeName = "SecurityMessage.newHexPassword";
    public static final String keyTActivationStatusAttributeName = "SecurityMessage.keyT.activationstatusT";
    public static final String SecurityTimeDurationAttributeName = "SecurityMessage.TimeDuration";
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
    public static final String newReadingClientPasswordAttributeName = "SecurityMessage.newReadingClientPassword";
    public static final String newManagementClientPasswordAttributeName = "SecurityMessage.newManagementClientPassword";
    public static final String newFirmwareClientPasswordAttributeName = "SecurityMessage.newFirmwareClientPassword";

    public static final String eventLogResetSealBreakTimeAttributeName = "SecurityMessage.eventlogresetseal.breaktime";
    public static final String restoreFactorySettingsSealBreakTimeAttributeName = "SecurityMessage.restorefactorysettingsseal.breaktime";
    public static final String restoreDefaultSettingsSealBreakTimeAttributeName = "SecurityMessage.restoredefaultsettingsseal.breaktime";
    public static final String statusChangeSealBreakTimeAttributeName = "SecurityMessage.statuschangeseal.breaktime";
    public static final String remoteConversionParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteconversionparametersconfigseal.breaktime";
    public static final String remoteAnalysisParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteanalysisparametersconfigseal.breaktime";
    public static final String downloadProgramSealBreakTimeAttributeName = "SecurityMessage.downloadprogramseal.breaktime";
    public static final String restoreDefaultPasswordSealBreakTimeAttributeName = "SecurityMessage.restoredefaultpasswordseal.breaktime";
    public static final String randomBytesAttributeName = "SecurityMessage.Random32Bytes";
    public static final String deviceGroupAttributeName = "SecurityMessage.DeviceGroup";

    public static final String usernameAttributeName = "username";   // commonly used translation key
    public static final String passwordAttributeName = "password";   // commonly used translation key
    public static final String apnAttributeName = "NetworkConnectivityMessage.apn";
    public static final String whiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.whitelist.phonenumbers";
    public static final String managedWhiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.managed.whitelist.phonenumbers";
    public static final String smsCenterPhoneNumberAttributeName = "NetworkConnectivityMessage.smscenter.phonenumber";
    public static final String devicePhoneNumberAttributeName = "NetworkConnectivityMessage.device.phonenumber";
    public static final String ipAddressAttributeName = "NetworkConnectivityMessage.ipaddress";
    public static final String portNumberAttributeName = "NetworkConnectivityMessage.portnumber";
    public static final String wakeupPeriodAttributeName = "NetworkConnectivityMessage.wakeup.period";
    public static final String inactivityTimeoutAttributeName = "NetworkConnectivityMessage.inactivity.timeout";
    public static final String p1InformationAttributeName = "DisplayDeviceMessage.consumer.p1";
    public static final String DisplayMessageAttributeName = "DisplayDeviceMessage.displayMessage";
    public static final String DisplayMessageTimeDurationAttributeName = "DisplayMessage.TimeDuration";
    public static final String DisplayMessageActivationDate = "DisplayMessage.ActivationDate";

    public static final String DemandCloseToContractPowerThresholdAttributeName = "LoadBalanceDeviceMessage.DemandCloseToContractPowerThreshold";
    public static final String readFrequencyInMinutesAttributeName = "LoadBalanceDeviceMessage.parameters.readFrequencyInMinutes";
    public static final String monitoredValueAttributeName = "LoadBalanceDeviceMessage.parameters.monitoredValue";
    public static final String normalThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.normalthreshold";
    public static final String controlThreshold1dAttributeName = "LoadBalanceDeviceMessage.controlThreshold1";
    public static final String controlThreshold2dAttributeName = "LoadBalanceDeviceMessage.controlThreshold2";
    public static final String controlThreshold3dAttributeName = "LoadBalanceDeviceMessage.controlThreshold3";
    public static final String controlThreshold4dAttributeName = "LoadBalanceDeviceMessage.controlThreshold4";
    public static final String controlThreshold5dAttributeName = "LoadBalanceDeviceMessage.controlThreshold5";
    public static final String controlThreshold6dAttributeName = "LoadBalanceDeviceMessage.controlThreshold6";
    public static final String activationDatedAttributeName = "LoadBalanceDeviceMessage.activationDate";
    public static final String emergencyThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencythreshold";
    public static final String overThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.overthresholdduration";
    public static final String underThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.underthresholdduration";
    public static final String emergencyProfileIdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileid";
    public static final String emergencyProfileActivationDateAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileActivationDate";
    public static final String emergencyProfileDurationAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileDuration";
    public static final String emergencyProfileGroupIdListAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileGroupIdList";
    public static final String actionWhenUnderThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.actionWhenUnderThreshold";
    public static final String invertDigitalOutput1AttributeName = "LoadBalanceDeviceMessage.invertDigitalOutput1";
    public static final String invertDigitalOutput2AttributeName = "LoadBalanceDeviceMessage.invertDigitalOutput2";
    public static final String activateNowAttributeName = "LoadBalanceDeviceMessage.activateNow";
    public static final String loadLimitGroupIDAttributeName = "LoadBalanceDeviceMessage.groupID";
    public static final String loadLimitStartDateAttributeName = "LoadBalanceDeviceMessage.StartDate";
    public static final String loadLimitEndDateAttributeName = "LoadBalanceDeviceMessage.EndDate";
    public static final String powerLimitThresholdAttributeName = "LoadBalanceDeviceMessage.PowerLimitThreshold";
    public static final String contractualPowerLimitAttributeName = "LoadBalanceDeviceMessage.ContractualPowerLimit";

    public static final String phaseAttributeName = "LoadBalanceDeviceMessage.phase";
    public static final String thresholdInAmpereAttributeName = "LoadBalanceDeviceMessage.thresholdInAmpere";
    public static final String capturePeriodAttributeName = "LoadProfileConfigurationMessage.capturePeriod";
    public static final String consumerProducerModeAttributeName = "LoadProfileConfigurationMessage.consumerProducerMode";

    public static final String xmlConfigAttributeName = "AdvancedTestMessage.xmlconfig";
    public static final String UserFileConfigAttributeName = "AdvancedTestMessage.ConfigUserfile";
    public static final String loadProfileAttributeName = "loadProfile";
    public static final String fromDateAttributeName = "from";
    public static final String toDateAttributeName = "to";
    public static final String powerQualityThresholdAttributeName = "ConfigurationChangeDeviceMessage.powerQualityThreshold";
    public static final String WriteExchangeStatus = "WriteWavecardParameters.writeExchangeStatus";
    public static final String WriteRadioAcknowledge = "WriteWavecardParameters.writeRadioAcknowledge";
    public static final String WriteRadioUserTimeout = "WriteWavecardParameters.writeRadioUserTimeout";

    public static final String SetDescriptionAttributeName = "ConfigurationChangeDeviceMessage.SetDescription";
    public static final String SetIntervalInSecondsAttributeName = "ConfigurationChangeDeviceMessage.SetIntervalInSeconds";
    public static final String SetUpgradeUrlAttributeName = "ConfigurationChangeDeviceMessage.SetUpgradeUrl";
    public static final String SetUpgradeOptionsAttributeName = "ConfigurationChangeDeviceMessage.SetUpgradeOptions";
    public static final String SetDebounceTresholdAttributeName = "ConfigurationChangeDeviceMessage.SetDebounceTreshold";
    public static final String SetTariffMomentAttributeName = "ConfigurationChangeDeviceMessage.SetTariffMoment";
    public static final String SetCommOffsetAttributeName = "ConfigurationChangeDeviceMessage.SetCommOffset";
    public static final String SetAggIntvAttributeName = "ConfigurationChangeDeviceMessage.SetAggIntv";
    public static final String SetPulseTimeTrueAttributeName = "ConfigurationChangeDeviceMessage.SetPulseTimeTrue";

    public static final String SetProxyServerAttributeName = "NetworkConnectivityMessage.SetProxyServer";
    public static final String SetProxyUsernameAttributeName = "NetworkConnectivityMessage.SetProxyUsername";
    public static final String SetProxyPasswordAttributeName = "NetworkConnectivityMessage.SetProxyPassword";
    public static final String SetDHCPAttributeName = "NetworkConnectivityMessage.SetDHCP";
    public static final String SetDHCPTimeoutAttributeName = "NetworkConnectivityMessage.SetDHCPTimeout";
    public static final String SetIPAddressAttributeName = "NetworkConnectivityMessage.SetIPAddress";
    public static final String SetSubnetMaskAttributeName = "NetworkConnectivityMessage.SetSubnetMask";
    public static final String SetGatewayAttributeName = "NetworkConnectivityMessage.SetGateway";
    public static final String SetNameServerAttributeName = "NetworkConnectivityMessage.SetNameServer";
    public static final String SetHttpPortAttributeName = "NetworkConnectivityMessage.SetHttpPort";
    public static final String NetworkConnectivityIPAddressAttributeName = "NetworkConnectivityMessage.IPAddress";
    public static final String NetworkConnectivityIntervalAttributeName = "NetworkConnectivityMessage.Interval";

    public static final String SetDSTAttributeName = "ClockDeviceMessage.SetDST";
    public static final String SetTimezoneAttributeName = "ClockDeviceMessage.SetTimezone";
    public static final String TimeZoneOffsetInHoursAttributeName = "ClockDeviceMessage.GMTOffsetInHours";
    public static final String SetTimeAdjustmentAttributeName = "ClockDeviceMessage.SetTimeAdjustment";
    public static final String SetNTPServerAttributeName = "ClockDeviceMessage.SetNTPServer";
    public static final String SetRefreshClockEveryAttributeName = "ClockDeviceMessage.SetRefreshClockEvery";
    public static final String SetNTPOptionsAttributeName = "ClockDeviceMessage.SetNTPOptions";

    public static final String SetEIWebPasswordAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebPassword";
    public static final String SetEIWebPageAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebPage";
    public static final String SetEIWebFallbackPageAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebFallbackPage";
    public static final String SetEIWebSendEveryAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebSendEvery";
    public static final String SetEIWebCurrentIntervalAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebCurrentInterval";
    public static final String SetEIWebDatabaseIDAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebDatabaseID";
    public static final String SetEIWebOptionsAttributeName = "EIWebConfigurationDeviceMessage.SetEIWebOptions";


    public static final String SetPOPUsernameAttributeName = "MailConfigurationDeviceMessage.SetPOPUsername";
    public static final String SetPOPPasswordAttributeName = "MailConfigurationDeviceMessage.SetPOPPassword";
    public static final String SetPOPHostAttributeName = "MailConfigurationDeviceMessage.SetPOPHost";
    public static final String SetPOPReadMailEveryAttributeName = "MailConfigurationDeviceMessage.SetPOPReadMailEvery";
    public static final String SetPOP3OptionsAttributeName = "MailConfigurationDeviceMessage.SetPOP3Options";
    public static final String SetSMTPFromAttributeName = "MailConfigurationDeviceMessage.SetSMTPFrom";
    public static final String SetSMTPToAttributeName = "MailConfigurationDeviceMessage.SetSMTPTo";
    public static final String SetSMTPConfigurationToAttributeName = "MailConfigurationDeviceMessage.SetSMTPConfigurationTo";
    public static final String SetSMTPServerAttributeName = "MailConfigurationDeviceMessage.SetSMTPServer";
    public static final String SetSMTPDomainAttributeName = "MailConfigurationDeviceMessage.SetSMTPDomain";
    public static final String SetSMTPSendMailEveryAttributeName = "MailConfigurationDeviceMessage.SetSMTPSendMailEvery";
    public static final String SetSMTPCurrentIntervalAttributeName = "MailConfigurationDeviceMessage.SetSMTPCurrentInterval";
    public static final String SetSMTPDatabaseIDAttributeName = "MailConfigurationDeviceMessage.SetSMTPDatabaseID";
    public static final String SetSMTPOptionsAttributeName = "MailConfigurationDeviceMessage.SetSMTPOptions";

    public static final String SetSmsDataNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsDataNbr";
    public static final String SetSmsAlarmNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsAlarmNbr";
    public static final String SetSmsEveryAttributeName = "SMSConfigurationDeviceMessage.SetSmsEvery";
    public static final String SetSmsNbrAttributeName = "SMSConfigurationDeviceMessage.SetSmsNbr";
    public static final String SetSmsCorrectionAttributeName = "SMSConfigurationDeviceMessage.SetSmsCorrection";
    public static final String SetSmsConfigAttributeName = "SMSConfigurationDeviceMessage.SetSmsConfig";
    public static final String SetDLMSDeviceIDAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSDeviceID";
    public static final String SetDLMSMeterIDAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSMeterID";
    public static final String SetDLMSPasswordAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSPassword";
    public static final String SetDLMSIdleTimeAttributeName = "DLMSConfigurationDeviceMessage.SetDLMSIdleTime";

    public static final String SetDukePowerIDAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerID";
    public static final String SetDukePowerPasswordAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerPassword";
    public static final String SetDukePowerIdleTimeAttributeName = "ConfigurationChangeDeviceMessage.SetDukePowerIdleTime";

    public static final String MeterScheme = "ConfigurationChangeDeviceMessage.MeterScheme";
    public static final String SwitchPointClockSettings = "ConfigurationChangeDeviceMessage.SwitchPointClockSettings";
    public static final String SwitchPointClockUpdateSettings = "ConfigurationChangeDeviceMessage.SwitchPointClockUpdateSettings";

    public static final String ConfigurationChangeDate = "ConfigurationChangeDeviceMessage.Date";

    public static final String CalorificValue = "ConfigurationChangeDeviceMessage.CalorificValue";
    public static final String ConversionFactor = "ConfigurationChangeDeviceMessage.ConversionFactor";
    public static final String ChangeOfSupplierName = "ConfigurationChangeDeviceMessage.ChangeOfSupplierName";
    public static final String ChangeOfSupplierID = "ConfigurationChangeDeviceMessage.ChangeOfSupplierID";
    public static final String ConfigurationChangeActivationDate = "ConfigurationChangeDeviceMessage.ActivationDate";
    public static final String AlarmFilterAttributeName = "ConfigurationChangeDeviceMessage.AlarmFilter";
    public static final String DefaultResetWindowAttributeName = "ConfigurationChangeDeviceMessage.DefaultResetWindow";

    public static final String SetDialCommandAttributeName = "ModemConfigurationDeviceMessage.SetDialCommand";
    public static final String SetModemInit1AttributeName = "ModemConfigurationDeviceMessage.SetModemInit1";
    public static final String SetModemInit2AttributeName = "ModemConfigurationDeviceMessage.SetModemInit2";
    public static final String SetModemInit3AttributeName = "ModemConfigurationDeviceMessage.SetModemInit3";
    public static final String SetPPPBaudRateAttributeName = "ModemConfigurationDeviceMessage.SetPPPBaudRate";
    public static final String SetModemtypeAttributeName = "ModemConfigurationDeviceMessage.SetModemtype";
    public static final String SetResetCycleAttributeName = "ModemConfigurationDeviceMessage.SetResetCycle";

    public static final String SetISP1PhoneAttributeName = "PPPConfigurationDeviceMessage.SetISP1Phone";
    public static final String SetISP1UsernameAttributeName = "PPPConfigurationDeviceMessage.SetISP1Username";
    public static final String SetISP1PasswordAttributeName = "PPPConfigurationDeviceMessage.SetISP1Password";
    public static final String SetISP1TriesAttributeName = "PPPConfigurationDeviceMessage.SetISP1Tries";
    public static final String SetISP2PhoneAttributeName = "PPPConfigurationDeviceMessage.SetISP2Phone";
    public static final String SetISP2UsernameAttributeName = "PPPConfigurationDeviceMessage.SetISP2Username";
    public static final String SetISP2PasswordAttributeName = "PPPConfigurationDeviceMessage.SetISP2Password";
    public static final String SetISP2TriesAttributeName = "PPPConfigurationDeviceMessage.SetISP2Tries";
    public static final String SetPPPIdleTimeoutAttributeName = "PPPConfigurationDeviceMessage.SetPPPIdleTimeout";
    public static final String SetPPPRetryIntervalAttributeName = "PPPConfigurationDeviceMessage.SetPPPRetryInterval";
    public static final String SetPPPOptionsAttributeName = "PPPConfigurationDeviceMessage.SetPPPOptions";

    public static final String SetFunctionAttributeName = "ChannelConfigurationDeviceMessage.SetFunction";
    public static final String SetParametersAttributeName = "ChannelConfigurationDeviceMessage.SetParameters";
    public static final String SetNameAttributeName = "ChannelConfigurationDeviceMessage.SetName";
    public static final String SetUnitAttributeName = "ChannelConfigurationDeviceMessage.SetUnit";
    public static final String ChannelConfigurationChnNbrAttributeName = "ChannelConfigurationDeviceMessage.ChannelNumber";
    public static final String DivisorAttributeName = "ChannelConfigurationDeviceMessage.Divisor";

    public static final String SetSumMaskAttributeName = "TotalizersConfigurationDeviceMessage.SetSumMask";
    public static final String SetSubstractMaskAttributeName = "TotalizersConfigurationDeviceMessage.SetSubstractMask";

    public static final String SetActiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.SetActiveChannel";
    public static final String SetReactiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.SetReactiveChannel";
    public static final String SetTimeBaseAttributeName = "PeakShaverConfigurationDeviceMessage.SetTimeBase";
    public static final String SetPOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetPOut";
    public static final String SetPInAttributeName = "PeakShaverConfigurationDeviceMessage.SetPIn";
    public static final String SetDeadTimeAttributeName = "PeakShaverConfigurationDeviceMessage.SetDeadTime";
    public static final String SetAutomaticAttributeName = "PeakShaverConfigurationDeviceMessage.SetAutomatic";
    public static final String SetCyclicAttributeName = "PeakShaverConfigurationDeviceMessage.SetCyclic";
    public static final String SetInvertAttributeName = "PeakShaverConfigurationDeviceMessage.SetInvert";
    public static final String SetAdaptSetpointAttributeName = "PeakShaverConfigurationDeviceMessage.SetAdaptSetpoint";
    public static final String SetInstantAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetInstantAnalogOut";
    public static final String SetPredictedAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetPredictedAnalogOut";
    public static final String SetpointAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetpointAnalogOut";
    public static final String SetDifferenceAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.SetDifferenceAnalogOut";
    public static final String SetTariffAttributeName = "PeakShaverConfigurationDeviceMessage.SetTariff";
    public static final String SetResetLoadsAttributeName = "PeakShaverConfigurationDeviceMessage.SetResetLoads";
    public static final String CurrentValueAttributeName = "PeakShaverConfigurationDeviceMessage.CurrentValue";
    public static final String NewValueAttributeName = "PeakShaverConfigurationDeviceMessage.NewValue";

    public static final String enableDSTAttributeName = "ConfigurationChangeDeviceMessage.EnableDST";
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

    public static final String loadIdAttributeName = "PeakShaverConfigurationDeviceMessage.loadId";
    public static final String MaxOffAttributeName = "PeakShaverConfigurationDeviceMessage.MaxOff";
    public static final String DelayAttributeName = "PeakShaverConfigurationDeviceMessage.Delay";
    public static final String ManualAttributeName = "PeakShaverConfigurationDeviceMessage.Manual";
    public static final String StatusAttributeName = "PeakShaverConfigurationDeviceMessage.Status";
    public static final String PeakShaverIPAddressAttributeName = "PeakShaverConfigurationDeviceMessage.IPAddress";
    public static final String PeakShaveChnNbrAttributeName = "PeakShaverConfigurationDeviceMessage.ChannelNumber";

    public static final String SetInputChannelAttributeName = "EventsConfigurationDeviceMessage.SetInputChannel";
    public static final String SetConditionAttributeName = "EventsConfigurationDeviceMessage.SetCondition";
    public static final String SetConditionValueAttributeName = "EventsConfigurationDeviceMessage.SetConditionValue";
    public static final String SetTimeTrueAttributeName = "EventsConfigurationDeviceMessage.SetTimeTrue";
    public static final String SetTimeFalseAttributeName = "EventsConfigurationDeviceMessage.SetTimeFalse";
    public static final String SetOutputChannelAttributeName = "EventsConfigurationDeviceMessage.SetOutputChannel";
    public static final String SetAlarmAttributeName = "EventsConfigurationDeviceMessage.SetAlarm";
    public static final String SetTagAttributeName = "EventsConfigurationDeviceMessage.SetTag";
    public static final String SetInverseAttributeName = "EventsConfigurationDeviceMessage.SetInverse";
    public static final String SetImmediateAttributeName = "EventsConfigurationDeviceMessage.SetImmediate";

    public static final String prepaidCreditAttributeName = "PrepaidConfigurationDeviceMessage.prepaidCredit";

    public static final String SetOpusOSNbrAttributeName = "OpusConfigurationDeviceMessage.SetOpusOSNbr";
    public static final String SetOpusPasswordAttributeName = "OpusConfigurationDeviceMessage.SetOpusPassword";
    public static final String SetOpusTimeoutAttributeName = "OpusConfigurationDeviceMessage.SetOpusTimeout";
    public static final String SetOpusConfigAttributeName = "OpusConfigurationDeviceMessage.SetOpusConfig";

    public static final String ReferenceVoltageAttributeName = "PowerConfigurationDeviceMessage.ReferenceVoltage";
    public static final String VoltageSagTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.VoltageSagTimeThreshold";
    public static final String VoltageSwellTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.VoltageSwellTimeThreshold";
    public static final String VoltageSagThresholdAttributeName = "PowerConfigurationDeviceMessage.VoltageSagThreshold";
    public static final String VoltageSwellThresholdAttributeName = "PowerConfigurationDeviceMessage.VoltageSwellThreshold";
    public static final String LongPowerFailureTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.LongPowerFailureTimeThreshold";
    public static final String LongPowerFailureThresholdAttributeName = "PowerConfigurationDeviceMessage.LongPowerFailureThreshold";

    public static final String SetMmEveryAttributeName = "ModbusConfigurationDeviceMessage.SetMmEvery";
    public static final String SetMmTimeoutAttributeName = "ModbusConfigurationDeviceMessage.SetMmTimeout";
    public static final String SetMmInstantAttributeName = "ModbusConfigurationDeviceMessage.SetMmInstant";
    public static final String SetMmOverflowAttributeName = "ModbusConfigurationDeviceMessage.SetMmOverflow";
    public static final String SetMmConfigAttributeName = "ModbusConfigurationDeviceMessage.SetMmConfig";
    public static final String RadixFormatAttributeName = "ModbusConfigurationDeviceMessage.RadixFormat";
    public static final String RegisterAddressAttributeName = "ModbusConfigurationDeviceMessage.RegisterAddress";
    public static final String RegisterValueAttributeName = "ModbusConfigurationDeviceMessage.RegisterValue(s)";


    public static final String SetMBusEveryAttributeName = "MBusConfigurationDeviceMessage.SetMBusEvery";
    public static final String SetMBusInterFrameTimeAttributeName = "MBusConfigurationDeviceMessage.SetMBusInterFrameTime";
    public static final String SetMBusConfigAttributeName = "MBusConfigurationDeviceMessage.SetMBusConfig";
    public static final String SetMBusVIFAttributeName = "MBusConfigurationDeviceMessage.MBusVIF";

    public static final String openKeyAttributeName = "MBusSetupDeviceMessage.openKey";
    public static final String transferKeyAttributeName = "MBusSetupDeviceMessage.transferKey";

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

    public static final String IEC1107ClassIdAttributeName = "GeneralDeviceMessage.IEC1107ClassId";
    public static final String OffsetAttributeName = "GeneralDeviceMessage.Offset";
    public static final String RawDataAttributeName = "GeneralDeviceMessage.RawData";

    public static final String MulticastAddress1AttributeName = "PLCConfigurationDeviceMessage.MulticastAddress1";
    public static final String MulticastAddress2AttributeName = "PLCConfigurationDeviceMessage.MulticastAddress2";
    public static final String MulticastAddress3AttributeName = "PLCConfigurationDeviceMessage.MulticastAddress3";

    public static final String ActiveChannelAttributeName = "PLCConfigurationDeviceMessage.ActiveChannel";
    public static final String CHANNEL1_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL1_FS";
    public static final String CHANNEL1_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL1_FM";
    public static final String CHANNEL1_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL1_SNR";
    public static final String CHANNEL1_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL1_CREDITWEIGHT";
    public static final String CHANNEL2_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL2_FS";
    public static final String CHANNEL2_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL2_FM";
    public static final String CHANNEL2_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL2_SNR";
    public static final String CHANNEL2_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL2_CREDITWEIGHT";
    public static final String CHANNEL3_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL3_FS";
    public static final String CHANNEL3_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL3_FM";
    public static final String CHANNEL3_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL3_SNR";
    public static final String CHANNEL3_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL3_CREDITWEIGHT";
    public static final String CHANNEL4_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL4_FS";
    public static final String CHANNEL4_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL4_FM";
    public static final String CHANNEL4_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL4_SNR";
    public static final String CHANNEL4_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL4_CREDITWEIGHT";
    public static final String CHANNEL5_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL5_FS";
    public static final String CHANNEL5_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL5_FM";
    public static final String CHANNEL5_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL5_SNR";
    public static final String CHANNEL5_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL5_CREDITWEIGHT";
    public static final String CHANNEL6_FSAttributeName = "PLCConfigurationDeviceMessage.CHANNEL6_FS";
    public static final String CHANNEL6_FMAttributeName = "PLCConfigurationDeviceMessage.CHANNEL6_FM";
    public static final String CHANNEL6_SNRAttributeName = "PLCConfigurationDeviceMessage.CHANNEL6_SNR";
    public static final String CHANNEL6_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.CHANNEL6_CREDITWEIGHT";

    public static final String MAX_RECEIVING_GAINAttributeName = "PLCConfigurationDeviceMessage.MAX_RECEIVING_GAIN";
    public static final String MAX_TRANSMITTING_GAINAttributeName = "PLCConfigurationDeviceMessage.MAX_TRANSMITTING_GAIN";
    public static final String SEARCH_INITIATOR_GAINAttributeName = "PLCConfigurationDeviceMessage.SEARCH_INITIATOR_GAIN";

    public static final String SEARCH_INITIATOR_TIMEOUTAttributeName = "PLCConfigurationDeviceMessage.SEARCH_INITIATOR_TIMEOUT";
    public static final String SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName = "PLCConfigurationDeviceMessage.SYNCHRONIZATION_CONFIRMATION_TIMEOUT";
    public static final String TIME_OUT_NOT_ADDRESSEDAttributeName = "PLCConfigurationDeviceMessage.TIME_OUT_NOT_ADDRESSED";
    public static final String TIME_OUT_FRAME_NOT_OKAttributeName = "PLCConfigurationDeviceMessage.TIME_OUT_FRAME_NOT_OK";

    public static final String MAX_FRAME_LENGTHAttributeName = "PLCConfigurationDeviceMessage.MAX_FRAME_LENGTH";
    public static final String REPEATERAttributeName = "PLCConfigurationDeviceMessage.REPEATER";

    public static final String INITIATOR_ELECTRICAL_PHASEAttributeName = "PLCConfigurationDeviceMessage.INITIATOR_ELECTRICAL_PHASE";

    public static final String ZigBeeConfigurationSASPanIdAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.PanId";
    public static final String ZigBeeConfigurationForceRemovalAttributeName = "ZigBeeConfigurationDeviceMessage.ForceRemoval";
    public static final String ZigBeeConfigurationZigBeeLinkKeyAttributeName = "ZigBeeConfigurationDeviceMessage.ZigBeeLinkKey";
    public static final String ZigBeeConfigurationActivationDateAttributeName = "ZigBeeConfigurationDeviceMessage.ActivationDate";
    public static final String ZigBeeConfigurationZigBeeAddressAttributeName = "ZigBeeConfigurationDeviceMessage.ZigBeeIEEEAddress";
    public static final String ZigBeeConfigurationMirrorAddressAttributeName = "ZigBeeConfigurationDeviceMessage.MirrorIEEEAddress";
    public static final String ZigBeeConfigurationFirmwareUpdateUserFileAttributeName = "ZigBeeConfigurationDeviceMessage.UserFile";
    public static final String ZigBeeConfigurationSASInsecureJoinAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.InsecureJoin";
    public static final String ZigBeeConfigurationSASExtendedPanIdAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.ExtendedPanId";
    public static final String ZigBeeConfigurationSASPanChannelMaskAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.PanChannelMask";
    public static final String ZigBeeConfigurationHANRestoreUserFileAttributeName = "ZigBeeConfigurationDeviceMessage.HANRestoreUserFile";

    public static final String StandingChargeAttributeName = "PricingInformation.StandingCharge";
    public static final String PricingInformationUserFileAttributeName = "PricingInformation.UserFile";
    public static final String PricingInformationActivationDateAttributeName = "PricingInformation.ActivationDate";

    public static final String alarmFilterAttributeName = "AlarmConfigurationMessage.alarmFilter";
    public static final String configUserFileAttributeName = "GeneralDeviceMessage.configUserFile";

    public static final String activeScanDurationAttributeName = "PLCConfigurationDeviceMessage.activeScanDuration";
    public static final String broadCastLogTableEntryTTLAttributeName = "PLCConfigurationDeviceMessage.broadCastLogTableEntryTTL";
    public static final String discoveryAttemptsSpeedAttributeName = "PLCConfigurationDeviceMessage.discoveryAttemptsSpeed";
    public static final String maxAgeTimeAttributeName = "PLCConfigurationDeviceMessage.maxAgeTime";
    public static final String maxNumberOfHopsAttributeName = "PLCConfigurationDeviceMessage.maxNumberOfHops";
    public static final String maxPANConflictsCountAttributeName = "PLCConfigurationDeviceMessage.maxPANConflictsCount";
    public static final String panConflictWaitTimeAttributeName = "PLCConfigurationDeviceMessage.panConflictWaitTime";
    public static final String toneMaskAttributeName = "PLCConfigurationDeviceMessage.toneMask";
    public static final String weakLQIValueAttributeName = "PLCConfigurationDeviceMessage.weakLQIValue";
    public static final String plcG3TimeoutAttributeName = "PLCConfigurationDeviceMessage.plcG3Timeout";
    public static final String G3PanIdAttributename = "PLCConfigurationDeviceMessage.G3PanId";
    public static final String MaxOrphanTimerAttributeName = "PLCConfigurationDeviceMessage.MaxOrphanTimer";
}