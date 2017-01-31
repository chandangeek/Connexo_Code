/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.generic.CommonObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes;

public class AS300ObisCodeProvider implements CommonObisCodeProvider {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode ClockSynchronizationObisCode = ObisCode.fromString("0.0.96.2.12.255");
    public static final ObisCode SerialNumberObisCode = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode LoadProfileP1 = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode EncryptedLP = ObisCode.fromString("1.128.99.2.0.255");
    public static final ObisCode LoadProfileDaily = ObisCode.fromString("0.0.98.2.0.255");
    public static final ObisCode LoadProfileMonthly = ObisCode.fromString("0.0.98.1.0.255");
    public static final ObisCode LoadProfileBlockDaily = ObisCode.fromString("0.0.98.2.1.255");       //Block profile data   ==> E = 1
    public static final ObisCode LoadProfileBlockMonthly = ObisCode.fromString("0.0.98.1.1.255");     //Block profile data   ==> E = 1
    public static final ObisCode AssociationLnCurrentClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode AssociationLnPublicClient = ObisCode.fromString("0.0.40.0.1.255");
    public static final ObisCode AssociationLnDataCollectionClient = ObisCode.fromString("0.0.40.0.2.255");
    public static final ObisCode AssociationLnExtDataCollectionClient = ObisCode.fromString("0.0.40.0.3.255");
    public static final ObisCode AssociationLnManagementClient = ObisCode.fromString("0.0.40.0.4.255");
    public static final ObisCode AssociationLnFirmwareClient = ObisCode.fromString("0.0.40.0.5.255");
    public static final ObisCode AssociationLnManufactureClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode FirmwareVersionObisCode = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode MIDCheckSumObisCode = ObisCode.fromString("1.0.0.2.8.255");
    public static final ObisCode FormerFirmwareVersionObisCode = ObisCode.fromString("1.0.0.1.0.255");
    public static final ObisCode E_OperationalFirmwareVersionObisCode = ObisCode.fromString("1.3.0.2.0.255");
    public static final ObisCode ActiveLongFirmwareIdentifierACOR = ObisCode.fromString("1.2.0.2.0.255");
    public static final ObisCode ActiveLongFirmwareIdentifierMCOR = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode ReferenceTime = ObisCode.fromString("0.128.3.6.0.255");
    public static final ObisCode FrameCounterPublicClient = ObisCode.fromString("0.0.43.1.1.255");
    public static final ObisCode FrameCounterDataCollectionClient = ObisCode.fromString("0.0.43.1.2.255");
    public static final ObisCode FrameCounterExtDataCollectionClient = ObisCode.fromString("0.0.43.1.3.255");
    public static final ObisCode FrameCounterManagementClient = ObisCode.fromString("0.0.43.1.4.255");
    public static final ObisCode FrameCounterFirmwareClient = ObisCode.fromString("0.0.43.1.5.255");
    public static final ObisCode FrameCounterManufactureClient = ObisCode.fromString("0.0.43.1.255.255");

    public static final ObisCode clockShiftEventLimit = ObisCode.fromString("1.0.0.9.11.255");
    public static final ObisCode clockShiftInvalidLimit = ObisCode.fromString("1.1.94.34.1.255");
    public static final ObisCode clockSyncWindow = ObisCode.fromString("1.0.0.9.9.255");

    public static final ObisCode LastBillingResetTimeStamp = ObisCode.fromString("0.0.0.1.11.255");
    public static final ObisCode BillingResetLockoutTime = ObisCode.fromString("0.1.94.34.40.255");
    public static final ObisCode DaysSinceBillingReset = ObisCode.fromString("1.0.0.9.0.255");

    public static final ObisCode ErrorRegister = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode AlarmRegister = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode AlarmFilter = ObisCode.fromString("0.0.97.98.10.255");

    public static final ObisCode STANDARD_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DISCONNECT_CONTROL_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FIRMWARE_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode POWER_QUALITY_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode POWER_FAILURE_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode COMMUNICATION_FAILURE_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode PREPAYMENT_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.7.255");
    public static final ObisCode TARIFF_UPDATE_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.9.255");
    public static final ObisCode CLOCK_SYNC_EVENTLOG_OBISCODE = ObisCode.fromString("0.0.99.98.30.255");

