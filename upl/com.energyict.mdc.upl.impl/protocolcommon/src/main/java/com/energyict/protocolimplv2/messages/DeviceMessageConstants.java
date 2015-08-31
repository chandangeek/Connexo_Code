package com.energyict.protocolimplv2.messages;

/**
 * Lists all translation keys that are used for the message attribute names.
 * Every key should be unique.
 * Also, do not name them CategoryName.MessageSpecName, this is already used as translation key for the message name.
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/03/13
 * Time: 8:45
 */
public class DeviceMessageConstants {

    public static final String contactorActivationDateAttributeName = "ContactorDeviceMessage.activationdate";
    public static final String digitalOutputAttributeName = "ContactorDeviceMessage.digitaloutput";
    public static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    public static final String relayNumberAttributeName = "ContactorDeviceMessage.relaynumber";
    public static final String relayOperatingModeAttributeName = "ContactorDeviceMessage.relayoperatingmode";

    public static final String broadcastLogicalDeviceIdAttributeName = "FirmwareDeviceMessage.broadcast.logicaldeviceid";
    public static final String broadcastClientMacAddressAttributeName = "FirmwareDeviceMessage.broadcast.clientmacaddress";
    public static final String broadcastGroupIdAttributeName = "FirmwareDeviceMessage.broadcast.groupid";
    public static final String broadcastInitialTimeBetweenBlocksAttributeName = "FirmwareDeviceMessage.broadcast.initialtimebetweenblocks";
    public static final String broadcastNumberOfBlocksInCycleAttributeName = "FirmwareDeviceMessage.broadcast.numberofblocksincycle";
    public static final String broadcastEncryptionKeyAttributeName = "FirmwareDeviceMessage.broadcast.encryptionkey";
    public static final String broadcastAuthenticationKeyAttributeName = "FirmwareDeviceMessage.broadcast.authenticationkey";

    public static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    public static final String meterTimeAttributeName = "ClockDeviceMessage.metertime";
    public static final String dstStartAlgorithmAttributeName = "ClockDeviceMessage.dststartalgorithm";
    public static final String dstEndAlgorithmAttributeName = "ClockDeviceMessage.dstendalgorithm";
    public static final String firmwareUpdateVersionNumberAttributeName = "FirmwareDeviceMessage.upgrade.version";
    public static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";
    public static final String firmwareUpdateImageIdentifierAttributeName = "FirmwareDeviceMessage.image.identifier";
    public static final String resumeFirmwareUpdateAttributeName = "FirmwareDeviceMessage.upgrade.resume";
    public static final String plcTypeFirmwareUpdateAttributeName = "FirmwareDeviceMessage.upgrade.plc";
    public static final String firmwareUpdateURLAttributeName = "FirmwareDeviceMessage.upgrade.url";
    public static final String contractAttributeName = "contract";
    public static final String activityCalendarTypeAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.type";
    public static final String activityCalendarNameAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.name";
    public static final String activityCalendarCodeTableAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.codetable";
    public static final String contractsXmlUserFileAttributeName = "ActivityCalendarDeviceMessage.contractsxmluserfile";
    public static final String specialDaysCodeTableAttributeName = "ActivityCalendarDeviceMessage.specialdays.codetable";
    public static final String XmlUserFileAttributeName = "ActivityCalendarDeviceMessage.xml.userfile";
    public static final String activityCalendarActivationDateAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.activationdate";
    public static final String encryptionLevelAttributeName = "SecurityMessage.dlmsencryption.encryptionlevel";
    public static final String authenticationLevelAttributeName = "SecurityMessage.dlmsauthentication.authenticationlevel";
    public static final String newEncryptionKeyAttributeName = "SecurityMessage.new.encryptionkey";
    public static final String newWrappedEncryptionKeyAttributeName = "SecurityMessage.new.wrapped.encryptionkey";
    public static final String newAuthenticationKeyAttributeName = "SecurityMessage.new.authenticationkey";
    public static final String newWrappedAuthenticationKeyAttributeName = "SecurityMessage.new.wrapped.authenticationkey";
    public static final String clientMacAddress = "ClientMacAddress";
    public static final String masterKey = "SecurityMessage.masterkey";
    public static final String pskAttributeName = "SecurityMessage.psk";
    public static final String newPasswordAttributeName = "SecurityMessage.new.password";
    public static final String newHexPasswordAttributeName = "SecurityMessage.newhexpassword";
    public static final String preparedDataAttributeName = "SecurityMessage.prepareddata";
    public static final String signatureAttributeName = "SecurityMessage.signature";
    public static final String verificationKeyAttributeName = "SecurityMessage.verificationkey";
    public static final String keyTActivationStatusAttributeName = "SecurityMessage.keyT.usage";
    public static final String SecurityTimeDurationAttributeName = "SecurityMessage.timeduration";
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
    public static final String newReadingClientPasswordAttributeName = "SecurityMessage.newreadingclientpassword";
    public static final String newManagementClientPasswordAttributeName = "SecurityMessage.newmanagementclientpassword";
    public static final String newFirmwareClientPasswordAttributeName = "SecurityMessage.newfirmwareclientpassword";

    public static final String eventLogResetSealBreakTimeAttributeName = "SecurityMessage.eventlogresetseal.breaktime";
    public static final String restoreFactorySettingsSealBreakTimeAttributeName = "SecurityMessage.restorefactorysettingsseal.breaktime";
    public static final String restoreDefaultSettingsSealBreakTimeAttributeName = "SecurityMessage.restoredefaultsettingsseal.breaktime";
    public static final String statusChangeSealBreakTimeAttributeName = "SecurityMessage.statuschangeseal.breaktime";
    public static final String remoteConversionParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteconversionparametersconfigseal.breaktime";
    public static final String remoteAnalysisParametersConfigSealBreakTimeAttributeName = "SecurityMessage.remoteanalysisparametersconfigseal.breaktime";
    public static final String downloadProgramSealBreakTimeAttributeName = "SecurityMessage.downloadprogramseal.breaktime";
    public static final String restoreDefaultPasswordSealBreakTimeAttributeName = "SecurityMessage.restoredefaultpasswordseal.breaktime";
    public static final String randomBytesAttributeName = "SecurityMessage.random32bytes";
    public static final String deviceGroupAttributeName = "SecurityMessage.devicegroup";

    public static final String dcDeviceIDAttributeName = "DeviceActionMessage.dcDeviceID";
    public static final String dcDeviceID2AttributeName = "DeviceActionMessage.dcDeviceID2";
    public static final String broadcastDevicesGroupAttributeName = "FirmwareDeviceMessage.broadcastDevicesGroup";

