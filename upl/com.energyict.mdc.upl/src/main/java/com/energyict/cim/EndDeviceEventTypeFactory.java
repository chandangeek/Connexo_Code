/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

/**
 * Factory containing static methods for the easy creation of different {@link EndDeviceEventType}s without knowing the exact code.
 *
 * @author sva
 * @since 4/06/13 - 15:42
 */
public class EndDeviceEventTypeFactory {

    public static EndDeviceEventType getOtherEventType() {
        return new EndDeviceEventType("0.0.0.0");
    }

    public static EndDeviceEventType getPowerDownEventType() {
        return new EndDeviceEventType("0.26.38.47");
    }

    public static EndDeviceEventType getPowerUpEventType() {
        return new EndDeviceEventType("0.26.38.49");
    }

    public static EndDeviceEventType getWatchdogResetEventType() {
        return new EndDeviceEventType("0.11.3.215");
    }

    public static EndDeviceEventType getSetClockBeforeEventType() {
        return new EndDeviceEventType("0.36.116.14");
    }

    public static EndDeviceEventType getSetClockAfterEventType() {
        return new EndDeviceEventType("0.36.116.24");
    }

    public static EndDeviceEventType getSetClockEventType() {
        return new EndDeviceEventType("0.36.116.13");
    }

    public static EndDeviceEventType getConfigurationChangeEventType() {
        return new EndDeviceEventType("0.7.31.13");
    }

