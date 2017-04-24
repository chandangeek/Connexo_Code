/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 * Describes an enumeration of all possible Events defined in the AS300 meter.
 */
public enum AS300EventDefinitions {

    MeterCoverRemoved(0, MeterEvent.COVER_OPENED, "Meter cover removed"),
    MeterCoverReplaced(1, MeterEvent.METER_COVER_CLOSED, "Meter cover replaced"),
    StrongMagneticField(2, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong Magnetic field"),
    StrongMagneticFieldRemoved(3, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "No Strong Magnetic field"),
    BatteryFailure(4, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery Failure"),
    BatteryLow(5, MeterEvent.BATTERY_VOLTAGE_LOW, "Low Battery"),
    ProgramMemoryError(6, MeterEvent.ROM_MEMORY_ERROR, "Program Memory Error"),
    RamError(7, MeterEvent.RAM_MEMORY_ERROR, "RAM Error"),
    NVMemoryError(8, MeterEvent.NV_MEMORY_ERROR, "NV Memory Error"),
    MeasurementSystemError(9, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Measurement System Error"),
    WatchDogError(10, MeterEvent.WATCHDOG_ERROR, "Watchdog Error"),
    SupplyDisconnectFailure(11, MeterEvent.REMOTE_DISCONNECTION, "Supply Disconnect Failure"),
    SupplyConnectFailure(13, MeterEvent.REMOTE_CONNECTION, "Supply Connect Failure"),
    DSTEnabled(14, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Enabled"),
    DSTDisabled(15, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Disabled"),
    ClockAdjustedBackWards(16, MeterEvent.SETCLOCK, "Clock Adjust Backward"),
    ClockAdjustForward(17, MeterEvent.SETCLOCK, "Clock Adjust Forward"),
    ClockInvalid(18, MeterEvent.CLOCK_INVALID, "Clock Invalid"),
    PowerLoss(22, MeterEvent.POWERDOWN, "Power Loss"),
    IncorrectProtocol(23, MeterEvent.OTHER, "Incorrect Protocol"),
    ErrorRegisterCleared(27, MeterEvent.CLEAR_DATA, "Error Register Cleared"),
    AlarmRegisterCleared(29, MeterEvent.ALARM_REGISTER_CLEARED, "Alarm Register Cleared"),
    EventLogCleared(31, MeterEvent.EVENT_LOG_CLEARED, "Event Log Cleared"),
    ManualDisconnect(32, MeterEvent.MANUAL_DISCONNECTION, "Manual Disconnect"),
    ManualConnect(33, MeterEvent.MANUAL_CONNECTION, "Manual Connect"),
    RemoteDisconnect(34, MeterEvent.REMOTE_DISCONNECTION, "Remote Disconnect"),
    LocalDisconnect(35, MeterEvent.LOCAL_DISCONNECTION, "Local Disconnect"),
    LoadProfileCleared(40, MeterEvent.LOADPROFILE_CLEARED, "Load Profile Cleared"),
    FirmwareReadyforActivation(41, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Firmware Ready for Activation"),
    FirmwareActivated(42, MeterEvent.FIRMWARE_ACTIVATED, "Firmware Activated"),
    TOUTariffActivation(44, MeterEvent.TOU_ACTIVATED, "TOU Tariff Activation"),
    Block_8x8_TariffActivation(45, MeterEvent.TOU_ACTIVATED, "8x8 (Block) Tariff Activation"),
    SingleRateTariffActivation(46, MeterEvent.TOU_ACTIVATED, "Single Rate Tariff Activation"),
    PowerUp(52, MeterEvent.POWERUP, "Power Up"),
    ClockSynchronisation(98, MeterEvent.SETCLOCK, "Clock Synchronisation"),
    IncorrectPolarity_ReverseRun(128, MeterEvent.REVERSE_RUN, "Incorrect Polarity (Reverse Run)"),
    UnderVoltage_1(130, MeterEvent.VOLTAGE_SAG, "Under Voltage #1"),
    OverVoltage_1(131, MeterEvent.VOLTAGE_SWELL, "Over Voltage #1"),
    PowerFactorUnderThreshold(133, MeterEvent.OTHER, "Power Factor Under Threshold"),
    PowerFactorOverThreshold(134, MeterEvent.OTHER, "Power Factor Over Threshold"),
    TerminalCoverRemoved(135, MeterEvent.TERMINAL_OPENED, "Terminal Cover Removed"),
    TerminalCoverReplaced(136, MeterEvent.TERMINAL_COVER_CLOSED, "Terminal Cover Replaced"),
    UnderVoltage_2(138, MeterEvent.VOLTAGE_SAG, "Under Voltage #2"),
    OverVoltage_2(139, MeterEvent.VOLTAGE_SWELL, "Over Voltage #2"),
    AsynchronousBilling_ChangeofTenancy(244, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Change of Tenancy"),
    ChangeofTenancy(245, MeterEvent.OTHER, "Change of Tenancy"),
    AsynchronousBilling_ChangeofSupplier(250, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Change of Supplier"),
    ChangeofSupplier(251, MeterEvent.OTHER, "Change of Supplier"),
    PriceChangeReceived(256, MeterEvent.OTHER, "Price Change Received"),
    PriceChangeActivated(258, MeterEvent.OTHER, "Price Change Activated"),
    AsynchronousBilling_PriceMatrixChange(259, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Price Matrix Change"),
    BlockThresholdChangeReceived(260, MeterEvent.OTHER, "Block Threshold Change Received"),
    BlockThresholdActivated(262, MeterEvent.OTHER, "Block Threshold Activated"),
    CurrencyChangeRecevied(264, MeterEvent.OTHER, "Currency Change Recevied"),
    CurrencyChangeActivated(266, MeterEvent.OTHER, "Currency Change Activated"),
    TariffChangeReceived(268, MeterEvent.OTHER, "Tariff Change Received"),
    TariffChangeActivated(270, MeterEvent.OTHER, "Tariff Change Activated"),
    AsynchronousBilling_TariffOperatingModeChange(271, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Tariff Operating Mode Change"),
    TOUChangeReceived(272, MeterEvent.OTHER, "TOU Change Received"),
    TOUChangeActivated(274, MeterEvent.OTHER, "TOU Change Activated"),
    AsynchronousBilling_ActivityCalendarActivation(275, MeterEvent.BILLING_ACTION, "Asynchronous Billing - Activity Calendar Activation"),
    BlockPeriodChangeReceived(276, MeterEvent.OTHER, "Block Period Change Received"),
    BlockPeriodChangeActivated(278, MeterEvent.OTHER, "Block Period Change Activated"),
    DLMSKeyChange_DataCollectionAuthentication(317, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Data Collection Authentication"),
    DLMSKeyChange_ExtendedDataCollectionAuthentication(318, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Extended Data Collection Authentication"),
    DLMSKeyChange_ManagementAuthentication(319, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Management Authentication"),
    DLMSKeyChange_FirmwareAuthentication(320, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Firmware Authentication"),
    DLMSKeyChange_DataCollectionEncryption(324, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Data Collection Encryption"),
    DLMSKeyChange_ExtendedDataCollectionEncryption(325, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Extended Data Collection Encryption"),
    DLMSKeyChange_ManagementEncryption(326, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Management Encryption"),
    DLMSKeyChange_FirmwareEncryption(327, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Firmware Encryption"),
    DLMSKeyFailure_DataCollectionAuthentication(331, MeterEvent.OTHER, "DLMS Key Failure: Data Collection Authentication"),
    DLMSKeyFailure_ExtendedDataCollectionAuthentication(332, MeterEvent.OTHER, "DLMS Key Failure: Extended Data Collection Authentication"),
    DLMSKeyFailure_ManagementAuthentication(333, MeterEvent.OTHER, "DLMS Key Failure: Management Authentication"),
    DLMSKeyFailure_FirmwareAuthentication(334, MeterEvent.OTHER, "DLMS Key Failure: Firmware Authentication"),
    DLMSKeyFailure_DataCollectionEncryption(338, MeterEvent.OTHER, "DLMS Key Failure: Data Collection Encryption"),
    DLMSKeyFailure_ExtendedDataCollectionEncryption(339, MeterEvent.OTHER, "DLMS Key Failure: Extended Data Collection Encryption"),
    DLMSKeyFailure_ManagementEncryption(340, MeterEvent.OTHER, "DLMS Key Failure: Management Encryption"),
    DLMSKeyFailure_FirmwareEncryption(341, MeterEvent.OTHER, "DLMS Key Failure: Firmware Encryption"),
    Limiter1Enabled(345, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Enabled"),
    Limiter2Enabled(346, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Enabled"),
    Limiter3Enabled(347, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Enabled"),
    Limiter4Enabled(348, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Enabled"),
    Limiter5Enabled(349, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Enabled"),
    Limiter6Enabled(350, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Enabled"),
    Limiter1OverThreshold(351, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 1 Over Threshold"),
    Limiter2OverThreshold(352, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 2 Over Threshold"),
    Limiter3OverThreshold(353, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 3 Over Threshold"),
    Limiter4OverThreshold(354, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 4 Over Threshold"),
    Limiter5OverThreshold(355, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 5 Over Threshold"),
    Limiter6OverThreshold(356, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 6 Over Threshold"),
    Limiter1UnderThreshold(357, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 1 Under Threshold"),
    Limiter2UnderThreshold(358, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 2 Under Threshold"),
    Limiter3UnderThreshold(359, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 3 Under Threshold"),
    Limiter4UnderThreshold(360, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 4 Under Threshold"),
    Limiter5UnderThreshold(361, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 5 Under Threshold"),
    Limiter6UnderThreshold(362, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter 6 Under Threshold"),
    Limiter1Disabled(363, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Disabled"),
    Limiter2Disabled(364, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Disabled"),
    Limiter3Disabled(365, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Disabled"),
    Limiter4Disabled(366, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Disabled"),
    Limiter5Disabled(367, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Disabled"),
    Limiter6Disabled(368, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Disabled"),
    Limiter1ThresholdChange(369, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 1 Threshold Change"),
    Limiter2ThresholdChange(370, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 2 Threshold Change"),
    Limiter3ThresholdChange(371, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 3 Threshold Change"),
    Limiter4ThresholdChange(372, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 4 Threshold Change"),
    Limiter5ThresholdChange(373, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 5 Threshold Change"),
    Limiter6ThresholdChange(374, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter 6 Threshold Change"),
    ContactorOpenviaLimiter1(461, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 1"),
    ContactorOpenviaLimiter2(462, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 2"),
    ContactorOpenviaLimiter3(463, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 3"),
    ContactorOpenviaLimiter4(464, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 4"),
    ContactorOpenviaLimiter5(465, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 5"),
    ContactorOpenviaLimiter6(466, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Open via Limiter 6"),
    ContactorArmviaLimiter1(467, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 1"),
    ContactorArmviaLimiter2(468, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 2"),
    ContactorArmviaLimiter3(469, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 3"),
    ContactorArmviaLimiter4(470, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 4"),
    ContactorArmviaLimiter5(471, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 5"),
    ContactorArmviaLimiter6(472, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Contactor Arm via Limiter 6"),
    BeginIF2Communications(480, MeterEvent.OTHER, "Begin IF2 Communications"),
    EndIF2Communications(481, MeterEvent.OTHER, "End IF2 Communications"),
    BeginOpticalCommunications(482, MeterEvent.OTHER, "Begin Optical Communications"),
    EndOpticalCommunications(483, MeterEvent.OTHER, "End Optical Communications"),
    BeginSerialCommunications(484, MeterEvent.OTHER, "Begin Serial Communications"),
    EndSerialCommunications(485, MeterEvent.OTHER, "End Serial Communications"),
    TOUSeasonUpdate(486, MeterEvent.OTHER, "TOU Season Update"),
    TOUWeekUpdate(487, MeterEvent.OTHER, "TOU Week Update"),
    TOUDayUpdate(488, MeterEvent.OTHER, "TOU Day Update"),
    TOUSpecialDateUpdate(489, MeterEvent.OTHER, "TOU Special Date Update"),
    IF2AuthenticationKeyChange(490, MeterEvent.CONFIGURATIONCHANGE, "IF2 Authentication Key Change"),
    IF2EncryptionKeyChange(491, MeterEvent.CONFIGURATIONCHANGE, "IF2 Encryption Key Change"),
    IF2AuthenticationKeyFailure(492, MeterEvent.OTHER, "IF2 Authentication Key Failure"),
    IF2EncryptionKeyFailure(493, MeterEvent.OTHER, "IF2 Encryption Key Failure"),
    ActiveImportRateChange(495, MeterEvent.CONFIGURATIONCHANGE, "Active Import Rate Change"),
    NeutralLoss(496, MeterEvent.VOLTAGE_SAG, "Neutral Loss"),
    DLMSKeyChange_ManufacturingAuthentication(497, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key Change: Manufacturing Authentication"),
    InstrumentationProfileCleared(498, MeterEvent.CLEAR_DATA, "Instrumentation Profile Cleared"),
    AnticreepEnd(499, MeterEvent.OTHER, "Anticreep End"),
    AnticreepStart(500, MeterEvent.OTHER, "Anticreep Start"),
    CriticalInternalError(510, MeterEvent.FATAL_ERROR, "Critical Internal Error"),

    UNKNOWNEVENT(9999, MeterEvent.OTHER, "UnKnown EVENT!!");

    /**
     * The EventCode (or ProtocolCode) we from the Device
     */
    private int deviceCode;
    /**
     * The mapped deviceCode to a {@link MeterEvent} code
     */
    private final int eiserverCode;
    /**
     * The description of the event
     */
    private final String description;

    private AS300EventDefinitions(final int deviceCode, final int eiserverCode, final String description) {
        this.description = description;
        this.eiserverCode = eiserverCode;
        this.deviceCode = deviceCode;
    }

    public static AS300EventDefinitions getEventFromDeviceCode(final int deviceCode) {
        for (AS300EventDefinitions as300Event : values()) {
            if (as300Event.deviceCode == deviceCode) {
                return as300Event;
            }
        }
        return updateDeviceCode(UNKNOWNEVENT, deviceCode);
    }

    private static AS300EventDefinitions updateDeviceCode(AS300EventDefinitions event, int deviceCode) {
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