    public static final String usernameAttributeName = "username";   // commonly used translation key
    public static final String passwordAttributeName = "password";   // commonly used translation key
    public static final String apnAttributeName = "NetworkConnectivityMessage.apn";
    public static final String whiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.whitelist.phonenumbers";
    public static final String discoverDuration = "NetworkConnectivityMessage.discover.duration";
    public static final String discoverInterval = "NetworkConnectivityMessage.discover.interval";
    public static final String repeaterCallInterval = "NetworkConnectivityMessage.repeater.call.interval";
    public static final String repeaterCallThreshold = "NetworkConnectivityMessage.repeater.call.threshold";
    public static final String repeaterCallTimeslots = "NetworkConnectivityMessage.repeater.call.timeslots";
    public static final String systemRebootThreshold = "ConfigurationChangeDeviceMessage.systemRebootThreshold";
    public static final String enableSSL = "ConfigurationChangeDeviceMessage.enableSSL";
    public static final String deviceName = "ConfigurationChangeDeviceMessage.deviceName";
    public static final String ntpAddress = "ConfigurationChangeDeviceMessage.ntpAddress";
    public static final String enableAutomaticDemandResetAttributeName = "ConfigurationChangeDeviceMessage.enabledemandreset";
    public static final String localMacAddress = "ConfigurationChangeDeviceMessage.localMacAddress";
    public static final String maxCredit = "ConfigurationChangeDeviceMessage.maxCredit";
    public static final String zeroCrossDelay = "ConfigurationChangeDeviceMessage.zeroCrossDelay";
    public static final String synchronisationBit = "ConfigurationChangeDeviceMessage.synchronisationBit";
    public static final String EnableEventNotifications = "EnableEventNotifications";

    public static final String managedWhiteListPhoneNumbersAttributeName = "NetworkConnectivityMessage.managed.whitelist.phonenumbers";
    public static final String smsCenterPhoneNumberAttributeName = "NetworkConnectivityMessage.smscenter.phonenumber";
    public static final String devicePhoneNumberAttributeName = "NetworkConnectivityMessage.device.phonenumber";
    public static final String ipAddressAttributeName = "NetworkConnectivityMessage.ipaddress";
    public static final String portNumberAttributeName = "NetworkConnectivityMessage.portnumber";
    public static final String wakeupPeriodAttributeName = "NetworkConnectivityMessage.wakeup.period";
    public static final String inactivityTimeoutAttributeName = "NetworkConnectivityMessage.inactivity.timeout";
    public static final String sessionTimeoutAttributeName = "NetworkConnectivityMessage.session.timeout";
    public static final String p1InformationAttributeName = "DisplayDeviceMessage.consumer.p1";
    public static final String DisplayMessageAttributeName = "DisplayDeviceMessage.displaymessage";
    public static final String DisplayMessageTimeDurationAttributeName = "DisplayMessage.timeduration";
    public static final String DisplayMessageActivationDate = "DisplayMessage.activationdate";

    public static final String threshold = "Threshold"; //Can be reused
    public static final String beginDatesAttributeName = "BeginDates";
    public static final String endDatesAttributeName = "EndDates";
    public static final String offOffsetsAttributeName = "OffOffsets";
    public static final String onOffsetsAttributeName = "OnOffsets";
    public static final String latitudeAttributeName = "latitude";
    public static final String longitudeAttributeName = "longitude";

    public static final String DemandCloseToContractPowerThresholdAttributeName = "LoadBalanceDeviceMessage.demandclosetocontractpowerthreshold";
    public static final String readFrequencyInMinutesAttributeName = "LoadBalanceDeviceMessage.parameters.readfrequencyinminutes";
    public static final String monitoredValueAttributeName = "LoadBalanceDeviceMessage.parameters.monitoredvalue";
    public static final String normalThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.normalthreshold";
    public static final String controlThreshold1dAttributeName = "LoadBalanceDeviceMessage.controlthreshold1";
    public static final String controlThreshold2dAttributeName = "LoadBalanceDeviceMessage.controlthreshold2";
    public static final String controlThreshold3dAttributeName = "LoadBalanceDeviceMessage.controlthreshold3";
    public static final String controlThreshold4dAttributeName = "LoadBalanceDeviceMessage.controlthreshold4";
    public static final String controlThreshold5dAttributeName = "LoadBalanceDeviceMessage.controlthreshold5";
    public static final String controlThreshold6dAttributeName = "LoadBalanceDeviceMessage.controlthreshold6";
    public static final String activationDatedAttributeName = "LoadBalanceDeviceMessage.activationdate";
    public static final String emergencyThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencythreshold";
    public static final String overThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.overthresholdduration";
    public static final String underThresholdDurationAttributeName = "LoadBalanceDeviceMessage.parameters.underthresholdduration";
    public static final String emergencyProfileIdAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofileid";
    public static final String emergencyProfileActivationDateAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileActivationDate";
    public static final String emergencyProfileDurationAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyProfileDuration";
    public static final String emergencyProfileGroupIdListAttributeName = "LoadBalanceDeviceMessage.parameters.emergencyprofilegroupidlist";
    public static final String actionWhenUnderThresholdAttributeName = "LoadBalanceDeviceMessage.parameters.actionwhenunderthreshold";
    public static final String invertDigitalOutput1AttributeName = "LoadBalanceDeviceMessage.invertdigitaloutput1";
    public static final String invertDigitalOutput2AttributeName = "LoadBalanceDeviceMessage.invertdigitaloutput2";
    public static final String activateNowAttributeName = "LoadBalanceDeviceMessage.activatenow";
    public static final String loadLimitGroupIDAttributeName = "LoadBalanceDeviceMessage.groupid";
    public static final String loadLimitStartDateAttributeName = "LoadBalanceDeviceMessage.startdate";
    public static final String loadLimitEndDateAttributeName = "LoadBalanceDeviceMessage.enddate";
    public static final String powerLimitThresholdAttributeName = "LoadBalanceDeviceMessage.powerlimitthreshold";
    public static final String contractualPowerLimitAttributeName = "LoadBalanceDeviceMessage.contractualpowerlimit";

    public static final String phaseAttributeName = "LoadBalanceDeviceMessage.phase";
    public static final String thresholdInAmpereAttributeName = "LoadBalanceDeviceMessage.thresholdinampere";
    public static final String capturePeriodAttributeName = "LoadProfileConfigurationMessage.captureperiod";
    public static final String consumerProducerModeAttributeName = "LoadProfileConfigurationMessage.consumerproducermode";

    public static final String xmlConfigAttributeName = "AdvancedTestMessage.xmlconfig";
    public static final String UserFileConfigAttributeName = "AdvancedTestMessage.configuserfile";
    public static final String loadProfileAttributeName = "loadProfile";
    public static final String fromDateAttributeName = "from";
    public static final String toDateAttributeName = "to";
    public static final String powerQualityThresholdAttributeName = "ConfigurationChangeDeviceMessage.powerqualitythreshold";
    public static final String WriteExchangeStatus = "WriteWavecardParameters.writeexchangestatus";
    public static final String WriteRadioAcknowledge = "WriteWavecardParameters.writeradioacknowledge";
    public static final String WriteRadioUserTimeout = "WriteWavecardParameters.writeradiousertimeout";