    public static final ObisCode InstantaneousEnergyValuesObisCode = ObisCode.fromString("0.0.21.0.6.255");
    public static final ObisCode RefVoltagePQObisCode = ObisCode.fromString("1.0.0.6.4.255");
    public static final ObisCode NrOfVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.90.255");
    public static final ObisCode DurationVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.91.255");
    public static final ObisCode NrOfVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.92.255");
    public static final ObisCode DurationVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.93.255");
    public static final ObisCode ActiveQuadrantObisCode = ObisCode.fromString("1.1.94.34.100.255");
    public static final ObisCode ActiveQuadrantL1ObisCode = ObisCode.fromString("1.1.94.34.101.255");
    public static final ObisCode ActiveQuadrantL2ObisCode = ObisCode.fromString("1.1.94.34.102.255");
    public static final ObisCode ActiveQuadrantL3ObisCode = ObisCode.fromString("1.1.94.34.103.255");
    public static final ObisCode PhasePrecense = ObisCode.fromString("1.1.94.34.104.255");
    public static final ObisCode TransformerRatioCurrentNumObisCode = ObisCode.fromString("1.0.0.4.2.255");
    public static final ObisCode TransformerRatioVoltageNumObisCode = ObisCode.fromString("1.0.0.4.3.255");
    public static final ObisCode TransformerRatioCurrentDenObisCode = ObisCode.fromString("1.0.0.4.5.255");
    public static final ObisCode TransformerRatioVoltageDenObisCode = ObisCode.fromString("1.0.0.4.6.255");

    public static final ObisCode ActivityCalendarContract1ObisCode = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode ActiveSpecialDayContract1ObisCode = ObisCode.fromString("0.0.11.0.1.255");
    public static final ObisCode PassiveSpecialDayContract1ObisCode = ObisCode.fromString("0.0.11.0.0.255");
    public static final ObisCode CurrentActiveRateContract1ObisCode = ObisCode.fromString("0.0.96.14.1.255");
    public static final ObisCode ActiveCalendarNameObisCode = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode PassiveCalendarNameObisCode = ObisCode.fromString("0.0.13.0.2.255");

    public static final ObisCode LoadProfileStatus30Min = ObisCode.fromString("0.0.96.10.1.255");
    public static final ObisCode LoadProfileStatusP2 = ObisCode.fromString("0.0.96.10.2.255");
    public static final ObisCode SCRIPT_TABLE_OBIS = ObisCode.fromString("0.0.10.0.100.255");
    public static final ObisCode SCRIPT_TABLE_PASSIVE_OBIS = ObisCode.fromString("0.0.10.1.100.255");

    //Apollo5 only
    public static final ObisCode Apollo5LoadProfileStatusHourly = ObisCode.fromString("0.0.96.10.7.255");
    public static final ObisCode Apollo5LoadProfileStatusDaily = ObisCode.fromString("0.0.96.10.8.255");
    public static final ObisCode Apollo5LoadProfileStatusEncrypted = ObisCode.fromString("0.128.96.10.8.255");
    public static final ObisCode StandardEventLogObisCode = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode PowerQualityFinishedEventLogObisCode = ObisCode.fromString("0.0.99.98.9.255");
    public static final ObisCode PowerQualityNotFinishedEventLogObisCode = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode FraudDetectionEventLogObisCode = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DemandManagementEventLogObisCode = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode CommonEventLogObisCode = ObisCode.fromString("0.0.99.98.7.255");
    public static final ObisCode PowerContractEventLogObisCode = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode FirmwareEventLogObisCode = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode ObjectSynchronizationEventLogObisCode = ObisCode.fromString("0.0.99.98.8.255");
    public static final ObisCode DisconnectControlLogObisCode = ObisCode.fromString("0.0.99.98.2.255");

    /**
     * @return the {@link #ClockObisCode} for the {@link com.energyict.dlms.cosem.Clock}
     */
    public ObisCode getClockObisCode() {
        return ClockObisCode;
    }

    /**
     * @return the obisCode for the <i>default</i> {@link com.energyict.dlms.cosem.ProfileGeneric}
     */
    public ObisCode getDefaultLoadProfileObisCode() {
        return LoadProfileP1;
    }

    /**
     * @return the obisCode fro the <i>Daily</i> LoadProfile
     */
    public ObisCode getDailyLoadProfileObisCode() {
        return LoadProfileDaily;
    }