    public static EndDeviceEventType getRamMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.85.79");
    }

    public static EndDeviceEventType getProgramFlowErrorEventType() {
        return new EndDeviceEventType("0.11.83.79");
    }

    public static EndDeviceEventType getRegisterOverflowEventType() {
        return new EndDeviceEventType("0.21.89.177");
    }

    public static EndDeviceEventType getFatalErrorEventType() {
        return new EndDeviceEventType("0.0.43.79");
    }

    public static EndDeviceEventType getClearDataEventType() {
        return new EndDeviceEventType("0.18.31.28");
    }

    public static EndDeviceEventType getHardwareErrorEventType() {
        return new EndDeviceEventType("0.0.0.79");
    }

    public static EndDeviceEventType getMeterAlarmEventType() {
        return new EndDeviceEventType("0.11.46.79");
    }

    public static EndDeviceEventType getRomMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.92.79");
    }

    public static EndDeviceEventType getMaximumDemandResetEventType() {
        return new EndDeviceEventType("0.8.87.215");
    }

    public static EndDeviceEventType getBillingActionEventType() {
        return new EndDeviceEventType("0.20.43.44");
    }

    public static EndDeviceEventType getApplicationAlertStartEventType() {
        return new EndDeviceEventType("0.11.43.242");
    }

    public static EndDeviceEventType getApplicationAlertStopEventType() {
        return new EndDeviceEventType("0.11.43.243");
    }

    public static EndDeviceEventType getPhaseFailureEventType() {
        return new EndDeviceEventType("1.26.25.85");
    }

    public static EndDeviceEventType getVoltageSagEventType() {
        return new EndDeviceEventType("1.26.79.223");
    }

    public static EndDeviceEventType getVoltageSwellEventType() {
        return new EndDeviceEventType("1.26.79.248");
    }

    public static EndDeviceEventType getTamperEventType() {
        return new EndDeviceEventType("0.12.43.257");
    }

    public static EndDeviceEventType getTamperClearedEventType() {
        return new EndDeviceEventType("0.12.43.291");
    }

    public static EndDeviceEventType getCoverOpenedEventType() {
        return new EndDeviceEventType("0.12.29.39");
    }

    public static EndDeviceEventType getTerminalOpenedEventType() {
        return new EndDeviceEventType("0.12.141.39");
    }

    public static EndDeviceEventType getReverseRunEventType() {
        return new EndDeviceEventType("0.12.48.219");
    }

    public static EndDeviceEventType getLoadProfileClearedEventType() {
        return new EndDeviceEventType("0.16.87.28");
    }

    public static EndDeviceEventType getEventLogClearedEventType() {
        return new EndDeviceEventType("0.17.44.28");
    }

    public static EndDeviceEventType getDaylightSavingTimeEnabledOrDisabledEventType() {
        return new EndDeviceEventType("0.36.56.24");
    }

    public static EndDeviceEventType getClockInvalidEventType() {
        return new EndDeviceEventType("0.36.43.35");
    }

    public static EndDeviceEventType getReplaceBatteryEventType() {
        return new EndDeviceEventType("0.2.0.150");
    }

    public static EndDeviceEventType getBatteryVoltageLowEventType() {
        return new EndDeviceEventType("0.2.22.150");
    }

    public static EndDeviceEventType getTouActivatedEventType() {
        return new EndDeviceEventType("0.20.121.4");
    }

    public static EndDeviceEventType getErrorRegisterClearedEventType() {
        return new EndDeviceEventType("0.17.89.28");
    }

    public static EndDeviceEventType getAlarmRegisterClearedEventType() {
        return new EndDeviceEventType("0.17.89.28");
    }

    public static EndDeviceEventType getProgramMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.83.79");
    }

    public static EndDeviceEventType getNvMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.72.79");
    }

    public static EndDeviceEventType getWatchdogErrorEventType() {
        return new EndDeviceEventType("0.11.3.79");
    }

    public static EndDeviceEventType getMeasurementSystemErrorEventType() {
        return new EndDeviceEventType("0.21.67.79");
    }

    public static EndDeviceEventType getFirmwareReadyForActivationEventType() {
        return new EndDeviceEventType("0.11.31.25");
    }

    public static EndDeviceEventType getFirmwareActivatedEventType() {
        return new EndDeviceEventType("0.11.31.4");
    }

    public static EndDeviceEventType getTerminalCoverClosedEventType() {
        return new EndDeviceEventType("0.12.141.16");
    }

    public static EndDeviceEventType getStrongDCFieldDetectedEventType() {
        return new EndDeviceEventType("0.12.66.242");
    }

    public static EndDeviceEventType getNoStrongDCFieldAnymoreEventType() {
        return new EndDeviceEventType("0.12.66.243");
    }

    public static EndDeviceEventType getMeterCoverClosedEventType() {
        return new EndDeviceEventType("0.12.29.16");
    }

    public static EndDeviceEventType getNTimesWrongPasswordEventType() {
        return new EndDeviceEventType("0.12.24.7");
    }

    public static EndDeviceEventType getManualDisconnectionEventType() {
        return new EndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getManualConnectionEventType() {
        return new EndDeviceEventType("0.31.0.42");
    }

    public static EndDeviceEventType getRemoteDisconnectionEventType() {
        return new EndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getRemoteConnectionEventType() {
        return new EndDeviceEventType("0.31.0.42");
    }

    public static EndDeviceEventType getLocalDisconnectionEventType() {
        return new EndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getLimiterThresholdExceededEventType() {
        return new EndDeviceEventType("0.15.261.139");
    }

    public static EndDeviceEventType getLimiterThresholdOkEventType() {
        return new EndDeviceEventType("0.15.261.216");
    }

    public static EndDeviceEventType getLimiterThresholdChangedEventType() {
        return new EndDeviceEventType("0.15.261.24");
    }

    public static EndDeviceEventType getCommunicationErrorMbusEventType() {
        return new EndDeviceEventType("0.1.147.79");
    }

    public static EndDeviceEventType getCommunicationOkMbusEventType() {
        return new EndDeviceEventType("0.1.147.216");
    }

    public static EndDeviceEventType getReplaceBatteryMbusEventType() {
        return new EndDeviceEventType("0.2.147.150");
    }

    public static EndDeviceEventType getFraudAttemptMbusEventType() {
        return new EndDeviceEventType("0.12.147.7");
    }

    public static EndDeviceEventType getClockAdjustedMbusEventType() {
        return new EndDeviceEventType("0.36.147.24");
    }

    public static EndDeviceEventType getManualDisconnectionMbusEventType() {
        return new EndDeviceEventType("0.31.147.68");
    }

    public static EndDeviceEventType getManualConnectionMbusEventType() {
        return new EndDeviceEventType("0.31.147.42");
    }

    public static EndDeviceEventType getRemoteDisconnectionMbusEventType() {
        return new EndDeviceEventType("0.31.147.68");
    }

    public static EndDeviceEventType getRemoteConnectionMbusEventType() {
        return new EndDeviceEventType("0.31.147.42");
    }

    public static EndDeviceEventType getValveAlarmMbusEventType() {
        return new EndDeviceEventType("0.31.147.79");
    }

    // Util class, so made constructor private
    private EndDeviceEventTypeFactory() {
    }

    public static EndDeviceEventType getClearedEventType() {
        return null;// TODO
    }

    public static EndDeviceEventType getPowerManagementSwitchLowPowerEventType() {
        return new EndDeviceEventType("10.26.80.57");
    }

    public static EndDeviceEventType getPowerManagementSwitchFullPowerEventType() {
        return new EndDeviceEventType("10.26.80.32");
    }

    public static EndDeviceEventType getPowerManagementSwitchReducedPowerEventType() {
        return new EndDeviceEventType("10.26.80.296");
    }

    public static EndDeviceEventType getPowerManagementMainsLostEventType() {
        return new EndDeviceEventType("10.26.80.68");
    }

    public static EndDeviceEventType getPowerManagementMainsRecoveredEventType() {
        return new EndDeviceEventType("10.26.80.42");
    }

    public static EndDeviceEventType getPowerManagementLastGaspEventType() {
        return new EndDeviceEventType("10.26.80.59");
    }

    public static EndDeviceEventType getPowerManagementBatteryChargeStartEventType() {
        return new EndDeviceEventType("10.2.22.54");
    }

    public static EndDeviceEventType getPowerManagementBatteryChargeStopEventType() {
        return new EndDeviceEventType("10.2.22.55");
    }

    public static EndDeviceEventType getIdisMeterDiscoveryEventType() {
        return new EndDeviceEventType("0.10.5.54");
    }

    public static EndDeviceEventType getIdisMeterAcceptedEventType() {
        return new EndDeviceEventType("0.10.74.17");
    }

    public static EndDeviceEventType getIdisMeterRejectedEventType() {
        return new EndDeviceEventType("0.10.74.85");
    }

    public static EndDeviceEventType getIdisMeterAlarmEventType() {
        return new EndDeviceEventType("0.17.285.83");
    }

    public static EndDeviceEventType getIdisAlarmConditionEventType() {
        return new EndDeviceEventType("0.17.285.79");
    }

    public static EndDeviceEventType getIdisMultiMasterEventType() {
        return null;//new EndDeviceEventType(""); // TODO
    }

    public static EndDeviceEventType getIdisPlcEquipmentInStateNewEventType() {
        return new EndDeviceEventType("0.39.17.215");
    }

    public static EndDeviceEventType getIdisExtendedAlarmStatusEventType() {
        return new EndDeviceEventType("0.17.285."); // TODO
    }

    public static EndDeviceEventType getIdisMeterDeletedEventType() {
        return new EndDeviceEventType("0.10.74.68");
    }

    public static EndDeviceEventType getIdisStackEventEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcPrimeRestartedEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcPrimeStackEventEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcPrimeRegisterNodeEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcPrimeUnregisterNodeEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcG3RestartedEventType() {
        return new EndDeviceEventType("0.23.43.53");
    }

    public static EndDeviceEventType getPlcG3StackEventEventType() {
        return new EndDeviceEventType("0."); // TODO
    }

    public static EndDeviceEventType getPlcG3RegisterNodeEventType() {
        return new EndDeviceEventType("0.10.5.42");
    }

    public static EndDeviceEventType getPlcG3UnregisterNodeEventType() {
        return new EndDeviceEventType("0.10.5.68");
    }

    public static EndDeviceEventType getPlcG3EventReceivedEventType() {
        return new EndDeviceEventType("0.1.43.3");
    }

    public static EndDeviceEventType getPlcG3JoinRequestNodeEventType() {
        return new EndDeviceEventType("0.10.5.7");
    }

    public static EndDeviceEventType getPlcG3UppermacStoppedEventType() {
        return new EndDeviceEventType("0.1.60.243");
    }

    public static EndDeviceEventType getPlcG3UppermacStartedEventType() {
        return new EndDeviceEventType("0.1.60.242");
    }

    public static EndDeviceEventType getPlcG3JoinFailedEventType() {
        return new EndDeviceEventType("0.23.74.85");
    }

    public static EndDeviceEventType getPlcG3AuthFailedEventType() {
        return new EndDeviceEventType("0.12.65.85");
    }

    public static EndDeviceEventType getDlmsServerSessionAcceptedEventType() {
        return new EndDeviceEventType("0.1.129.42");
    }

    public static EndDeviceEventType getDlmsServerSessionFinishedEventType() {
        return new EndDeviceEventType("0.1.129.68");
    }

    public static EndDeviceEventType getDlmsOtherEventType() {
        return new EndDeviceEventType("0.1.0.0"); // TODO
    }

    public static EndDeviceEventType getDlmsUpstreamTestEventType() {
        return new EndDeviceEventType("0.1.111.60");
    }

    public static EndDeviceEventType getModemWdgPppdResetEventType() {
        return new EndDeviceEventType("0.19.122.214");
    }

    public static EndDeviceEventType getModemWdgHwResetEventType() {
        return new EndDeviceEventType("0.19.?.214"); // TODO
    }

    public static EndDeviceEventType getModemWdgRebootRequestedEventType() {
        return new EndDeviceEventType("0.19.?.?"); // TODO
    }
}