    public static final String SetDescriptionAttributeName = "ConfigurationChangeDeviceMessage.setdescription";
    public static final String SetIntervalInSecondsAttributeName = "ConfigurationChangeDeviceMessage.setintervalinseconds";
    public static final String SetUpgradeUrlAttributeName = "ConfigurationChangeDeviceMessage.setupgradeurl";
    public static final String SetUpgradeOptionsAttributeName = "ConfigurationChangeDeviceMessage.setupgradeoptions";
    public static final String SetDebounceTresholdAttributeName = "ConfigurationChangeDeviceMessage.setdebouncetreshold";
    public static final String SetTariffMomentAttributeName = "ConfigurationChangeDeviceMessage.settariffmoment";
    public static final String SetCommOffsetAttributeName = "ConfigurationChangeDeviceMessage.setcommoffset";
    public static final String SetAggIntvAttributeName = "ConfigurationChangeDeviceMessage.setaggintv";
    public static final String SetPulseTimeTrueAttributeName = "ConfigurationChangeDeviceMessage.setpulsetimetrue";

    public static final String SetProxyServerAttributeName = "NetworkConnectivityMessage.setproxyserver";
    public static final String SetProxyUsernameAttributeName = "NetworkConnectivityMessage.setproxyusername";
    public static final String SetProxyPasswordAttributeName = "NetworkConnectivityMessage.setproxypassword";
    public static final String SetDHCPAttributeName = "NetworkConnectivityMessage.setdhcp";
    public static final String primaryDNSAddressAttributeName = "NetworkConnectivityMessage.primaryDNSAddress";
    public static final String secondaryDNSAddressAttributeName = "NetworkConnectivityMessage.secondaryDNSAddress";
    public static final String gprsModeAttributeName = "NetworkConnectivityMessage.gprsMode";
    public static final String SetDHCPTimeoutAttributeName = "NetworkConnectivityMessage.setdhcptimeout";
    public static final String SetIPAddressAttributeName = "NetworkConnectivityMessage.setipaddress";
    public static final String SetSubnetMaskAttributeName = "NetworkConnectivityMessage.setsubnetmask";
    public static final String SetGatewayAttributeName = "NetworkConnectivityMessage.setgateway";
    public static final String SetNameServerAttributeName = "NetworkConnectivityMessage.setnameserver";
    public static final String SetHttpPortAttributeName = "NetworkConnectivityMessage.sethttpport";
    public static final String NetworkConnectivityIPAddressAttributeName = "NetworkConnectivityMessage.ipaddress";
    public static final String NetworkConnectivityIntervalAttributeName = "NetworkConnectivityMessage.interval";
    public static final String preferGPRSUpstreamCommunication = "NetworkConnectivityMessage.preferGPRSUpstreamCommunication";
    public static final String enableModemWatchdog = "NetworkConnectivityMessage.enableModemWatchdog";
    public static final String modemWatchdogInterval = "NetworkConnectivityMessage.modemWatchdogInterval";
    public static final String modemResetThreshold = "NetworkConnectivityMessage.modemResetThreshold";
    public static final String networkOperator = "NetworkConnectivityMessage.networkOperator";
    public static final String Destination1IPAddressAttributeName = "NetworkConnectivityMessage.destination.1";
    public static final String Destination2IPAddressAttributeName = "NetworkConnectivityMessage.destination.2";

    public static final String enableDSTAttributeName = "ClockDeviceMessage.enabledst";
    public static final String DSTDeviationAttributeName= "ClockDeviceMessage.dst.deviation";
    public static final String SetDSTAttributeName = "ClockDeviceMessage.setdst";
    public static final String StartOfDSTAttributeName = "ClockDeviceMessage.startofdst";
    public static final String EndOfDSTAttributeName = "ClockDeviceMessage.endofdst";
    public static final String SetTimezoneAttributeName = "ClockDeviceMessage.settimezone";
    public static final String TimeZoneOffsetInHoursAttributeName = "ClockDeviceMessage.gmtoffsetinhours";
    public static final String SetTimeAdjustmentAttributeName = "ClockDeviceMessage.settimeadjustment";
    public static final String SetNTPServerAttributeName = "ClockDeviceMessage.setntpserver";
    public static final String SetRefreshClockEveryAttributeName = "ClockDeviceMessage.setrefreshclockevery";
    public static final String SetNTPOptionsAttributeName = "ClockDeviceMessage.setntpoptions";

    public static final String SetEIWebPasswordAttributeName = "EIWebConfigurationDeviceMessage.seteiwebpassword";
    public static final String SetEIWebPageAttributeName = "EIWebConfigurationDeviceMessage.seteiwebpage";
    public static final String SetEIWebFallbackPageAttributeName = "EIWebConfigurationDeviceMessage.seteiwebfallbackpage";
    public static final String SetEIWebSendEveryAttributeName = "EIWebConfigurationDeviceMessage.seteiwebsendevery";
    public static final String SetEIWebCurrentIntervalAttributeName = "EIWebConfigurationDeviceMessage.seteiwebcurrentinterval";
    public static final String SetEIWebDatabaseIDAttributeName = "EIWebConfigurationDeviceMessage.seteiwebdatabaseid";
    public static final String SetEIWebOptionsAttributeName = "EIWebConfigurationDeviceMessage.seteiweboptions";


    public static final String SetPOPUsernameAttributeName = "MailConfigurationDeviceMessage.setpopusername";
    public static final String SetPOPPasswordAttributeName = "MailConfigurationDeviceMessage.setpoppassword";
    public static final String SetPOPHostAttributeName = "MailConfigurationDeviceMessage.setpophost";
    public static final String SetPOPReadMailEveryAttributeName = "MailConfigurationDeviceMessage.setpopreadmailevery";
    public static final String SetPOP3OptionsAttributeName = "MailConfigurationDeviceMessage.setpop3options";
    public static final String SetSMTPFromAttributeName = "MailConfigurationDeviceMessage.setsmtpfrom";
    public static final String SetSMTPToAttributeName = "MailConfigurationDeviceMessage.setsmtpto";
    public static final String SetSMTPConfigurationToAttributeName = "MailConfigurationDeviceMessage.setsmtpconfigurationto";
    public static final String SetSMTPServerAttributeName = "MailConfigurationDeviceMessage.setsmtpserver";
    public static final String SetSMTPDomainAttributeName = "MailConfigurationDeviceMessage.setsmtpdomain";
    public static final String SetSMTPSendMailEveryAttributeName = "MailConfigurationDeviceMessage.setsmtpsendmailevery";
    public static final String SetSMTPCurrentIntervalAttributeName = "MailConfigurationDeviceMessage.setsmtpcurrentinterval";
    public static final String SetSMTPDatabaseIDAttributeName = "MailConfigurationDeviceMessage.setsmtpdatabaseid";
    public static final String SetSMTPOptionsAttributeName = "MailConfigurationDeviceMessage.setsmtpoptions";