    /**
     * @return the {@link #ClockSynchronizationObisCode} for the ClockSynchronization
     */
    public ObisCode getClockSynchronization() {
        return ClockSynchronizationObisCode;
    }

    /**
     * @return the {@link #SerialNumberObisCode} for the SerialNumber
     */
    public ObisCode getSerialNumberObisCode() {
        return SerialNumberObisCode;
    }

    /**
     * We return the Association according to the Management Client (clientId = 1)
     *
     * @return the obisCode for the AssociationLn object
     */
    public ObisCode getAssociationLnObisCode() {
        return getAssociationLnObisCode(MultipleClientRelatedObisCodes.MANAGEMENT_CLIENT.getClientId());
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#frameCounterObisCode}
     */
    public ObisCode getFrameCounterObisCode(int clientId){
        return MultipleClientRelatedObisCodes.frameCounterForClient(clientId);
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#associationObisCode} for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    public ObisCode getAssociationLnObisCode(int clientId) {
        return MultipleClientRelatedObisCodes.associationLNForClient(clientId);
    }

    /**
     * @return the {@link #FirmwareVersionObisCode} of this Firmware Version
     */
    public ObisCode getFirmwareVersion() {
        return FirmwareVersionObisCode;
    }

    /**
     * @return the {@link #InstantaneousEnergyValuesObisCode} of the Instantaneous Values Profile
     */
    public ObisCode getInstantaneousEnergyValueObisCode(){
        return InstantaneousEnergyValuesObisCode;
    }

    /**
     * @return the {@link #ActivityCalendarContract1ObisCode} for the ActivityCalendar
     */
    public ObisCode getActivityCalendarContract1ObisCode() {
        return ActivityCalendarContract1ObisCode;
    }

    /**
     * @return the {@link #ActiveSpecialDayContract1ObisCode} for the Active SpecialDay Table
     */
    public ObisCode getActiveSpecialDayContract1ObisCode() {
        return ActiveSpecialDayContract1ObisCode;
    }

    /**
     * @return the {@link #PassiveSpecialDayContract1ObisCode} for the Passive SpecialDay Table
     */
    public ObisCode getPassiveSpecialDayContract1ObisCode() {
        return PassiveSpecialDayContract1ObisCode;
    }

    public ObisCode getScriptTablePassiveObisCode() {
        return SCRIPT_TABLE_PASSIVE_OBIS;
    }

    public ObisCode getScriptTableObisCode() {
        return SCRIPT_TABLE_OBIS;
    }

    /**
     * @return the {@link #CurrentActiveRateContract1ObisCode} for the currently active Rate on the ActivityCalendar
     */
    public ObisCode getCurrentActiveRateContract1ObisCode() {
        return CurrentActiveRateContract1ObisCode;
    }

    public ObisCode getActiveCalendarNameObisCode(){
        return ActiveCalendarNameObisCode;
    }

    public ObisCode getActiveLongFirmwareIdentifierACOR() {
        return ActiveLongFirmwareIdentifierACOR;
    }

    public ObisCode getActiveLongFirmwareIdentifierMCOR() {
        return ActiveLongFirmwareIdentifierMCOR;
    }

    public ObisCode getStandardEventLogObisCode() {
        return StandardEventLogObisCode;
    }

    public ObisCode getPowerQualityFinishedEventLogObisCode() {
        return PowerQualityFinishedEventLogObisCode;
    }

    public ObisCode getPowerQualityNotFinishedEventLogObisCode() {
        return PowerQualityNotFinishedEventLogObisCode;
    }

    public ObisCode getFraudDetectionEventLogObisCode() {
        return FraudDetectionEventLogObisCode;
    }

    public ObisCode getDemandManagementEventLogObisCode() {
        return DemandManagementEventLogObisCode;
    }

    public ObisCode getCommonEventLogObisCode() {
        return CommonEventLogObisCode;
    }

    public ObisCode getPowerContractEventLogObisCode() {
        return PowerContractEventLogObisCode;
    }

    public ObisCode getFirmwareEventLogObisCode() {
        return FirmwareEventLogObisCode;
    }

    public ObisCode getObjectSynchronizationEventLogObisCode() {
        return ObjectSynchronizationEventLogObisCode;
    }

    public ObisCode getDisconnectControlLogObisCode() {
        return DisconnectControlLogObisCode;
    }
}
