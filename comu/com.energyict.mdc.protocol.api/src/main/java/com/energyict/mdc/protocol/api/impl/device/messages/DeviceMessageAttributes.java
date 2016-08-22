package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
public enum DeviceMessageAttributes implements TranslationKey {

    firmwareUpdateFileAttributeName(DeviceMessageConstants.firmwareUpdateFileAttributeName, "Firmware version"),
    resumeFirmwareUpdateAttributeName(DeviceMessageConstants.resumeFirmwareUpdateAttributeName, "Resume firmware upload"),
    plcTypeFirmwareUpdateAttributeName(DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName, "PLC type"),
    firmwareUpdateActivationDateAttributeName(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName, "Activation date"),
    firmwareUpdateVersionNumberAttributeName(DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName, "Version number"),
    firmwareUpdateURLAttributeName(DeviceMessageConstants.firmwareUpdateURLAttributeName, "Download url"),
    controlThreshold1dAttributeName(DeviceMessageConstants.controlThreshold1dAttributeName, "ControlThreshold 1"),
    controlThreshold2dAttributeName(DeviceMessageConstants.controlThreshold2dAttributeName, "ControlThreshold 2"),
    controlThreshold3dAttributeName(DeviceMessageConstants.controlThreshold3dAttributeName, "ControlThreshold 3"),
    controlThreshold4dAttributeName(DeviceMessageConstants.controlThreshold4dAttributeName, "ControlThreshold 4"),
    controlThreshold5dAttributeName(DeviceMessageConstants.controlThreshold5dAttributeName, "ControlThreshold 5"),
    controlThreshold6dAttributeName(DeviceMessageConstants.controlThreshold6dAttributeName, "ControlThreshold 6"),
    normalThresholdAttributeName(DeviceMessageConstants.normalThresholdAttributeName, "Normal threshold"),
    unitAttributeName(DeviceMessageConstants.unitAttributeName, "Unit"),
    tariffsAttributeName(DeviceMessageConstants.tariffsAttributeName, "Tariff(s)"),
    readingTypeAttributeName(DeviceMessageConstants.readingTypeAttributeName, "Reading type"),
    emergencyThresholdAttributeName(DeviceMessageConstants.emergencyThresholdAttributeName, "Emergency threshold"),
    activationDatedAttributeName(DeviceMessageConstants.activationDatedAttributeName, "Activation date"),
    DemandCloseToContractPowerThresholdAttributeName(DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName, "Demand close to contractual power threshold"),
    overThresholdDurationAttributeName(DeviceMessageConstants.overThresholdDurationAttributeName, "Over threshold duration"),
    underThresholdDurationAttributeName(DeviceMessageConstants.underThresholdDurationAttributeName, "Under threshold duration"),
    emergencyProfileIdAttributeName(DeviceMessageConstants.emergencyProfileIdAttributeName, "Emergency profileid"),
    emergencyProfileActivationDateAttributeName(DeviceMessageConstants.emergencyProfileActivationDateAttributeName, "Emergency profile activation date"),
    emergencyProfileDurationAttributeName(DeviceMessageConstants.emergencyProfileDurationAttributeName, "Emergency profile duration"),
    emergencyProfileGroupIdListAttributeName(DeviceMessageConstants.emergencyProfileGroupIdListAttributeName, "Emergency profile group id list"),
    actionWhenUnderThresholdAttributeName(DeviceMessageConstants.actionWhenUnderThresholdAttributeName, "Action when under threshold"),
    readFrequencyInMinutesAttributeName(DeviceMessageConstants.readFrequencyInMinutesAttributeName, "Read frequency (minutes)"),
    invertDigitalOutput1AttributeName(DeviceMessageConstants.invertDigitalOutput1AttributeName, "Invert digital output 1"),
    invertDigitalOutput2AttributeName(DeviceMessageConstants.invertDigitalOutput2AttributeName, "Invert digital output 2"),
    activateNowAttributeName(DeviceMessageConstants.activateNowAttributeName, "Activate now"),
    monitoredValueAttributeName(DeviceMessageConstants.monitoredValueAttributeName, "Monitored value"),
    loadLimitGroupIDAttributeName(DeviceMessageConstants.loadLimitGroupIDAttributeName, "GroupID"),
    loadLimitStartDateAttributeName(DeviceMessageConstants.loadLimitStartDateAttributeName, "Start date"),
    loadLimitEndDateAttributeName(DeviceMessageConstants.loadLimitEndDateAttributeName, "End date"),
    powerLimitThresholdAttributeName(DeviceMessageConstants.powerLimitThresholdAttributeName, "Power limit threshold"),
    contractualPowerLimitAttributeName(DeviceMessageConstants.contractualPowerLimitAttributeName, "Contractual power limit"),
    phaseAttributeName(DeviceMessageConstants.phaseAttributeName, "Phase"),
    thresholdInAmpereAttributeName(DeviceMessageConstants.thresholdInAmpereAttributeName, "threshold (Ampere)"),
    keyTActivationStatusAttributeName(DeviceMessageConstants.keyTActivationStatusAttributeName, "Activation status T"),
    SecurityTimeDurationAttributeName(DeviceMessageConstants.SecurityTimeDurationAttributeName, "time duration"),
    executionKeyAttributeName(DeviceMessageConstants.executionKeyAttributeName, "Execution key"),
    temporaryKeyAttributeName(DeviceMessageConstants.temporaryKeyAttributeName, "Temporary key"),
    eventLogResetSealAttributeName(DeviceMessageConstants.eventLogResetSealAttributeName, "Event log reset seal"),
    restoreFactorySettingsSealAttributeName(DeviceMessageConstants.restoreFactorySettingsSealAttributeName, "Restore factory settings seal"),
    restoreDefaultSettingsSealAttributeName(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName, "Restore default settings seal"),
    statusChangeSealAttributeName(DeviceMessageConstants.statusChangeSealAttributeName, "Status change seal"),
    remoteConversionParametersConfigSealAttributeName(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName, "Remote conversion parameters config seal"),
    remoteAnalysisParametersConfigSealAttributeName(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName, "Remote analysis parameters config seal"),
    downloadProgramSealAttributeName(DeviceMessageConstants.downloadProgramSealAttributeName, "Download program seal"),
    restoreDefaultPasswordSealAttributeName(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName, "Restore default password seal"),
    eventLogResetSealBreakTimeAttributeName(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, "Eventlog reset seal breaktime"),
    restoreFactorySettingsSealBreakTimeAttributeName(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, "Restore factory settings seal breaktime"),
    restoreDefaultSettingsSealBreakTimeAttributeName(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, "Restore default settings seal breaktime"),
    statusChangeSealBreakTimeAttributeName(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, "Status change seal breaktime"),
    remoteConversionParametersConfigSealBreakTimeAttributeName(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, "Remote conversion parameters config seal breaktime"),
    remoteAnalysisParametersConfigSealBreakTimeAttributeName(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, "Remote analysis parameters config seal breaktime"),
    downloadProgramSealBreakTimeAttributeName(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, "Download program seal breaktime"),
    restoreDefaultPasswordSealBreakTimeAttributeName(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, "Restore default password seal breaktime"),
    randomBytesAttributeName(DeviceMessageConstants.randomBytesAttributeName, "32 random bytes"),
    deviceListAttributeName(DeviceMessageConstants.deviceListAttributeName, "Device group"),
    encryptionLevelAttributeName(DeviceMessageConstants.encryptionLevelAttributeName, "Encryption level"),
    authenticationLevelAttributeName(DeviceMessageConstants.authenticationLevelAttributeName, "Authentication level"),
    newReadingClientPasswordAttributeName(DeviceMessageConstants.newReadingClientPasswordAttributeName, "New reading client password"),
    newManagementClientPasswordAttributeName(DeviceMessageConstants.newManagementClientPasswordAttributeName, "New management client password"),
    newFirmwareClientPasswordAttributeName(DeviceMessageConstants.newFirmwareClientPasswordAttributeName, "New firmware client password"),
    pskAttributeName(DeviceMessageConstants.pskAttributeName, "psk"),
    newEncryptionKeyAttributeName(DeviceMessageConstants.newEncryptionKeyAttributeName, "New encryption key"),
    newWrappedEncryptionKeyAttributeName(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName, "New wrapped encryption key"),
    newAuthenticationKeyAttributeName(DeviceMessageConstants.newAuthenticationKeyAttributeName, "New authentication key"),
    newWrappedAuthenticationKeyAttributeName(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName, "New wrapped authentication key"),
    newPasswordAttributeName(DeviceMessageConstants.newPasswordAttributeName, "new password"),
    newHexPasswordAttributeName(DeviceMessageConstants.newHexPasswordAttributeName, "new hex password"),
    preparedDataAttributeName(DeviceMessageConstants.preparedDataAttributeName, "Prepared data"),
    signatureAttributeName(DeviceMessageConstants.signatureAttributeName, "signature"),
    verificationKeyAttributeName(DeviceMessageConstants.verificationKeyAttributeName, "Verification key"),
    activityCalendarTypeAttributeName(DeviceMessageConstants.activityCalendarTypeAttributeName, "Activity calendar type"),
    activityCalendarContractAttributeName(DeviceMessageConstants.contractAttributeName, "Contract"),
    activityCalendarNameAttributeName(DeviceMessageConstants.activityCalendarNameAttributeName, "Activity calendar name"),
    activityCalendarAttributeName(DeviceMessageConstants.activityCalendarAttributeName, "Activity calendar"),
    specialDaysAttributeName(DeviceMessageConstants.specialDaysAttributeName, "Special days"),
    activityCalendarActivationDateAttributeName(DeviceMessageConstants.activityCalendarActivationDateAttributeName, "Activity calendar activation date"),
    contractsXmlUserFileAttributeName(DeviceMessageConstants.contractsXmlUserFileAttributeName, "contracts xml userFile"),
    WriteExchangeStatus(DeviceMessageConstants.WriteExchangeStatus, "Write exchange status"),
    WriteRadioAcknowledge(DeviceMessageConstants.WriteRadioAcknowledge, "Write radio acknowledge"),
    WriteRadioUserTimeout(DeviceMessageConstants.WriteRadioUserTimeout, "Write radio user timeout"),
    converterTypeAttributeName(DeviceMessageConstants.converterTypeAttributeName, "Converter type"),
    converterSerialNumberAttributeName(DeviceMessageConstants.converterSerialNumberAttributeName, "Converter serial number"),
    meterTypeAttributeName(DeviceMessageConstants.meterTypeAttributeName, "Meter type"),
    meterCaliberAttributeName(DeviceMessageConstants.meterCaliberAttributeName, "Meter caliber"),
    meterSerialNumberAttributeName(DeviceMessageConstants.meterSerialNumberAttributeName, "Meter serialnumber"),
    gasDensityAttributeName(DeviceMessageConstants.gasDensityAttributeName, "Gas density"),
    airDensityAttributeName(DeviceMessageConstants.airDensityAttributeName, "Air density"),
    relativeDensityAttributeName(DeviceMessageConstants.relativeDensityAttributeName, "Relative density"),
    molecularNitrogenAttributeName(DeviceMessageConstants.molecularNitrogenAttributeName, "Molecular nitrogen percentage"),
    carbonDioxideAttributeName(DeviceMessageConstants.carbonDioxideAttributeName, "Carbondioxide percentage"),
    molecularHydrogenAttributeName(DeviceMessageConstants.molecularHydrogenAttributeName, "Molecular hydrogen percentage"),
    higherCalorificValueAttributeName(DeviceMessageConstants.higherCalorificValueAttributeName, "Higher calorific value"),
    SetDescriptionAttributeName(DeviceMessageConstants.SetDescriptionAttributeName, "Description"),
    SetIntervalInSecondsAttributeName(DeviceMessageConstants.SetIntervalInSecondsAttributeName, "Interval (Seconds)"),
    SetUpgradeUrlAttributeName(DeviceMessageConstants.SetUpgradeUrlAttributeName, "UpgradeUrl"),
    SetUpgradeOptionsAttributeName(DeviceMessageConstants.SetUpgradeOptionsAttributeName, "Upgrade options"),
    SetDebounceTresholdAttributeName(DeviceMessageConstants.SetDebounceTresholdAttributeName, "Debounce treshold"),
    SetTariffMomentAttributeName(DeviceMessageConstants.SetTariffMomentAttributeName, "Tariff moment"),
    SetCommOffsetAttributeName(DeviceMessageConstants.SetCommOffsetAttributeName, "Com offset"),
    SetAggIntvAttributeName(DeviceMessageConstants.SetAggIntvAttributeName, "Set aggIntv"),
    SetPulseTimeTrueAttributeName(DeviceMessageConstants.SetPulseTimeTrueAttributeName, "Set pulseTimeTrue"),
    SetDukePowerIDAttributeName(DeviceMessageConstants.SetDukePowerIDAttributeName, "Set DukePowerID"),
    SetDukePowerPasswordAttributeName(DeviceMessageConstants.SetDukePowerPasswordAttributeName, "Set DukePowerPassword"),
    SetDukePowerIdleTimeAttributeName(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName, "Set DukePowerIdleTime"),
    ConfigurationChangeDate(DeviceMessageConstants.ConfigurationChangeDate, "Date"),
    ChangeOfSupplierName(DeviceMessageConstants.ChangeOfSupplierName, "Change of supplier name"),
    ChangeOfSupplierID(DeviceMessageConstants.ChangeOfSupplierID, "Change of supplier ID"),
    ConfigurationChangeActivationDate(DeviceMessageConstants.ConfigurationChangeActivationDate, "Activation date"),
    AlarmFilterAttributeName(DeviceMessageConstants.AlarmFilterAttributeName, "Alarm filter"),
    DefaultResetWindowAttributeName(DeviceMessageConstants.DefaultResetWindowAttributeName, "Default reset window"),
    AdministrativeStatusAttributeName(DeviceMessageConstants.AdministrativeStatusAttributeName, "Administrative status"),
    CalorificValue(DeviceMessageConstants.CalorificValue, "Calorific value"),
    ConversionFactor(DeviceMessageConstants.ConversionFactor, "Conversion factor"),
    enableSSL(DeviceMessageConstants.enableSSL, "Enable SSL"),
    deviceName(DeviceMessageConstants.deviceName, "Device name"),
    deviceId(DeviceMessageConstants.deviceId, "Device id"),
    trackingId(DeviceMessageConstants.trackingId, "Tracking id"),
    servletURL(DeviceMessageConstants.servletURL, "Servlet URL"),
    logLevel(DeviceMessageConstants.logLevel, "Log level"),
    fileInfo(DeviceMessageConstants.fileInfo, "File information"),
    DeviceActionMessageYear(DeviceMessageConstants.year, "year"),
    DeviceActionMessageMonth(DeviceMessageConstants.month, "month"),
    DeviceActionMessageDay(DeviceMessageConstants.day, "day"),
    DeviceActionMessageDayOfMonth(DeviceMessageConstants.dayOfMonth, "day of month"),
    DeviceActionMessageDayOfWeek(DeviceMessageConstants.dayOfWeek, "day of week"),
    DeviceActionMessageHour(DeviceMessageConstants.hour, "hour"),
    DeviceActionMessageMinute(DeviceMessageConstants.minute, "minute"),
    DeviceActionMessageSecond(DeviceMessageConstants.second, "second"),
    DeviceActionMessageId("DeviceActionMessage." + DeviceMessageConstants.id, "ID"),
    DeviceActionMessageContract("DeviceActionMessage." + DeviceMessageConstants.contractAttributeName, "Contract"),
    OutputConfigurationMessageId("OutputConfigurationMessage." + DeviceMessageConstants.id, "ID"),
    OutputConfigurationMessageDelete("OutputConfigurationMessage." + DeviceMessageConstants.delete, "Delete"),
    OutputConfigurationMessageDuration("OutputConfigurationMessage." + DeviceMessageConstants.duration, "Duration"),
    capturePeriodAttributeName(DeviceMessageConstants.capturePeriodAttributeName, "capture period"),
    consumerProducerModeAttributeName(DeviceMessageConstants.consumerProducerModeAttributeName, "Consumer producer mode"),
    SetMmEveryAttributeName(DeviceMessageConstants.SetMmEveryAttributeName, "SetMmEvery"),
    SetMmTimeoutAttributeName(DeviceMessageConstants.SetMmTimeoutAttributeName, "SetMmTimeout"),
    SetMmInstantAttributeName(DeviceMessageConstants.SetMmInstantAttributeName, "SetMmInstant"),
    SetMmOverflowAttributeName(DeviceMessageConstants.SetMmOverflowAttributeName, "SetMmOverflow"),
    SetMmConfigAttributeName(DeviceMessageConstants.SetMmConfigAttributeName, "SetMmConfig"),
    RadixFormatAttributeName(DeviceMessageConstants.RadixFormatAttributeName, "Radix format"),
    RegisterAddressAttributeName(DeviceMessageConstants.RegisterAddressAttributeName, "Register address"),
    RegisterValueAttributeName(DeviceMessageConstants.RegisterValueAttributeName, "Register value(s)"),
    usernameAttributeName("NetWorkConnectivity." + DeviceMessageConstants.usernameAttributeName, "Username"),
    passwordAttributeName("NetWorkConnectivity." + DeviceMessageConstants.passwordAttributeName, "Password"),
    apnAttributeName(DeviceMessageConstants.apnAttributeName, "APN"),
    whiteListPhoneNumbersAttributeName(DeviceMessageConstants.whiteListPhoneNumbersAttributeName, "whitelist phone numbers"),
    discoverDuration(DeviceMessageConstants.discoverDuration, "discover duration"),
    discoverInterval(DeviceMessageConstants.discoverInterval, "discover interval"),
    repeaterCallInterval(DeviceMessageConstants.repeaterCallInterval, "repeater call interval"),
    repeaterCallThreshold(DeviceMessageConstants.repeaterCallThreshold, "repeater call threshold"),
    repeaterCallTimeslots(DeviceMessageConstants.repeaterCallTimeslots, "repeater call timeslots"),
    systemRebootThreshold(DeviceMessageConstants.systemRebootThreshold, "system reboot threshold"),
    managedWhiteListPhoneNumbersAttributeName(DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName, "Managed phone number whitelist"),
    smsCenterPhoneNumberAttributeName(DeviceMessageConstants.smsCenterPhoneNumberAttributeName, "smscenter phone number"),
    devicePhoneNumberAttributeName(DeviceMessageConstants.devicePhoneNumberAttributeName, "device phone number"),
    ipAddressAttributeName(DeviceMessageConstants.ipAddressAttributeName, "ip Address"),
    portNumberAttributeName(DeviceMessageConstants.portNumberAttributeName, "Port number"),
    wakeupPeriodAttributeName(DeviceMessageConstants.wakeupPeriodAttributeName, "Wakeup period"),
    inactivityTimeoutAttributeName(DeviceMessageConstants.inactivityTimeoutAttributeName, "Inactivity timeout"),
    SetProxyServerAttributeName(DeviceMessageConstants.SetProxyServerAttributeName, "Set proxy server"),
    SetProxyUsernameAttributeName(DeviceMessageConstants.SetProxyUsernameAttributeName, "Set proxy username"),
    SetProxyPasswordAttributeName(DeviceMessageConstants.SetProxyPasswordAttributeName, "Set proxy password"),
    SetDHCPAttributeName(DeviceMessageConstants.SetDHCPAttributeName, "Set DHCP"),
    SetDHCPTimeoutAttributeName(DeviceMessageConstants.SetDHCPTimeoutAttributeName, "Set DHCP timeout"),
    SetIPAddressAttributeName(DeviceMessageConstants.SetIPAddressAttributeName, "Set IP address"),
    SetSubnetMaskAttributeName(DeviceMessageConstants.SetSubnetMaskAttributeName, "Set subnetmask"),
    SetGatewayAttributeName(DeviceMessageConstants.SetGatewayAttributeName, "Set gateway"),
    SetNameServerAttributeName(DeviceMessageConstants.SetNameServerAttributeName, "Set name server"),
    SetHttpPortAttributeName(DeviceMessageConstants.SetHttpPortAttributeName, "Set Http port"),
    NetworkConnectivityIPAddressAttributeName(DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName, "IP address"),
    NetworkConnectivityIntervalAttributeName(DeviceMessageConstants.NetworkConnectivityIntervalAttributeName, "Interval"),
    preferGPRSUpstreamCommunication(DeviceMessageConstants.preferGPRSUpstreamCommunication, "preferGPRSUpstreamCommunication"),
    enableModemWatchdog(DeviceMessageConstants.enableModemWatchdog, "Enable modem watchdog"),
    modemWatchdogInterval(DeviceMessageConstants.modemWatchdogInterval, "Modem watchdog interval"),
    modemResetThreshold(DeviceMessageConstants.modemResetThreshold, "Modem reset threshold"),
    PPPDaemonResetThreshold(DeviceMessageConstants.PPPDaemonResetThreshold, "PPP daemon reset threshold"),
    startTime("NetWorkConnectivity." + DeviceMessageConstants.startTime, "Start time"),
    endTime("NetWorkConnectivity." + DeviceMessageConstants.endTime, "End time"),
    OutputConfigurationMessageOutputBitMap("OutputConfigurationMessage." + DeviceMessageConstants.outputBitMap, "Output bitmap"),
    OutputConfigurationMessageOutputId(DeviceMessageConstants.outputId, "Output id"),
    OutputConfigurationMessageNewState(DeviceMessageConstants.newState, "New state"),
    alarmFilterAttributeName(DeviceMessageConstants.alarmFilterAttributeName, "Alarm filter"),
    configUserFileAttributeName(DeviceMessageConstants.configUserFileAttributeName, "Configuration file"),
    transportTypeAttributeName(DeviceMessageConstants.transportTypeAttributeName, "Transport type"),
    destinationAddressAttributeName(DeviceMessageConstants.destinationAddressAttributeName, "Destination address"),
    messageTypeAttributeName(DeviceMessageConstants.messageTypeAttributeName, "Message type"),
    FTIONReboot(DeviceMessageConstants.FTIONReboot, "FTI on reboot"),
    FTIONInitialize(DeviceMessageConstants.FTIONInitialize, "FTI on initialize"),
    FTIONMailLog(DeviceMessageConstants.FTIONMailLog, "FTI on mail log"),
    FTIONSaveConfig(DeviceMessageConstants.FTIONSaveConfig, "FTI on save configuration"),
    FTIONUpgrade(DeviceMessageConstants.FTIONUpgrade, "FTI on upgrade"),
    FTIONClearMem(DeviceMessageConstants.FTIONClearMem, "FTI on clear memory"),
    FTIONMailConfig(DeviceMessageConstants.FTIONMailConfig, "FTI on mail configuration"),
    FTIONModemReset(DeviceMessageConstants.FTIONModemReset, "FTI on modem reset"),
    AdminOld(DeviceMessageConstants.AdminOld, "Admin old"),
    AdminNew(DeviceMessageConstants.AdminNew, "Admin new"),
    AnalogOutValue(DeviceMessageConstants.AnalogOutValue, "Analog out value"),
    OutputOn(DeviceMessageConstants.OutputOn, "Output on"),
    OutputOff(DeviceMessageConstants.OutputOff, "Output off"),
    OutputToggle(DeviceMessageConstants.OutputToggle, "Output toggle"),
    OutputPulse(DeviceMessageConstants.OutputPulse, "Output pulse"),
    output(DeviceMessageConstants.output, "Output"),
    IEC1107ClassIdAttributeName(DeviceMessageConstants.IEC1107ClassIdAttributeName, "GeneralDeviceMessage.IEC1107ClassId"),
    OffsetAttributeName(DeviceMessageConstants.OffsetAttributeName, "GeneralDeviceMessage.Offset"),
    RawDataAttributeName(DeviceMessageConstants.RawDataAttributeName, "GeneralDeviceMessage.RawData"),
    xmlConfigAttributeName(DeviceMessageConstants.xmlConfigAttributeName, "xml config"),
    UserFileConfigAttributeName(DeviceMessageConstants.UserFileConfigAttributeName, "Configuration file"),
    prepaidCreditAttributeName(DeviceMessageConstants.prepaidCreditAttributeName, "PrepaidConfigurationDeviceMessage.prepaidCredit"),
    SetSumMaskAttributeName(DeviceMessageConstants.SetSumMaskAttributeName, "Set sumMask"),
    SetSubstractMaskAttributeName(DeviceMessageConstants.SetSubstractMaskAttributeName, "Set substractMask"),
    ;

    private final String key;
    private final String defaultFormat;

    DeviceMessageAttributes(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