    public static final String SetSmsDataNbrAttributeName = "SMSConfigurationDeviceMessage.setsmsdatanbr";
    public static final String SetSmsAlarmNbrAttributeName = "SMSConfigurationDeviceMessage.setsmsalarmnbr";
    public static final String SetSmsEveryAttributeName = "SMSConfigurationDeviceMessage.setsmsevery";
    public static final String SetSmsNbrAttributeName = "SMSConfigurationDeviceMessage.setsmsnbr";
    public static final String SetSmsCorrectionAttributeName = "SMSConfigurationDeviceMessage.setsmscorrection";
    public static final String SetSmsConfigAttributeName = "SMSConfigurationDeviceMessage.setsmsconfig";
    public static final String SetDLMSDeviceIDAttributeName = "DLMSConfigurationDeviceMessage.setdlmsdeviceid";
    public static final String SetDLMSMeterIDAttributeName = "DLMSConfigurationDeviceMessage.setdlmsmeterid";
    public static final String SetDLMSPasswordAttributeName = "DLMSConfigurationDeviceMessage.setdlmspassword";
    public static final String SetDLMSIdleTimeAttributeName = "DLMSConfigurationDeviceMessage.setdlmsidletime";

    public static final String SetDukePowerIDAttributeName = "ConfigurationChangeDeviceMessage.setdukepowerid";
    public static final String SetDukePowerPasswordAttributeName = "ConfigurationChangeDeviceMessage.setdukepowerpassword";
    public static final String SetDukePowerIdleTimeAttributeName = "ConfigurationChangeDeviceMessage.setdukepoweridletime";

    public static final String MeterScheme = "ConfigurationChangeDeviceMessage.meterscheme";
    public static final String SwitchPointClockSettings = "ConfigurationChangeDeviceMessage.switchpointclocksettings";
    public static final String SwitchPointClockUpdateSettings = "ConfigurationChangeDeviceMessage.switchpointclockupdatesettings";

    public static final String ConfigurationChangeDate = "ConfigurationChangeDeviceMessage.date";

    public static final String CalorificValue = "ConfigurationChangeDeviceMessage.calorificvalue";
    public static final String ConversionFactor = "ConfigurationChangeDeviceMessage.conversionfactor";
    public static final String ChangeOfSupplierName = "ConfigurationChangeDeviceMessage.changeofsuppliername";
    public static final String ChangeOfSupplierID = "ConfigurationChangeDeviceMessage.changeofsupplierid";
    public static final String tenantReference = "ConfigurationChangeDeviceMessage.tenantreference";
    public static final String supplierReference = "ConfigurationChangeDeviceMessage.supplierreference";
    public static final String scriptExecuted = "ConfigurationChangeDeviceMessage.scriptexecuted";
    public static final String ConfigurationChangeActivationDate = "ConfigurationChangeDeviceMessage.activationdate";
    public static final String AlarmFilterAttributeName = "ConfigurationChangeDeviceMessage.alarmfilter";
    public static final String DefaultResetWindowAttributeName = "ConfigurationChangeDeviceMessage.defaultresetwindow";
    public static final String AdministrativeStatusAttributeName = "ConfigurationChangeDeviceMessage.administrativestatus";
    public static final String engineerPin = "ConfigurationChangeDeviceMessage.engineerPin";
    public static final String engineerPinTimeout = "ConfigurationChangeDeviceMessage.engineerPinTimeout";

    public static final String SetDialCommandAttributeName = "ModemConfigurationDeviceMessage.setdialcommand";
    public static final String SetModemInit1AttributeName = "ModemConfigurationDeviceMessage.setmodeminit1";
    public static final String SetModemInit2AttributeName = "ModemConfigurationDeviceMessage.setmodeminit2";
    public static final String SetModemInit3AttributeName = "ModemConfigurationDeviceMessage.setmodeminit3";
    public static final String SetPPPBaudRateAttributeName = "ModemConfigurationDeviceMessage.setpppbaudrate";
    public static final String SetModemtypeAttributeName = "ModemConfigurationDeviceMessage.setmodemtype";
    public static final String SetResetCycleAttributeName = "ModemConfigurationDeviceMessage.setresetcycle";

    public static final String SetISP1PhoneAttributeName = "PPPConfigurationDeviceMessage.setisp1phone";
    public static final String SetISP1UsernameAttributeName = "PPPConfigurationDeviceMessage.setisp1username";
    public static final String SetISP1PasswordAttributeName = "PPPConfigurationDeviceMessage.setisp1password";
    public static final String SetISP1TriesAttributeName = "PPPConfigurationDeviceMessage.setisp1tries";
    public static final String SetISP2PhoneAttributeName = "PPPConfigurationDeviceMessage.setisp2phone";
    public static final String SetISP2UsernameAttributeName = "PPPConfigurationDeviceMessage.setisp2username";
    public static final String SetISP2PasswordAttributeName = "PPPConfigurationDeviceMessage.setisp2password";
    public static final String SetISP2TriesAttributeName = "PPPConfigurationDeviceMessage.setisp2tries";
    public static final String SetPPPIdleTimeoutAttributeName = "PPPConfigurationDeviceMessage.setpppidletimeout";
    public static final String SetPPPIdleTime = "PPPConfigurationDeviceMessage.setPPPIdleTime";
    public static final String PPPDaemonResetThreshold = "PPPConfigurationDeviceMessage.pppDaemonResetThreshold";
    public static final String SetPPPRetryIntervalAttributeName = "PPPConfigurationDeviceMessage.setpppretryinterval";
    public static final String SetPPPOptionsAttributeName = "PPPConfigurationDeviceMessage.setpppoptions";

    public static final String SetFunctionAttributeName = "ChannelConfigurationDeviceMessage.setfunction";
    public static final String SetParametersAttributeName = "ChannelConfigurationDeviceMessage.setparameters";
    public static final String SetNameAttributeName = "ChannelConfigurationDeviceMessage.setname";
    public static final String SetUnitAttributeName = "ChannelConfigurationDeviceMessage.setunit";
    public static final String ChannelConfigurationChnNbrAttributeName = "ChannelConfigurationDeviceMessage.channelnumber";
    public static final String DivisorAttributeName = "ChannelConfigurationDeviceMessage.divisor";

    public static final String SetSumMaskAttributeName = "TotalizersConfigurationDeviceMessage.setsummask";
    public static final String SetSubstractMaskAttributeName = "TotalizersConfigurationDeviceMessage.setsubstractmask";

