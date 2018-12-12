package com.energyict.smartmeterprotocolimpl.elster.AS300P.eventhandling;

import com.energyict.protocol.MeterEvent;

/**
 * Describes an enumeration of all possible Events defined in the AS300 meter.
 */
public enum AS300PEventDefinitions {

    ErrorRegisterCleared(0, MeterEvent.CLEAR_DATA, "Event Register Cleared"),
    AlarmRegisterCleared(1, MeterEvent.ALARM_REGISTER_CLEARED, "Alarm Register Cleared"),
    EventLogCleared(2, MeterEvent.EVENT_LOG_CLEARED, "Event Log Cleared"),
    EventFilterChanged(3, MeterEvent.OTHER, "Event Filter Changed"),
    LoadProfileCleared(4, MeterEvent.LOADPROFILE_CLEARED, "Load Profile Cleared"),

    DLMSKeyChange_DataCollectionAuthentication(11, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Data Collection Authentication"),
    DLMSKeyFailure_DataCollectionAuthentication(12, MeterEvent.OTHER, "DLMS Key Failure: Data Collection Authentication"),
    DLMSKeyChange_ExtendedDataCollectionAuthentication(14, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Extended Data Collection Authentication"),
    DLMSKeyFailure_ExtendedDataCollectionAuthentication(15, MeterEvent.OTHER, "DLMS Key Failure: Extended Data Collection Authentication"),
    DLMSKeyChange_FirmwareAuthentication(17, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Firmware Authentication"),
    DLMSKeyFailure_FirmwareAuthentication(18, MeterEvent.OTHER, "DLMS Key Failure: Firmware Authentication"),
    DLMSKeyChange_ManagementAuthentication(20, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Management Authentication"),
    DLMSKeyFailure_ManagementAuthentication(21, MeterEvent.OTHER, "DLMS Key Failure: Management Authentication"),
    DLMSKeyChange_DataCollectionEncryption(23, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Data Collection Encryption"),
    DLMSKeyFailure_DataCollectionEncryption(24, MeterEvent.OTHER, "DLMS Key Failure: Data Collection Encryption"),
    DLMSKeyChange_ExtendedDataCollectionEncryption(26, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Extended Data Collection Encryption"),
    DLMSKeyFailure_ExtendedDataCollectionEncryption(27, MeterEvent.OTHER, "DLMS Key Failure: Extended Data Collection Encryption"),
    DLMSKeyChange_FirmwareEncryption(29, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Firmware Encryption"),
    DLMSKeyFailure_FirmwareEncryption(30, MeterEvent.OTHER, "DLMS Key Failure: Firmware Encryption"),
    DLMSKeyChange_ManagementEncryption(32, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Management Encryption"),
    DLMSKeyFailure_ManagementEncryption(33, MeterEvent.OTHER, "DLMS Key Failure: Management Encryption"),

    ClockAdjustForward(50, MeterEvent.SETCLOCK, "Clock Adjust Forward"),
    ClockAdjustedBackWards(51, MeterEvent.SETCLOCK, "Clock Adjust Backward"),
    ClockChangeFail(54, MeterEvent.OTHER, "Clock Change Fail"),
    DSTDisabled(55, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Disabled"),
    DSTEnabled(56, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Enabled"),
    ClockInvalid(57, MeterEvent.CLOCK_INVALID, "Clock Invalid"),
    ClockSynchronisation(58, MeterEvent.SETCLOCK, "Clock Synchronisation"),

    AsynchronousBilling(80, MeterEvent.BILLING_ACTION, "Asynchronous Billing Occurred"),
    SynchronousBilling(81, MeterEvent.BILLING_ACTION, "Synchronous Billing Occurred"),
    TariffChangeActivated(82, MeterEvent.OTHER, "Tariff Change Activated"),
    TariffChangeReceived(83, MeterEvent.OTHER, "Tariff Change Received"),
    PriceChangeActivated(84, MeterEvent.OTHER, "Price Change Activated"),
    PriceChangeReceived(85, MeterEvent.OTHER, "Price Change Received"),
    CurrencyChangeActivated(56, MeterEvent.OTHER, "Currency Change Activated"),
    CurrencyChangeReceived(87, MeterEvent.OTHER, "Currency Change Received"),
    EmergencyTariffActivated(88, MeterEvent.OTHER, "Emergency Tariff Activated"),
    EmergencyTariffDeactivated(89, MeterEvent.OTHER, "Currency Emergency Tariff Deactivated"),
    CalendarChangeReceived(90, MeterEvent.OTHER, "Calendar Change Received"),
    CalendarChangeActivated(91, MeterEvent.OTHER, "Calendar Change Activated"),

    DisconnectedDueToLoadLimit(102, MeterEvent.OTHER, "Disconnected Due To Load Limit"),
    SupplyConnectFailure(103, MeterEvent.REMOTE_CONNECTION, "Supply Connect Failure"),
    SupplyDisconnectFailure(104, MeterEvent.REMOTE_DISCONNECTION, "Supply Disconnect Failure"),
    LocalDisconnect(107, MeterEvent.LOCAL_DISCONNECTION, "Local Disconnect"),
    ManualConnect(108, MeterEvent.MANUAL_CONNECTION, "Manual Connect"),
    ManualDisconnect(109, MeterEvent.MANUAL_DISCONNECTION, "Manual Disconnect"),
    RemoteDisconnect(111, MeterEvent.REMOTE_DISCONNECTION, "Remote Disconnect"),

    Limiter1ThresholdChange(115, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Threshold Change"),
    Limiter2ThresholdChange(116, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Threshold Change"),
    Limiter3ThresholdChange(117, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Threshold Change"),
    Limiter4ThresholdChange(118, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Threshold Change"),
    Limiter5ThresholdChange(119, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Threshold Change"),
    Limiter6ThresholdChange(120, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Threshold Change"),
    Limiter1Disabled(121, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Disabled"),
    Limiter2Disabled(122, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Disabled"),
    Limiter3Disabled(123, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Disabled"),
    Limiter4Disabled(124, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Disabled"),
    Limiter5Disabled(125, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Disabled"),
    Limiter6Disabled(126, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Disabled"),
    Limiter1Enabled(127, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Enabled"),
    Limiter2Enabled(128, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Enabled"),
    Limiter3Enabled(129, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Enabled"),
    Limiter4Enabled(130, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Enabled"),
    Limiter5Enabled(131, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Enabled"),
    Limiter6Enabled(132, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Enabled"),
    Limiter1OverThreshold(133, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 1 Over Threshold"),
    Limiter2OverThreshold(134, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 2 Over Threshold"),
    Limiter3OverThreshold(135, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 3 Over Threshold"),
    Limiter4OverThreshold(136, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 4 Over Threshold"),
    Limiter5OverThreshold(137, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 5 Over Threshold"),
    Limiter6OverThreshold(138, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 6 Over Threshold"),
    Limiter1UnderThreshold(139, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 1 Under Threshold"),
    Limiter2UnderThreshold(140, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 2 Under Threshold"),
    Limiter3UnderThreshold(141, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 3 Under Threshold"),
    Limiter4UnderThreshold(142, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 4 Under Threshold"),
    Limiter5UnderThreshold(143, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 5 Under Threshold"),
    Limiter6UnderThreshold(144, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 6 Under Threshold"),

    FrequencyOver(200, MeterEvent.OTHER, "Frequency Over"),
    FrequencyUnder(201, MeterEvent.OTHER, "Frequency Under"),
    IncorrectPolarity_ReverseRun(202, MeterEvent.REVERSE_RUN, "Incorrect Polarity (Reverse Run)"),
    IncorrectProtocol(203, MeterEvent.OTHER, "Incorrect Protocol"),
    OverCurrent(207, MeterEvent.OTHER, "Over Current"),
    PowerFactorUnderThreshold(208, MeterEvent.OTHER, "Power Factor Under Threshold"),
    PowerFactorOverThreshold(209, MeterEvent.OTHER, "Power Factor Over Threshold"),
    OverVoltage_1(215, MeterEvent.VOLTAGE_SWELL, "Over Voltage #1"),
    OverVoltage_1End(216, MeterEvent.VOLTAGE_SWELL, "Over Voltage #1 End"),
    OverVoltage_2(217, MeterEvent.VOLTAGE_SWELL, "Over Voltage #2"),
    OverVoltage_2End(218, MeterEvent.VOLTAGE_SWELL, "Over Voltage #2 End"),
    UnderVoltage_1(219, MeterEvent.VOLTAGE_SAG, "Under Voltage #1"),
    UnderVoltage_1End(220, MeterEvent.VOLTAGE_SAG, "Under Voltage #1 End"),
    UnderVoltage_2(221, MeterEvent.VOLTAGE_SAG, "Under Voltage #2"),
    UnderVoltage_2End(222, MeterEvent.VOLTAGE_SAG, "Under Voltage #2 End"),

    MaxDemandEnergyAPlusRate1Updated(230, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy A+ Rate 1 Updated"),
    MaxDemandEnergyAPlusRate2Updated(231, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy A+ Rate 2 Updated"),
    MaxDemandEnergyAPlusTotalUpdated(232, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy A+ Total Updated"),
    MaxDemandEnergyRPlusTotalUpdated(233, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy R+ Total Updated"),
    MaxDemandEnergyAMinTotalUpdated(234, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy A- Total Updated"),
    MaxDemandEnergyRMinTotalUpdated(235, MeterEvent.CONFIGURATIONCHANGE, "Max Demand Energy R- Total Updated"),

    NVMemoryError(240, MeterEvent.NV_MEMORY_ERROR, "NV Memory Error"),
    RamError(243, MeterEvent.RAM_MEMORY_ERROR, "RAM Error"),
    WatchDogError(246, MeterEvent.WATCHDOG_ERROR, "Watchdog Error"),

    FirmwareActivated(260, MeterEvent.FIRMWARE_ACTIVATED, "Firmware Activated"),
    FirmwareCancelled(261, MeterEvent.OTHER, "Firmware Cancelled"),
    FirmwareReadyForActivation(263, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Firmware Ready For Activation"),
    FirmwareReceived(264, MeterEvent.OTHER, "Firmware Received"),
    FirmwareRejected(265, MeterEvent.OTHER, "Firmware Rejected"),

    ConsumerMessageMeterActivated(290, MeterEvent.OTHER, "Consumer Message - Meter - Activated"),
    ConsumerMessageMeterReset(291, MeterEvent.OTHER, "Consumer Message - Meter - Reset"),
    ConsumerMessageMeterReceived(292, MeterEvent.OTHER, "Consumer Message - Meter - Received"),
    ConsumerMessageMeterRejected(293, MeterEvent.OTHER, "Consumer Message - Meter - Rejected"),
    ConsumerMessageIPDActivated(294, MeterEvent.OTHER, "Consumer Message - IPD - Activated"),
    ConsumerMessageIPDReset(295, MeterEvent.OTHER, "Consumer Message - IPD - Reset"),
    ConsumerMessageIPDReceived(296, MeterEvent.OTHER, "Consumer Message - IPD - Received"),
    ConsumerMessageIPDRejected(297, MeterEvent.OTHER, "Consumer Message - IPD - Rejected"),

    ChangeofSupplierActivated(300, MeterEvent.OTHER, "Change of Supplier Activated"),
    ChangeofSupplierReceived(302, MeterEvent.OTHER, "Change of Supplier Received"),
    ChangeofTenancyActivated(304, MeterEvent.OTHER, "Change of Tenancy Activated"),
    ChangeofTenancyReceived(306, MeterEvent.OTHER, "Change of Tenancy Received"),

    TOUDayUpdate(308, MeterEvent.OTHER, "TOU Day Update"),
    TOUSeasonUpdate(312, MeterEvent.OTHER, "TOU Season Update"),
    TOUSpecialDateUpdate(316, MeterEvent.OTHER, "TOU Special Date Update"),
    TOUWeekUpdate(320, MeterEvent.OTHER, "TOU Week Update"),
    BlockPeriodChangeActivated(328, MeterEvent.OTHER, "Block Period Change Activated"),
    BlockPeriodChangeReceived(330, MeterEvent.OTHER, "Block Period Change Received"),
    BlockThresholdActivated(332, MeterEvent.OTHER, "Block Threshold Activated"),
    BlockThresholdChangeReceived(334, MeterEvent.OTHER, "Block Threshold Change Received"),

    AsynchronousBilling_ChangeofTenancy(376, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Change of Tenancy"),
    AsynchronousBilling_ChangeofSupplier(377, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Change of Supplier"),
    AsynchronousBilling_PriceMatrixChange(378, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Price Matrix Change"),
    AsynchronousBilling_CurrencyChange(379, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Currency Change"),
    AsynchronousBilling_ClockFail(380, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Clock Fail"),
    AsynchronousBilling_ActivityCalendarActivation(381, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Activity Calendar Activation"),
    AsynchronousBilling_BlockPeriodChange(382, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Block Period Change"),

    ContactorOpenviaLimiter1(383, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 1"),
    ContactorOpenviaLimiter2(384, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 2"),
    ContactorOpenviaLimiter3(385, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 3"),
    ContactorOpenviaLimiter4(386, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 4"),
    ContactorOpenviaLimiter5(387, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 5"),
    ContactorOpenviaLimiter6(388, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 6"),
    ContactorArmviaLimiter1(389, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 1"),
    ContactorArmviaLimiter2(390, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 2"),
    ContactorArmviaLimiter3(391, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 3"),
    ContactorArmviaLimiter4(392, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 4"),
    ContactorArmviaLimiter5(393, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 5"),
    ContactorArmviaLimiter6(394, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 6"),

    BeginSerialCommunications(395, MeterEvent.OTHER, "Begin Serial Communications"),
    EndSerialCommunications(396, MeterEvent.OTHER, "End Serial Communications"),
    BeginIF2Communications(397, MeterEvent.OTHER, "Begin IF2 Communications"),
    EndIF2Communications(398, MeterEvent.OTHER, "End IF2 Communications"),
    BeginOpticalCommunications(399, MeterEvent.OTHER, "Begin Optical Communications"),
    EndOpticalCommunications(400, MeterEvent.OTHER, "End Optical Communications"),
    IF2AuthenticationKeyChange(401, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: IF2 Authentication"),
    IF2AuthenticationKeyFailure(402, MeterEvent.OTHER, "DLMS Key Failure: IF2 Authentication"),
    IF2EncryptionKeyChange(403, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: IF2 Encryption"),
    IF2EncryptionKeyFailure(404, MeterEvent.OTHER, "DLMS Key Failure: IF2 Encryption"),
    TOUAPlusRateUpdate(405, MeterEvent.OTHER, "TOU A+ Rate Update"),
    NeutralLoss(406, MeterEvent.VOLTAGE_SAG, "Neutral Loss"),
    DLMSKeyChange_ManufacturingAuthentication(407, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Manufacturing Authentication"),
    InstrumentationProfileCleared(408, MeterEvent.CLEAR_DATA, "Instrumentation Profile Cleared"),
    AnticreepEnd(409, MeterEvent.OTHER, "Anticreep End"),
    AnticreepStart(410, MeterEvent.OTHER, "Anticreep Start"),
    CriticalInternalError(411, MeterEvent.FATAL_ERROR, "Critical Internal Error"),
    ResetWithoutSave(412, MeterEvent.OTHER, "Reset Without Save"),

    BatteryFailure(472, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery Failure"),
    BatteryLow(473, MeterEvent.BATTERY_VOLTAGE_LOW, "Low Battery"),
    PowerLoss(477, MeterEvent.POWERDOWN, "Power Loss"),
    PowerUp(478, MeterEvent.POWERUP, "Power Up"),
    MeterCoverReplaced(480, MeterEvent.METER_COVER_CLOSED, "Meter Cover Replaced"),
    MeterCoverRemoved(481, MeterEvent.COVER_OPENED, "Meter Cover Removed"),
    StrongMagneticField(484, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong Magnetic Field Detected"),
    StrongMagneticFieldRemoved(485, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "Strong Magnetic Field Removed"),
    TerminalCoverRemoved(486, MeterEvent.TERMINAL_OPENED, "Terminal Cover Removed"),
    TerminalCoverReplaced(487, MeterEvent.TERMINAL_COVER_CLOSED, "Terminal Cover Replaced"),
    MeasurementSystemError(492, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Measurement System Error"),
    ProgramMemoryError(494, MeterEvent.ROM_MEMORY_ERROR, "Program Memory Error"),
    EngineeringAccessed(500, MeterEvent.OTHER, "Engineering Accessed"),
    AlarmQueued(501, MeterEvent.OTHER, "Alarm Queued"),

    UNKNOWNEVENT(9999, MeterEvent.OTHER, "UnKnown EVENT!!");

    /**
     * The EventCode (or ProtocolCode) we from the Device
     */
    private int deviceCode;
    /**
     * The mapped deviceCode to a {@link com.energyict.protocol.MeterEvent} code
     */
    private final int eiserverCode;
    /**
     * The description of the event
     */
    private final String description;

    private AS300PEventDefinitions(final int deviceCode, final int eiserverCode, final String description) {
        this.description = description;
        this.eiserverCode = eiserverCode;
        this.deviceCode = deviceCode;
    }

    public static AS300PEventDefinitions getEventFromDeviceCode(final int deviceCode) {
        for (AS300PEventDefinitions as300Event : values()) {
            if (as300Event.deviceCode == deviceCode) {
                return as300Event;
            }
        }
        return updateDeviceCode(UNKNOWNEVENT, deviceCode);
    }

    private static AS300PEventDefinitions updateDeviceCode(AS300PEventDefinitions event, int deviceCode) {
        event.deviceCode = deviceCode;
        return event;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public int getEiserverCode() {
        return eiserverCode;
    }

    public String getDescription() {
        return description;
    }
}