    public static final String SetActiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.setactivechannel";
    public static final String SetReactiveChannelAttributeName = "PeakShaverConfigurationDeviceMessage.setreactivechannel";
    public static final String SetTimeBaseAttributeName = "PeakShaverConfigurationDeviceMessage.settimebase";
    public static final String SetPOutAttributeName = "PeakShaverConfigurationDeviceMessage.setpout";
    public static final String SetPInAttributeName = "PeakShaverConfigurationDeviceMessage.setpin";
    public static final String SetDeadTimeAttributeName = "PeakShaverConfigurationDeviceMessage.setdeadtime";
    public static final String SetAutomaticAttributeName = "PeakShaverConfigurationDeviceMessage.setautomatic";
    public static final String SetCyclicAttributeName = "PeakShaverConfigurationDeviceMessage.setcyclic";
    public static final String SetInvertAttributeName = "PeakShaverConfigurationDeviceMessage.setinvert";
    public static final String SetAdaptSetpointAttributeName = "PeakShaverConfigurationDeviceMessage.setadaptsetpoint";
    public static final String SetInstantAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.setinstantanalogout";
    public static final String SetPredictedAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.setpredictedanalogout";
    public static final String SetpointAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.setpointanalogout";
    public static final String SetDifferenceAnalogOutAttributeName = "PeakShaverConfigurationDeviceMessage.setdifferenceanalogout";
    public static final String SetTariffAttributeName = "PeakShaverConfigurationDeviceMessage.settariff";
    public static final String SetResetLoadsAttributeName = "PeakShaverConfigurationDeviceMessage.setresetloads";
    public static final String CurrentValueAttributeName = "PeakShaverConfigurationDeviceMessage.currentvalue";
    public static final String NewValueAttributeName = "PeakShaverConfigurationDeviceMessage.newvalue";

    public static final String newPDRAttributeName = "ConfigurationChangeDeviceMessage.pdr";
    public static final String converterTypeAttributeName = "ConfigurationChangeDeviceMessage.convertertype";
    public static final String converterSerialNumberAttributeName = "ConfigurationChangeDeviceMessage.converter.serialnumber";
    public static final String meterTypeAttributeName = "ConfigurationChangeDeviceMessage.metertype";
    public static final String meterCaliberAttributeName = "ConfigurationChangeDeviceMessage.metercaliber";
    public static final String meterSerialNumberAttributeName = "ConfigurationChangeDeviceMessage.meter.serialnumber";
    public static final String gasDensityAttributeName = "ConfigurationChangeDeviceMessage.gas.density";
    public static final String airDensityAttributeName = "ConfigurationChangeDeviceMessage.air.density";
    public static final String relativeDensityAttributeName = "ConfigurationChangeDeviceMessage.relative.density";
    public static final String molecularNitrogenPercentageAttributeName = "ConfigurationChangeDeviceMessage.molecularnitrogen.percentage";
    public static final String carbonDioxidePercentageAttributeName = "ConfigurationChangeDeviceMessage.carbondioxide.percentage";
    public static final String molecularHydrogenPercentageAttributeName = "ConfigurationChangeDeviceMessage.molecularhydrogen.percentage";
    public static final String higherCalorificValueAttributeName = "ConfigurationChangeDeviceMessage.highercalorificvalue";
    public static final String billingPeriodLengthAttributeName = "ConfigurationChangeDeviceMessage.billingperiod.length";
    public static final String setOnDemandBillingDateAttributeName= "ConfigurationChangeDeviceMessage.ondemand.billing.date";
    public static final String OnDemandBillingReasonAttributeName = "ConfigurationChangeDeviceMessage.ondemand.billing.reason";
    public static final String UnitStatusAttributeName = "ConfigurationChangeDeviceMessage.unit.status";
    public static final String IgnoreDSTAttributeName = "ConfigurationChangeDeviceMessage.ignore.dst";
    public static final String StartOfGasDayAttributeName = "ConfigurationChangeDeviceMessage.start.of.gasday";
    public static final String enableRSSIMultipleSampling = "ConfigurationChangeDeviceMessage.enable.rssi.multiple.sampling";
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
    public static final String singleOptionAttributeName = "option";

    public static final String loadIdAttributeName = "PeakShaverConfigurationDeviceMessage.loadid";
    public static final String MaxOffAttributeName = "PeakShaverConfigurationDeviceMessage.maxoff";
    public static final String DelayAttributeName = "PeakShaverConfigurationDeviceMessage.delay";
    public static final String ManualAttributeName = "PeakShaverConfigurationDeviceMessage.manual";
    public static final String StatusAttributeName = "PeakShaverConfigurationDeviceMessage.status";
    public static final String PeakShaverIPAddressAttributeName = "PeakShaverConfigurationDeviceMessage.ipaddress";
    public static final String PeakShaveChnNbrAttributeName = "PeakShaverConfigurationDeviceMessage.channelnumber";

    public static final String SetInputChannelAttributeName = "EventsConfigurationDeviceMessage.setinputchannel";
    public static final String SetConditionAttributeName = "EventsConfigurationDeviceMessage.setcondition";
    public static final String SetConditionValueAttributeName = "EventsConfigurationDeviceMessage.setconditionvalue";
    public static final String SetTimeTrueAttributeName = "EventsConfigurationDeviceMessage.settimetrue";
    public static final String SetTimeFalseAttributeName = "EventsConfigurationDeviceMessage.settimefalse";
    public static final String SetOutputChannelAttributeName = "EventsConfigurationDeviceMessage.setoutputchannel";
    public static final String SetAlarmAttributeName = "EventsConfigurationDeviceMessage.setalarm";
    public static final String SetTagAttributeName = "EventsConfigurationDeviceMessage.settag";
    public static final String SetInverseAttributeName = "EventsConfigurationDeviceMessage.setinverse";
    public static final String SetImmediateAttributeName = "EventsConfigurationDeviceMessage.setimmediate";

    public static final String prepaidCreditAttributeName = "PrepaidConfigurationDeviceMessage.prepaidcredit";

    public static final String SetOpusOSNbrAttributeName = "OpusConfigurationDeviceMessage.setopusosnbr";
    public static final String SetOpusPasswordAttributeName = "OpusConfigurationDeviceMessage.setopuspassword";
    public static final String SetOpusTimeoutAttributeName = "OpusConfigurationDeviceMessage.setopustimeout";
    public static final String SetOpusConfigAttributeName = "OpusConfigurationDeviceMessage.setopusconfig";

    public static final String enableUplinkPing = "UplinkConfigurationDeviceMessage.enableUplinkPing";
    public static final String uplinkPingDestinationAddress = "UplinkConfigurationDeviceMessage.uplinkPingDestinationAddress";
    public static final String uplinkPingInterval = "UplinkConfigurationDeviceMessage.uplinkPingInterval";
    public static final String uplinkPingTimeout = "UplinkConfigurationDeviceMessage.uplinkPingTimeout";

    public static final String ReferenceVoltageAttributeName = "PowerConfigurationDeviceMessage.referencevoltage";
    public static final String VoltageSagTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.voltagesagtimethreshold";
    public static final String VoltageSwellTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.voltageswelltimethreshold";
    public static final String VoltageSagThresholdAttributeName = "PowerConfigurationDeviceMessage.voltagesagthreshold";
    public static final String VoltageSwellThresholdAttributeName = "PowerConfigurationDeviceMessage.voltageswellthreshold";
    public static final String LongPowerFailureTimeThresholdAttributeName = "PowerConfigurationDeviceMessage.longpowerfailuretimethreshold";
    public static final String LongPowerFailureThresholdAttributeName = "PowerConfigurationDeviceMessage.longpowerfailurethreshold";

    public static final String SetMmEveryAttributeName = "ModbusConfigurationDeviceMessage.setmmevery";
    public static final String SetMmTimeoutAttributeName = "ModbusConfigurationDeviceMessage.setmmtimeout";
    public static final String SetMmInstantAttributeName = "ModbusConfigurationDeviceMessage.setmminstant";
    public static final String SetMmOverflowAttributeName = "ModbusConfigurationDeviceMessage.setmmoverflow";
    public static final String SetMmConfigAttributeName = "ModbusConfigurationDeviceMessage.setmmconfig";
    public static final String RadixFormatAttributeName = "ModbusConfigurationDeviceMessage.radixformat";
    public static final String RegisterAddressAttributeName = "ModbusConfigurationDeviceMessage.registeraddress";
    public static final String RegisterValueAttributeName = "ModbusConfigurationDeviceMessage.RegisterValue(s)";


    public static final String SetMBusEveryAttributeName = "MBusConfigurationDeviceMessage.setmbusevery";
    public static final String SetMBusInterFrameTimeAttributeName = "MBusConfigurationDeviceMessage.setmbusinterframetime";
    public static final String SetMBusConfigAttributeName = "MBusConfigurationDeviceMessage.setmbusconfig";
    public static final String SetMBusVIFAttributeName = "MBusConfigurationDeviceMessage.mbusvif";

    public static final String openKeyAttributeName = "MBusSetupDeviceMessage.openkey";
    public static final String transferKeyAttributeName = "MBusSetupDeviceMessage.transferkey";
    public static final String defaultKeyAttributeName = "MBusSetupDeviceMessage.defaultkey";
    public static final String dib = "MBusSetupDeviceMessage.dib";
    public static final String vib = "MBusSetupDeviceMessage.vib";
    public static final String dibInstance1 = "MBusSetupDeviceMessage.dibInstance1";
    public static final String vibInstance1 = "MBusSetupDeviceMessage.vibInstance1";
    public static final String dibInstance2 = "MBusSetupDeviceMessage.dibInstance2";
    public static final String vibInstance2 = "MBusSetupDeviceMessage.vibInstance2";
    public static final String dibInstance3 = "MBusSetupDeviceMessage.dibInstance3";
    public static final String vibInstance3 = "MBusSetupDeviceMessage.vibInstance3";
    public static final String dibInstance4 = "MBusSetupDeviceMessage.dibInstance4";
    public static final String vibInstance4 = "MBusSetupDeviceMessage.vibInstance4";
    public static final String mbusChannel = "MBusSetupDeviceMessage.channel";
    public static final String mbusSerialNumber = "MBusSetupDeviceMessage.mbusSerialNumber";

    public static final String AnalogOutValue = "DeviceActionMessage.analogoutvalue";
    public static final String OutputOn = "DeviceActionMessage.outputon";
    public static final String OutputOff = "DeviceActionMessage.outputoff";
    public static final String OutputToggle = "DeviceActionMessage.outputtoggle";
    public static final String OutputPulse = "DeviceActionMessage.outputpulse";

    public static final String FTIONReboot = "DeviceActionMessage.ftionreboot";
    public static final String FTIONInitialize = "DeviceActionMessage.ftioninitialize";
    public static final String FTIONMailLog = "DeviceActionMessage.ftionmaillog";
    public static final String FTIONSaveConfig = "DeviceActionMessage.ftionsaveconfig";
    public static final String FTIONUpgrade = "DeviceActionMessage.ftionupgrade";
    public static final String FTIONClearMem = "DeviceActionMessage.ftionclearmem";
    public static final String FTIONMailConfig = "DeviceActionMessage.ftionmailconfig";
    public static final String FTIONModemReset = "DeviceActionMessage.ftionmodemreset";
    public static final String AdminOld = "DeviceActionMessage.adminold";
    public static final String AdminNew = "DeviceActionMessage.adminnew";

    public static final String IEC1107ClassIdAttributeName = "GeneralDeviceMessage.iec1107classid";
    public static final String OffsetAttributeName = "GeneralDeviceMessage.offset";
    public static final String RawDataAttributeName = "GeneralDeviceMessage.rawdata";

    public static final String MulticastAddress1AttributeName = "PLCConfigurationDeviceMessage.multicastaddress1";
    public static final String MulticastAddress2AttributeName = "PLCConfigurationDeviceMessage.multicastaddress2";
    public static final String MulticastAddress3AttributeName = "PLCConfigurationDeviceMessage.multicastaddress3";

    public static final String ActiveChannelAttributeName = "PLCConfigurationDeviceMessage.activechannel";
    public static final String CHANNEL1_FSAttributeName = "PLCConfigurationDeviceMessage.channel1_fs";
    public static final String CHANNEL1_FMAttributeName = "PLCConfigurationDeviceMessage.channel1_fm";
    public static final String CHANNEL1_SNRAttributeName = "PLCConfigurationDeviceMessage.channel1_snr";
    public static final String CHANNEL1_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel1_creditweight";
    public static final String CHANNEL2_FSAttributeName = "PLCConfigurationDeviceMessage.channel2_fs";
    public static final String CHANNEL2_FMAttributeName = "PLCConfigurationDeviceMessage.channel2_fm";
    public static final String CHANNEL2_SNRAttributeName = "PLCConfigurationDeviceMessage.channel2_snr";
    public static final String CHANNEL2_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel2_creditweight";
    public static final String CHANNEL3_FSAttributeName = "PLCConfigurationDeviceMessage.channel3_fs";
    public static final String CHANNEL3_FMAttributeName = "PLCConfigurationDeviceMessage.channel3_fm";
    public static final String CHANNEL3_SNRAttributeName = "PLCConfigurationDeviceMessage.channel3_snr";
    public static final String CHANNEL3_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel3_creditweight";
    public static final String CHANNEL4_FSAttributeName = "PLCConfigurationDeviceMessage.channel4_fs";
    public static final String CHANNEL4_FMAttributeName = "PLCConfigurationDeviceMessage.channel4_fm";
    public static final String CHANNEL4_SNRAttributeName = "PLCConfigurationDeviceMessage.channel4_snr";
    public static final String CHANNEL4_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel4_creditweight";
    public static final String CHANNEL5_FSAttributeName = "PLCConfigurationDeviceMessage.channel5_fs";
    public static final String CHANNEL5_FMAttributeName = "PLCConfigurationDeviceMessage.channel5_fm";
    public static final String CHANNEL5_SNRAttributeName = "PLCConfigurationDeviceMessage.channel5_snr";
    public static final String CHANNEL5_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel5_creditweight";
    public static final String CHANNEL6_FSAttributeName = "PLCConfigurationDeviceMessage.channel6_fs";
    public static final String CHANNEL6_FMAttributeName = "PLCConfigurationDeviceMessage.channel6_fm";
    public static final String CHANNEL6_SNRAttributeName = "PLCConfigurationDeviceMessage.channel6_snr";
    public static final String CHANNEL6_CREDITWEIGHTAttributeName = "PLCConfigurationDeviceMessage.channel6_creditweight";

    public static final String MAX_RECEIVING_GAINAttributeName = "PLCConfigurationDeviceMessage.max_receiving_gain";
    public static final String MAX_TRANSMITTING_GAINAttributeName = "PLCConfigurationDeviceMessage.max_transmitting_gain";
    public static final String SEARCH_INITIATOR_GAINAttributeName = "PLCConfigurationDeviceMessage.search_initiator_gain";

    public static final String SEARCH_INITIATOR_TIMEOUTAttributeName = "PLCConfigurationDeviceMessage.search_initiator_timeout";
    public static final String SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName = "PLCConfigurationDeviceMessage.synchronization_confirmation_timeout";
    public static final String TIME_OUT_NOT_ADDRESSEDAttributeName = "PLCConfigurationDeviceMessage.time_out_not_addressed";
    public static final String TIME_OUT_FRAME_NOT_OKAttributeName = "PLCConfigurationDeviceMessage.time_out_frame_not_ok";

    public static final String MAX_FRAME_LENGTHAttributeName = "PLCConfigurationDeviceMessage.max_frame_length";
    public static final String REPEATERAttributeName = "PLCConfigurationDeviceMessage.repeater";

    public static final String INITIATOR_ELECTRICAL_PHASEAttributeName = "PLCConfigurationDeviceMessage.initiator_electrical_phase";

    public static final String ZigBeeConfigurationSASPanIdAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.panid";
    public static final String ZigBeeConfigurationForceRemovalAttributeName = "ZigBeeConfigurationDeviceMessage.forceremoval";
    public static final String ZigBeeConfigurationZigBeeLinkKeyAttributeName = "ZigBeeConfigurationDeviceMessage.zigbeelinkkey";
    public static final String ZigBeeConfigurationActivationDateAttributeName = "ZigBeeConfigurationDeviceMessage.activationdate";
    public static final String ZigBeeConfigurationZigBeeAddressAttributeName = "ZigBeeConfigurationDeviceMessage.zigbeeieeeaddress";
    public static final String ZigBeeConfigurationMirrorAddressAttributeName = "ZigBeeConfigurationDeviceMessage.mirrorieeeaddress";
    public static final String ZigBeeConfigurationFirmwareUpdateUserFileAttributeName = "ZigBeeConfigurationDeviceMessage.userfile";
    public static final String ZigBeeConfigurationSASInsecureJoinAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.insecurejoin";
    public static final String ZigBeeConfigurationSASExtendedPanIdAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.extendedpanid";
    public static final String ZigBeeConfigurationSASPanChannelMaskAttributeName = "ZigBeeConfigurationDeviceMessage.SAS.panchannelmask";
    public static final String ZigBeeConfigurationHANRestoreUserFileAttributeName = "ZigBeeConfigurationDeviceMessage.hanrestoreuserfile";
    public static final String ZigBeeConfigurationDeviceType = "ZigBeeConfigurationDeviceMessage.devicetype";

    public static final String StandingChargeAttributeName = "PricingInformation.standingcharge";
    public static final String PricingInformationUserFileAttributeName = "PricingInformation.userfile";
    public static final String PricingInformationActivationDateAttributeName = "PricingInformation.activationdate";
    public static final String currency = "currency";

    public static final String alarmRegisterAttributeName = "AlarmConfigurationMessage.alarmRegister";
    public static final String alarmBitMaskAttributeName = "AlarmConfigurationMessage.alarmBitMask";
    public static final String alarmFilterAttributeName = "AlarmConfigurationMessage.alarmfilter";
    public static final String configUserFileAttributeName = "GeneralDeviceMessage.configuserfile";
    public static final String xmlMessageAttributeName = "GeneralDeviceMessage.xmlMessage";
    public static final String transportTypeAttributeName = "AlarmConfigurationMessage.transportType";
    public static final String objectDefinitionsAttributeName = "AlarmConfigurationMessage.objectDefinitions";
    public static final String typeAttributeName = "type";
    public static final String destinationAddressAttributeName = "AlarmConfigurationMessage.destinationAddress";
    public static final String messageTypeAttributeName = "AlarmConfigurationMessage.messageType";

    public static final String broadCastLogTableEntryTTLAttributeName = "PLCConfigurationDeviceMessage.broadcastlogtableentryttl";
    public static final String maxJoinWaitTime = "PLCConfigurationDeviceMessage.maxJoinWaitTime";
    public static final String pathDiscoveryTime = "PLCConfigurationDeviceMessage.pathDiscoveryTime";
    public static final String maxNumberOfHopsAttributeName = "PLCConfigurationDeviceMessage.maxnumberofhops";
    public static final String metricType = "PLCConfigurationDeviceMessage.metricType";
    public static final String coordShortAddress = "PLCConfigurationDeviceMessage.coordShortAddress";
    public static final String toneMaskAttributeName = "PLCConfigurationDeviceMessage.tonemask";
    public static final String TMRTTL = "PLCConfigurationDeviceMessage.mtrTTL";
    public static final String MaxFrameRetries = "PLCConfigurationDeviceMessage.maxFrameRetries";
    public static final String NeighbourTableEntryTTL = "PLCConfigurationDeviceMessage.neighbourTableEntryTTL";
    public static final String HighPriorityWindowSize = "PLCConfigurationDeviceMessage.highPriorityWindowSize";
    public static final String CSMAFairnessLimit = "PLCConfigurationDeviceMessage.csmaFairnessLimit";
    public static final String BeaconRandomizationWindowLength = "PLCConfigurationDeviceMessage.beaconRandomizationWindowLength";
    public static final String MacA = "PLCConfigurationDeviceMessage.macA";
    public static final String MacK = "PLCConfigurationDeviceMessage.macK";
    public static final String MinimumCWAttempts = "PLCConfigurationDeviceMessage.minimumCWAttempts";
    public static final String maxBe = "PLCConfigurationDeviceMessage.maxBe";
    public static final String maxCSMABackOff = "PLCConfigurationDeviceMessage.maxCSMABackOff";
    public static final String minBe = "PLCConfigurationDeviceMessage.minBe";
    public static final String plcSecurityLevel = "PLCConfigurationDeviceMessage.plcSecurityLevel";
    public static final String weakLQIValueAttributeName = "PLCConfigurationDeviceMessage.weaklqivalue";
    public static final String plcG3TimeoutAttributeName = "PLCConfigurationDeviceMessage.plcg3timeout";
    public static final String G3PanIdAttributename = "PLCConfigurationDeviceMessage.g3panid";
    public static final String adp_Kr = "PLCConfigurationDeviceMessage.adp_Kr";
    public static final String adp_Km = "PLCConfigurationDeviceMessage.adp_Km";
    public static final String adp_Kc = "PLCConfigurationDeviceMessage.adp_Kc";
    public static final String adp_Kq = "PLCConfigurationDeviceMessage.adp_Kq";
    public static final String adp_Kh = "PLCConfigurationDeviceMessage.adp_Kh";
    public static final String adp_Krt = "PLCConfigurationDeviceMessage.adp_Krt";
    public static final String adp_RREQ_retries = "PLCConfigurationDeviceMessage.adp_RREQ_retries";
    public static final String adp_RLC_enabled = "PLCConfigurationDeviceMessage.adp_RLC_enabled";
    public static final String adp_net_traversal_time = "PLCConfigurationDeviceMessage.adp_net_traversal_time";
    public static final String adp_routing_table_entry_TTL = "PLCConfigurationDeviceMessage.adp_routing_table_entry_TTL";
    public static final String adp_RREQ_RERR_wait = "PLCConfigurationDeviceMessage.adp_RREQ_RERR_wait";
    public static final String adp_Blacklist_table_entry_TTL = "PLCConfigurationDeviceMessage.adp_Blacklist_table_entry_TTL";
    public static final String adp_unicast_RREQ_gen_enable = "PLCConfigurationDeviceMessage.adp_unicast_RREQ_gen_enable";
    public static final String adp_add_rev_link_cost = "PLCConfigurationDeviceMessage.adp_add_rev_link_cost";
    public static final String disableDefaultRouting = "PLCConfigurationDeviceMessage.disableDefaultRouting";
    public static final String deviceType = "PLCConfigurationDeviceMessage.deviceType";
    public static final String pingEnabled = "PLCConfigurationDeviceMessage.pingEnabled";
    public static final String routeRequestEnabled = "PLCConfigurationDeviceMessage.routeRequestEnabled";
    public static final String pathRequestEnabled = "PLCConfigurationDeviceMessage.pathRequestEnabled";
    public static final String EnableSNR = "PLCConfigurationDeviceMessage.enableSNR";
    public static final String SNRPacketInterval = "PLCConfigurationDeviceMessage.snrPacketInterval";
    public static final String SNRQuietTime = "PLCConfigurationDeviceMessage.snrQuietTime";
    public static final String SNRPayload = "PLCConfigurationDeviceMessage.snrPayload";
    public static final String EnableKeepAlive = "PLCConfigurationDeviceMessage.enableKeepAlive";
    public static final String keepAliveStartTime = "PLCConfigurationDeviceMessage.keepAliveStartTime";
    public static final String keepAliveSendPeriod = "PLCConfigurationDeviceMessage.keepAliveSendPeriod";
    public static final String KeepAliveScheduleInterval = "PLCConfigurationDeviceMessage.keepAliveScheduleInterval";
    public static final String KeepAliveBucketSize = "PLCConfigurationDeviceMessage.keepAliveBucketSize";
    public static final String minInactiveMeterTime = "PLCConfigurationDeviceMessage.minInactiveMeterTime";
    public static final String maxInactiveMeterTime = "PLCConfigurationDeviceMessage.maxInactiveMeterTime";
    public static final String KeepAliveRetries = "PLCConfigurationDeviceMessage.keepAliveRetries";
    public static final String KeepAliveTimeout = "PLCConfigurationDeviceMessage.keepAliveTimeout";

    public static final String groupName = "PLCConfigurationDeviceMessage.groupName";
    public static final String bitSync = "PLCConfigurationDeviceMessage.bitSync";
    public static final String zeroCrossAdjust = "PLCConfigurationDeviceMessage.zeroCrossAdjust";
    public static final String txGain = "PLCConfigurationDeviceMessage.txGain";
    public static final String rxGain = "PLCConfigurationDeviceMessage.rxGain";
    public static final String addCredit = "PLCConfigurationDeviceMessage.addCredit";
    public static final String minCredit = "PLCConfigurationDeviceMessage.minCredit";

    public static final String interval = "interval";
    public static final String duration = "duration";
    public static final String deviceId = "rtuDeviceId";
    public static final String trackingId = "trackingId";
    public static final String delete = "delete";
    public static final String startTime = "startTime";
    public static final String macAddress = "macAddress";
    public static final String output = "output";
    public static final String enabled = "enabled";
    public static final String outputId = "OutputConfigurationMessage.outputId";
    public static final String newState = "OutputConfigurationMessage.newState";

    public static final String EnableDLMS = "FirewallConfigurationMessage.enableDLMS";
    public static final String EnableHTTP = "FirewallConfigurationMessage.enableHTTP";
    public static final String EnableSSH = "FirewallConfigurationMessage.enableSSH";
    public static final String defaultEnabled = "FirewallConfigurationMessage.defaultEnabled";

    public static final String receptionThreshold = "receptionThreshold";
    public static final String numberOfTimeSlotsForNewSystems = "numberOfTimeSlotsForNewSystems";
    public static final String outputBitMap = "outputBitMap";
    public static final String endTime = "endTime";
    public static final String rfAddress = "rfAddress";
    public static final String waveCardFirmware = "waveCardFirmware";
    public static final String nodeListUserFile = "nodeListUserFile";
    public static final String enableWavenis = "enableWavenis";
    public static final String rfCommand = "rfCommand";
    public static final String friendlyName = "friendlyName";
    public static final String preferredL1NodeList = "preferredL1NodeList";
    public static final String enablePLC = "enablePLC";
    public static final String enableBootSync = "enableBootSync";
    public static final String frequencyPair = "frequencyPair";
    public static final String discoveryMaxCredits = "discoveryMaxCredits";
    public static final String fileInfo = "fileInfo";
    public static final String sslCertificateUserFile = "sslCertificateUserFile";
    public static final String servletURL = "servletURL";
    public static final String logLevel = "logLevel";
}