/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

/**
 * Factory containing static methods for the easy creation of different {@link EndDeviceEventType}s without knowing the exact code.
 *
 * Codes for EndDeviceEventTypes are categorized in a manner that divides the enumerated code into 4 parts:
 * EndDeviceEventType:= <EndDeviceType>.<EndDeviceDomain>.<EndDeviceSubdomain>.<EndDeviceEventOrAction>
 *          where
                    <EndDeviceType> = a numeric value from the EndDeviceType enumeration (see EndDeviceType section)
                    <EndDeviceDomain> = a numeric value from the EndDeviceDomain enumeration (see  EndDeviceDomain section)
                    <EndDeviceSubdomain> = a numeric value from the EndDeviceSubdomain enumeration (see EndDeviceSubdomain section)
                    <EndDeviceEventOrAction> = a numeric value from the EndDeviceEventOrAction enumeration (see EndDeviceEventOrAction section)
 *
 */
public class EndDeviceEventTypeFactory {

    public static EndDeviceEventType getOtherEventType() {
        return new EndDeviceEventType("0.0.0.0");
    }

    public static EndDeviceEventType getPowerDownEventType() {
        return new EndDeviceEventType("0.26.38.85");
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
        return new EndDeviceEventType("0.7.31.24");
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
        return new EndDeviceEventType("0.8.0.215");
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
        return new EndDeviceEventType("0.12.141.212");
    }

    public static EndDeviceEventType getTerminalOpenedEventType() {
        return new EndDeviceEventType("0.12.29.212");
    }

    public static EndDeviceEventType getReverseRunEventType() {
        return new EndDeviceEventType("0.12.6.219");
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
        return new EndDeviceEventType("0.36.114.35");
    }

    public static EndDeviceEventType getReplaceBatteryEventType() {
        return new EndDeviceEventType("0.2.0.85");
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
        return new EndDeviceEventType("0.17.285.28");
    }

    public static EndDeviceEventType getProgramMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.83.79");
    }

    public static EndDeviceEventType getNvMemoryErrorEventType() {
        return new EndDeviceEventType("0.18.72.79");
    }

    public static EndDeviceEventType getWatchdogErrorEventType() {
        return new EndDeviceEventType("0.37.3.79");
    }

    public static EndDeviceEventType getMeasurementSystemErrorEventType() {
        return new EndDeviceEventType("0.21.67.79");
    }

    public static EndDeviceEventType getFirmwareReadyForActivationEventType() {
        return new EndDeviceEventType("0.11.283.280");
    }

    public static EndDeviceEventType getFirmwareActivatedEventType() {
        return new EndDeviceEventType("0.11.283.4");
    }

    public static EndDeviceEventType getTerminalCoverClosedEventType() {
        return new EndDeviceEventType("0.12.29.16");
    }

    public static EndDeviceEventType getStrongDCFieldDetectedEventType() {
        return new EndDeviceEventType("0.12.66.242");
    }

    public static EndDeviceEventType getNoStrongDCFieldAnymoreEventType() {
        return new EndDeviceEventType("0.12.66.243");
    }

    public static EndDeviceEventType getMeterCoverClosedEventType() {
        return new EndDeviceEventType("0.12.141.16");
    }

    public static EndDeviceEventType getNTimesWrongPasswordEventType() {
        return new EndDeviceEventType("0.12.24.85");
    }

    public static EndDeviceEventType getManualDisconnectionEventType() {
        return new EndDeviceEventType("0.15.26.68");
    }

    public static EndDeviceEventType getManualConnectionEventType() {
        return new EndDeviceEventType("0.15.26.42");
    }

    public static EndDeviceEventType getRemoteDisconnectionEventType() {
        return new EndDeviceEventType("0.31.26.68");
    }

    public static EndDeviceEventType getRemoteConnectionEventType() {
        return new EndDeviceEventType("0.31.26.42");
    }

    public static EndDeviceEventType getLocalDisconnectionEventType() {
        return new EndDeviceEventType("0.15.26.68");
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
        return new EndDeviceEventType("0.17.44.28");
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
        return new EndDeviceEventType("0.0.0.0");
    }

    public static EndDeviceEventType getIdisPlcEquipmentInStateNewEventType() {
        return new EndDeviceEventType("0.39.17.215");
    }

    public static EndDeviceEventType getIdisExtendedAlarmStatusEventType() {
        return new EndDeviceEventType("0.17.285.3");
    }

    public static EndDeviceEventType getIdisMeterDeletedEventType() {
        return new EndDeviceEventType("0.10.74.68");
    }

    public static EndDeviceEventType getIdisStackEventEventType() {
        return new EndDeviceEventType("0.17.43.0");
    }

    public static EndDeviceEventType getPlcPrimeRestartedEventType() {
        return new EndDeviceEventType("0.17.43.53");
    }

    public static EndDeviceEventType getPlcPrimeStackEventEventType() {
        return new EndDeviceEventType("0.17.43.0");
    }

    public static EndDeviceEventType getPlcPrimeRegisterNodeEventType() {
        return new EndDeviceEventType("0.10.43.42");
    }

    public static EndDeviceEventType getPlcPrimeUnregisterNodeEventType() {
        return new EndDeviceEventType("0.10.43.68");
    }

    public static EndDeviceEventType getPlcG3RestartedEventType() {
        return new EndDeviceEventType("0.23.43.53");
    }

    public static EndDeviceEventType getPlcG3StackEventEventType() {
        return new EndDeviceEventType("0.0.43.0");
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
        return new EndDeviceEventType("0.1.0.0");
    }

    public static EndDeviceEventType getDlmsUpstreamTestEventType() {
        return new EndDeviceEventType("0.1.111.60");
    }

    public static EndDeviceEventType getModemWdgPppdResetEventType() {
        return new EndDeviceEventType("0.19.122.214");
    }

    public static EndDeviceEventType getModemWdgHwResetEventType() {
        return new EndDeviceEventType("0.19.3.214");
    }

    public static EndDeviceEventType getModemWdgRebootRequestedEventType() {
        return new EndDeviceEventType("0.19.3.214");
    }

    public static EndDeviceEventType getModemConnectedEventType() {
        return new EndDeviceEventType("0.19.0.42");
    }

    public static EndDeviceEventType getModemDisconnectedEventType() {
        return new EndDeviceEventType("0.19.0.68");
    }

    public static EndDeviceEventType getModemWakeUpEventType() {
        return new EndDeviceEventType("0.19.0.54");
    }

    public static EndDeviceEventType getProtocolPreliminaryTaskCompletedEventType() {
        return new EndDeviceEventType("0.1.0.58");
    }

    public static EndDeviceEventType getProtocolPreliminaryTaskFailedEventType() {
        return new EndDeviceEventType("0.1.0.85");
    }

    public static EndDeviceEventType getProtocolConsecutiveFailureEventType() {
        return new EndDeviceEventType("0.1.0.85");
    }

    public static EndDeviceEventType getFirmwareUpgradeEventType() {
        return new EndDeviceEventType("10.11.0.52");
    }

    public static EndDeviceEventType getFirmwareModifiedEventType() {
        return new EndDeviceEventType("10.11.0.24");
    }

    public static EndDeviceEventType getCpuOverloadEventType() {
        return new EndDeviceEventType("10.0.82.177");
    }

    public static EndDeviceEventType getRamTooHighEventType() {
        return new EndDeviceEventType("10.18.85.177");
    }

    public static EndDeviceEventType getDiskUsageTooHighEventType() {
        return new EndDeviceEventType("10.0.109.177");
    }

    public static EndDeviceEventType getPaceExceptionEventType() {
        return new EndDeviceEventType("10.0.202.85");
    }

    public static EndDeviceEventType getSshLoginEventType() {
        return new EndDeviceEventType("10.12.211.2");
    }

    public static EndDeviceEventType getFactoryResetEventType() {
        return new EndDeviceEventType("10.0.88.214");
    }

    public static EndDeviceEventType getWebportalLoginEventType() {
        return new EndDeviceEventType("10.14.65.2");
    }

    public static EndDeviceEventType getWebportalActionEventType() {
        return new EndDeviceEventType("10.14.1.30");
    }

    public static EndDeviceEventType getWebportalFailedLoginEventType() {
        return new EndDeviceEventType("10.14.65.85");
    }

    public static EndDeviceEventType getWebportalLockedUserEventType() {
        return new EndDeviceEventType("10.14.1.227");
    }

    public static EndDeviceEventType getMeterMulticastUpgradeStartEventType() {
        return new EndDeviceEventType("3.11.83.54");
    }

    public static EndDeviceEventType getMeterMulticastUpgradeCompletedEventType() {
        return new EndDeviceEventType("3.11.83.58");
    }

    public static EndDeviceEventType getMeterMulticastUpgradeFailedEventType() {
        return new EndDeviceEventType("3.11.83.85");
    }

    public static EndDeviceEventType getMeterMulticastUpgradeInfoEventType() {
        return new EndDeviceEventType("3.11.83.0");
    }

    public static EndDeviceEventType getGeneralSecurityErrorEventType() {
        return new EndDeviceEventType("10.12.0.79");
    }

    public static EndDeviceEventType getWrapKeyErrorEventType() {
        return new EndDeviceEventType("10.12.32.79");
    }

    public static EndDeviceEventType getDlmsAuthenticationLevelUpdatedEventType() {
        return new EndDeviceEventType("0.12.1.24");
    }

    public static EndDeviceEventType getDlmsSecurityPolicyUpdatedEventType() {
        return new EndDeviceEventType("0.12.32.24");
    }

    public static EndDeviceEventType getDlmsSecuritySuiteUpdatedEventType() {
        return new EndDeviceEventType("0.12.32.24");
    }

    public static EndDeviceEventType getDlmsKeysUpdatedEventType() {
        return new EndDeviceEventType("0.12.32.24");
    }

    public static EndDeviceEventType getDlmsAccessViolationEventType() {
        return new EndDeviceEventType("0.12.1.38");
    }

    public static EndDeviceEventType getDlmsAuthenticationFailureEventType() {
        return new EndDeviceEventType("0.12.1.85");
    }

    public static EndDeviceEventType getDlmsCipheringErrorEventType() {
        return new EndDeviceEventType("0.12.16.79");
    }

    public static EndDeviceEventType getUnknownRegisterEventType() {
        return new EndDeviceEventType("0.0.89.61");
    }

    public static EndDeviceEventType getPlcG3BlacklistEventType() {
        return new EndDeviceEventType("10.23.68.161");
    }

    public static EndDeviceEventType getPlcG3NodeLinkLostEventType() {
        return new EndDeviceEventType("10.23.68.51");
    }

    public static EndDeviceEventType getPlcG3NodeLinkRecoveredEventType() {
        return new EndDeviceEventType("10.23.68.49");
    }

    public static EndDeviceEventType getPlcG3PanIdEventType() {
        return new EndDeviceEventType("10.23.68.33");
    }

    public static EndDeviceEventType getPlcG3TopologyUpdateEventType() {
        return new EndDeviceEventType("10.23.68.24");
    }

    public static EndDeviceEventType getModemNewSimEventType() {
        return new EndDeviceEventType("0.19.146.52");
    }

    public static EndDeviceEventType getModemNewEquipmentEventType() {
        return new EndDeviceEventType("0.19.146.242");
    }

    public static EndDeviceEventType getCheckDataConcentratorConfigEventType() {
        return new EndDeviceEventType("10.7.31.3");
    }

    public static EndDeviceEventType getLinkUpEventType() {
        return new EndDeviceEventType("10.23.15.42");
    }

    public static EndDeviceEventType getLinkDownEventType() {
        return new EndDeviceEventType("10.23.15.68");
    }

    public static EndDeviceEventType getUsbAddEventType() {
        return new EndDeviceEventType("10.7.60.83");
    }

    public static EndDeviceEventType getUsbRemoveEventType() {
        return new EndDeviceEventType("10.7.60.212");
    }

    public static EndDeviceEventType getFileTransferCompletedEventType() {
        return new EndDeviceEventType("10.7.31.58");
    }

    public static EndDeviceEventType getFileTransferFailedEventType() {
        return new EndDeviceEventType("10.7.31.85");
    }

    public static EndDeviceEventType getScriptExecutionStartedEventType() {
        return new EndDeviceEventType("10.7.83.242");
    }

    public static EndDeviceEventType getScriptExecutionCompletedEventType() {
        return new EndDeviceEventType("10.7.83.58");
    }

    public static EndDeviceEventType getScriptExecutionFailedEventType() {
        return new EndDeviceEventType("10.7.83.85");
    }

    public static EndDeviceEventType getScriptExecutionScheduledEventType() {
        return new EndDeviceEventType("10.7.83.225");
    }

    public static EndDeviceEventType getScriptExecutionDescheduledEventType() {
        return new EndDeviceEventType("10.7.83.55");
    }

    public static EndDeviceEventType getWebportalCsrfAttackEventType() {
        return new EndDeviceEventType("10.12.1.257");
    }

    public static EndDeviceEventType getSnmpOtherEventType() {
        return new EndDeviceEventType("10.17.300.0");
    }

    public static EndDeviceEventType getSnmpInfoEventType() {
        return new EndDeviceEventType("10.17.300.0");
    }

    public static EndDeviceEventType getSnmpUnsupportedVersionEventType() {
        return new EndDeviceEventType("10.14.300.40");
    }

    public static EndDeviceEventType getSnmpUnsupportedSecModelEventType() {
        return new EndDeviceEventType("10.14.300.85");
    }

    public static EndDeviceEventType getSnmpInvalidUserNameEventType() {
        return new EndDeviceEventType("10.14.300.257");
    }

    public static EndDeviceEventType getSnmpInvalidEngineIdEventType() {
        return new EndDeviceEventType("10.14.300.61");
    }

    public static EndDeviceEventType getSnmpAuthenticationFailureEventType() {
        return new EndDeviceEventType("10.14.300.85");
    }

    public static EndDeviceEventType getSnmpKeysUpdatedEventType() {
        return new EndDeviceEventType("10.14.300.24");
    }

    public static EndDeviceEventType getCrlUpdatedEventType() {
        return new EndDeviceEventType("0.12.21.24");
    }

    public static EndDeviceEventType getCrlUpdateRejectedEventType() {
        return new EndDeviceEventType("0.12.21.85");
    }

    public static EndDeviceEventType getKeyUpdateRequestEventType() {
        return new EndDeviceEventType("0.12.32.243");
    }

    public static EndDeviceEventType getCrlRemovedEventType() {
        return new EndDeviceEventType("0.12.21.212");
    }

    public static EndDeviceEventType getDot1xSuccessEventType() {
        return new EndDeviceEventType("0.12.74.58");
    }

    public static EndDeviceEventType getDot1xFailureEventType() {
        return new EndDeviceEventType("0.12.74.85");
    }

    public static EndDeviceEventType getReplayAttackEventType() {
        return new EndDeviceEventType("0.12.74.35");
    }

    public static EndDeviceEventType getCertificateAddedEventType() {
        return new EndDeviceEventType("0.12.21.83");
    }

    public static EndDeviceEventType getCertificateRemovedEventType() {
        return new EndDeviceEventType("0.12.21.212");
    }

    public static EndDeviceEventType getCertificateExpiredEventType() {
        return new EndDeviceEventType("0.12.21.64");
    }

    public static EndDeviceEventType getPowerDownPowerLostEventType() {
        return new EndDeviceEventType("10.26.0.59");
    }

    public static EndDeviceEventType getPowerDownUserRequestEventType() {
        return new EndDeviceEventType("10.26.0.243");
    }

    public static EndDeviceEventType getPowerDownSoftwareFaultEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownHardwareFaultEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownNetworkInactivityEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownFirmwareUpgradeEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownFirmwareRollbackEventType() {
        return new EndDeviceEventType("10.26.47.68");
    }

    public static EndDeviceEventType getPowerDownDiskErrorEventType() {
        return new EndDeviceEventType("10.26.60.68");
    }

    public static EndDeviceEventType getPowerDownConfigurationErrorEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownFactoryResetEventType() {
        return new EndDeviceEventType("10.26.47.68");
    }

    public static EndDeviceEventType getPowerDownTamperingEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownTemperatureEventType() {
        return new EndDeviceEventType("10.26.0.68");
    }

    public static EndDeviceEventType getPowerDownSystemWatchdogEventType() {
        return new EndDeviceEventType("10.26.3.68");
    }

    public static EndDeviceEventType getPowerDownWwanModemWatchdogEventType() {
        return new EndDeviceEventType("10.26.3.68");
    }

    public static EndDeviceEventType getPowerDownSecureElementWatchdogEventType() {
        return new EndDeviceEventType("10.26.3.68");
    }

    public static EndDeviceEventType getPowerDownExternalWatchdogEventType() {
        return new EndDeviceEventType("10.26.3.68");
    }

    public static EndDeviceEventType getProtocolLogClearedEventType() {
        return new EndDeviceEventType("3.17.44.28");
    }

    public static EndDeviceEventType getMeterClockInvalidEventType() {
        return new EndDeviceEventType("3.36.117.35");
    }

    public static EndDeviceEventType getMetrologicalMaintenanceEventType() {
        return new EndDeviceEventType(EndDeviceType.NOT_APPLICABLE,EndDeviceDomain.METROLOGY,EndDeviceSubdomain.MAINTMODE,EndDeviceEventOrAction.ACTIVATED);
    }

    public static EndDeviceEventType getTechnicalMaintenanceEventType() {
        return new EndDeviceEventType(EndDeviceType.NOT_APPLICABLE,EndDeviceDomain.FIRMWARE,EndDeviceSubdomain.MAINTMODE,EndDeviceEventOrAction.ACTIVATED);
    }

    public static EndDeviceEventType getRetrieveEmeterReadingsElectricityEventType() {
        return new EndDeviceEventType(EndDeviceType.ELECTRICMETER,EndDeviceDomain.COMMUNICATION,EndDeviceSubdomain.READINGS,EndDeviceEventOrAction.READ);
    }

    public static EndDeviceEventType getRetrieveEmeterReadingsGasEventType() {
        return new EndDeviceEventType(EndDeviceType.GASMETER,EndDeviceDomain.COMMUNICATION,EndDeviceSubdomain.READINGS,EndDeviceEventOrAction.READ);
    }

    public static EndDeviceEventType getRetrieveEmeterIntervalElectricityEventType() {
        return new EndDeviceEventType(EndDeviceType.ELECTRICMETER,EndDeviceDomain.COMMUNICATION,EndDeviceSubdomain.INTERVAL ,EndDeviceEventOrAction.READ);
    }

    public static EndDeviceEventType getRetrieveEmeterIntervalGasEventType() {
        return new EndDeviceEventType(EndDeviceType.GASMETER,EndDeviceDomain.COMMUNICATION,EndDeviceSubdomain.INTERVAL ,EndDeviceEventOrAction.READ);
    }

    public static EndDeviceEventType getShortVoltageSagL1EventType() {
        return new EndDeviceEventType(EndDeviceType.ELECTRICMETER,EndDeviceDomain.POWER,EndDeviceSubdomain.PHASEAVOLTAGE ,EndDeviceEventOrAction.SAGSTOPPED);
    }

    public static EndDeviceEventType getShortVoltageSagL2EventType() {
        return new EndDeviceEventType(EndDeviceType.ELECTRICMETER,EndDeviceDomain.POWER,EndDeviceSubdomain.PHASEBVOLTAGE ,EndDeviceEventOrAction.SAGSTOPPED);
    }

    public static EndDeviceEventType getShortVoltageSagL3EventType() {
        return new EndDeviceEventType(EndDeviceType.ELECTRICMETER,EndDeviceDomain.POWER,EndDeviceSubdomain.PHASECVOLTAGE ,EndDeviceEventOrAction.SAGSTOPPED);
    }

    public static EndDeviceEventType getSagEventPhaseA() {
        return new EndDeviceEventType("3.26.131.223");
    }

    public static EndDeviceEventType getSagEventPhaseB() {
        return new EndDeviceEventType("3.26.132.223");
    }

    public static EndDeviceEventType getSagEventPhaseC() {
        return new EndDeviceEventType("3.26.133.223");
    }

    public static EndDeviceEventType getSagEventPhaseAB() {
        return new EndDeviceEventType("3.26.25.223");
    }

    public static EndDeviceEventType getSagEventPhaseABC() {
        return new EndDeviceEventType("3.26.28.223");
    }

    public static EndDeviceEventType getSagEventPhaseAC() {
        return new EndDeviceEventType("3.26.79.223");
    }

    public static EndDeviceEventType getSagEventPhaseBC() {
        return new EndDeviceEventType("3.26.80.223");
    }

    public static EndDeviceEventType getSagEventCleared() {
        return new EndDeviceEventType("3.17.43.28");
    }

    public static EndDeviceEventType getDeviceAccessedRead() {
        return new EndDeviceEventType("0.0.202.46");
    }

    public static EndDeviceEventType getDeviceAccessedWrite() {
        return new EndDeviceEventType("0.0.282.0");
    }

    public static EndDeviceEventType getProcedureInvoked() {
        return new EndDeviceEventType("0.0.0.44");
    }

    public static EndDeviceEventType getTableWritten() {
        return new EndDeviceEventType("0.21.110.24");
    }

    public static EndDeviceEventType getCommunicationTerminatedNormally() {
        return new EndDeviceEventType("0.1.0.59");
    }

    public static EndDeviceEventType getCommunicationTerminatedAbnormally() {
        return new EndDeviceEventType("0.1.0.85");
    }

    public static EndDeviceEventType getResetListPointers() {
        return new EndDeviceEventType("0.21.63.214");
    }

    public static EndDeviceEventType getUpdateListPointers() {
        return new EndDeviceEventType("0.21.63.24");
    }

    public static EndDeviceEventType getHistoryLogCleared() {
        return new EndDeviceEventType("0.17.53.28");
    }

    public static EndDeviceEventType getHistoryLogPointer() {
        return new EndDeviceEventType("0.17.53.24");
    }

    public static EndDeviceEventType getEventLogPointer() {
        return new EndDeviceEventType("0.17.44.24");
    }

    public static EndDeviceEventType getSelfRead() {
        return new EndDeviceEventType("0.21.231.58");
    }

    public static EndDeviceEventType getDaylightSavingTimeOn() {
        return new EndDeviceEventType("0.36.56.76");
    }

    public static EndDeviceEventType getDaylightSavingTimeOff() {
        return new EndDeviceEventType("0.36.56.66");
    }

    public static EndDeviceEventType getSeasonChange() {
        return new EndDeviceEventType("0.36.228.24");
    }

    public static EndDeviceEventType getRateChange() {
        return new EndDeviceEventType("0.7.86.24");
    }

    public static EndDeviceEventType getSpecialSchedule() {
        return new EndDeviceEventType("0.20.95.4");
    }

    public static EndDeviceEventType getTierSwitch() {
        return new EndDeviceEventType("0.20.113.24");
    }

    public static EndDeviceEventType getPendingTableActivation() {
        return new EndDeviceEventType("0.21.110.4");
    }

    public static EndDeviceEventType getPendingTableClear() {
        return new EndDeviceEventType("0.21.110.28");
    }

    public static EndDeviceEventType getMeteringModeStart() {
        return new EndDeviceEventType("0.7.12.242");
    }

    public static EndDeviceEventType getMeteringModeStop() {
        return new EndDeviceEventType("0.7.12.243");
    }

    public static EndDeviceEventType getTestModeStart() {
        return new EndDeviceEventType("0.7.19.242");
    }

    public static EndDeviceEventType getTestModeStop() {
        return new EndDeviceEventType("0.7.19.243");
    }

    public static EndDeviceEventType getMeterShopStart() {
        return new EndDeviceEventType("0.7.11.242");
    }

    public static EndDeviceEventType getMeterShopStop() {
        return new EndDeviceEventType("0.7.11.243");
    }

    public static EndDeviceEventType getConfigurationError() {
        return new EndDeviceEventType("0.7.0.79");
    }

    public static EndDeviceEventType getSelfCheckError() {
        return new EndDeviceEventType("0.11.100.79");
    }

    public static EndDeviceEventType getRAMFailure() {
        return new EndDeviceEventType("0.18.85.85");
    }

    public static EndDeviceEventType getROMFailure() {
        return new EndDeviceEventType("0.18.92.85");
    }

    public static EndDeviceEventType getNonVolatileMemoryFailure() {
        return new EndDeviceEventType("0.18.72.85");
    }

    public static EndDeviceEventType getClockError() {
        return new EndDeviceEventType("0.36.114.79");
    }

    public static EndDeviceEventType getLowLossPotential() {
        return new EndDeviceEventType("0.26.38.47");
    }

    public static EndDeviceEventType getDemandOverload() {
        return new EndDeviceEventType("0.8.261.139");
    }

    public static EndDeviceEventType getPowerFailure() {
        return new EndDeviceEventType("0.26.25.85");
    }

    public static EndDeviceEventType getReverseRotation() {
        return new EndDeviceEventType("0.26.78.219");
    }

    public static EndDeviceEventType getEnterExitTier() {
        return new EndDeviceEventType("0.0.113.0");
    }

    public static EndDeviceEventType getCoverTemper() {
        return new EndDeviceEventType("0.12.29.212");
    }

    public static EndDeviceEventType getExternalEvent() {
        return new EndDeviceEventType("0.39.55.242");
    }

    public static EndDeviceEventType getPhaseAOff() {
        return new EndDeviceEventType("0.26.126.85");
    }

    public static EndDeviceEventType getPhaseAOn() {
        return new EndDeviceEventType("0.26.126.216");
    }

    public static EndDeviceEventType getPhaseBOff() {
        return new EndDeviceEventType("0.26.134.85");
    }

    public static EndDeviceEventType getPhaseBOn() {
        return new EndDeviceEventType("0.26.134.216");
    }

    public static EndDeviceEventType getPhaseCOff() {
        return new EndDeviceEventType("0.26.135.85");
    }

    public static EndDeviceEventType getPhaseCOn() {
        return new EndDeviceEventType("0.26.135.216");
    }

    public static EndDeviceEventType getRWP() {
        return new EndDeviceEventType("0.26.0.46");
    }

    public static EndDeviceEventType getDeviceProgrammed() {
        return new EndDeviceEventType("0.7.83.24");
    }

    public static EndDeviceEventType getRemoteFlashFailure() {
        return new EndDeviceEventType("0.11.43.85");
    }

    public static EndDeviceEventType getServiceVoltageTest() {
        return new EndDeviceEventType("0.26.38.44");
    }

    public static EndDeviceEventType getLowCurrentTest() {
        return new EndDeviceEventType("0.26.6.44");
    }

    public static EndDeviceEventType getPowerFactor() {
        return new EndDeviceEventType("0.26.27.0");
    }

    public static EndDeviceEventType getSecondHarmonicCurrentTest() {
        return new EndDeviceEventType("0.0.6.44");
    }

    public static EndDeviceEventType getTotalHarmonicCurrent() {
        return new EndDeviceEventType("0.26.6.69");
    }

    public static EndDeviceEventType getTotalHarmonicVoltage() {
        return new EndDeviceEventType("0.26.38.69");
    }

    public static EndDeviceEventType getVoltageImbalance() {
        return new EndDeviceEventType("0.26.38.98");
    }

    public static EndDeviceEventType getCurrentImbalance() {
        return new EndDeviceEventType("0.26.6.98");
    }

    public static EndDeviceEventType getTotalDemandDistortion() {
        return new EndDeviceEventType("0.8.0.69");
    }

    public static EndDeviceEventType getHighVoltageTest() {
        return new EndDeviceEventType("3.26.79.248");
    }

    public static EndDeviceEventType getReversePowerTest() {
        return new EndDeviceEventType("3.26.27.44");
    }

    public static EndDeviceEventType getTouProgrammed() {
        return new EndDeviceEventType("0.20.121.58");
    }

    public static EndDeviceEventType getExternalAlert() {
        return new EndDeviceEventType("0.39.55.242");
    }

    public static EndDeviceEventType getFirmwareVerificationFail() {
        return new EndDeviceEventType("0.11.283.85");
    }

    public static EndDeviceEventType getUnexpectedConsumption() {
        return new EndDeviceEventType("0.21.67.35");
    }

    public static EndDeviceEventType getPhaseReversal() {
        return new EndDeviceEventType("0.26.78.219");
    }

    public static EndDeviceEventType getMissingNeutral() {
        return new EndDeviceEventType("0.26.38.285");
    }

    public static EndDeviceEventType getNTimeDecryptFail() {
        return new EndDeviceEventType("0.12.32.85");
    }

    public static EndDeviceEventType getDisconnectorReadyForReconn() {
        return new EndDeviceEventType("0.15.26.11");
    }

    public static EndDeviceEventType getDisconnectReconnectFail() {
        return new EndDeviceEventType("0.15.26.84");
    }

    public static EndDeviceEventType getLocalReconnection() {
        return new EndDeviceEventType("0.15.26.42");
    }

    public static EndDeviceEventType getSupervision1Exceeded() {
        return new EndDeviceEventType("0.15.287.93");
    }

    public static EndDeviceEventType getSupervision1Ok() {
        return new EndDeviceEventType("0.15.287.73");
    }

    public static EndDeviceEventType getSupervision2Exceeded() {
        return new EndDeviceEventType("0.15.288.93");
    }

    public static EndDeviceEventType getSupervision2Ok() {
        return new EndDeviceEventType("0.15.288.73");
    }

    public static EndDeviceEventType getSupervision3Exceeded() {
        return new EndDeviceEventType("0.15.289.93");
    }

    public static EndDeviceEventType getSupervision3Ok() {
        return new EndDeviceEventType("0.15.289.73");
    }

    public static EndDeviceEventType getPhaseAsymmetry() {
        return new EndDeviceEventType("0.26.25.98");
    }

    public static EndDeviceEventType getCommunicationTimeout() {
        return new EndDeviceEventType("0.1.125.85");
    }

    public static EndDeviceEventType getModemInitializationFail() {
        return new EndDeviceEventType("0.19.298.85");
    }

    public static EndDeviceEventType getSimCardFail() {
        return new EndDeviceEventType("0.19.17.85");
    }

    public static EndDeviceEventType getSimCardOk() {
        return new EndDeviceEventType("0.19.17.58");
    }

    public static EndDeviceEventType getGSM_GPRS_RegistrationFail() {
        return new EndDeviceEventType("0.19.5.85");
    }

    public static EndDeviceEventType getPDPContextEstablished() {
        return new EndDeviceEventType("0.19.90.29");
    }

    public static EndDeviceEventType getPDPContextDestroyed() {
        return new EndDeviceEventType("0.19.90.59");
    }

    public static EndDeviceEventType getPDPContextFail() {
        return new EndDeviceEventType("0.19.90.85");
    }

    public static EndDeviceEventType getModemSWReset() {
        return new EndDeviceEventType("0.19.47.214");
    }

    public static EndDeviceEventType getModemHWReset() {
        return new EndDeviceEventType("0.19.47.215");
    }

    public static EndDeviceEventType getGSMConnection() {
        return new EndDeviceEventType("0.19.129.29");
    }

    public static EndDeviceEventType getGSMHangUp() {
        return new EndDeviceEventType("0.19.129.68");
    }

    public static EndDeviceEventType getDiagnosticFailure() {
        return new EndDeviceEventType("0.19.111.85");
    }

    public static EndDeviceEventType getUserInitializationFail() {
        return new EndDeviceEventType("0.19.298.35");
    }

    public static EndDeviceEventType getAnswerNumberExceeded() {
        return new EndDeviceEventType("0.19.261.93");
    }

    public static EndDeviceEventType getLocalCommunicationAttempt() {
        return new EndDeviceEventType("0.1.65.2");
    }

    public static EndDeviceEventType getGlobalKey() {
        return new EndDeviceEventType("0.12.32.15");
    }

    public static EndDeviceEventType getUndervoltageL1() {
        return new EndDeviceEventType("0.26.131.223");
    }

    public static EndDeviceEventType getUndervoltageL2() {
        return new EndDeviceEventType("0.26.132.223");
    }

    public static EndDeviceEventType getUndervoltageL3() {
        return new EndDeviceEventType("0.26.133.223");
    }

    public static EndDeviceEventType getOvervoltageL1() {
        return new EndDeviceEventType("0.26.131.248");
    }

    public static EndDeviceEventType getOvervoltageL2() {
        return new EndDeviceEventType("0.26.132.248");
    }

    public static EndDeviceEventType getOvervoltageL3() {
        return new EndDeviceEventType("0.26.133.248");
    }

    public static EndDeviceEventType getMissingVoltageL1() {
        return new EndDeviceEventType("0.26.131.285");
    }

    public static EndDeviceEventType getMissingVoltageL2() {
        return new EndDeviceEventType("0.26.132.285");
    }

    public static EndDeviceEventType getMissingVoltageL3() {
        return new EndDeviceEventType("0.26.133.285");
    }

    public static EndDeviceEventType getNormalVoltageL1() {
        return new EndDeviceEventType("0.26.131.37");
    }

    public static EndDeviceEventType getNormalVoltageL2() {
        return new EndDeviceEventType("0.26.132.37");
    }

    public static EndDeviceEventType getNormalVoltageL3() {
        return new EndDeviceEventType("0.26.133.37");
    }

    public static EndDeviceEventType getBadVoltageL1() {
        return new EndDeviceEventType("0.26.131.40");
    }

    public static EndDeviceEventType getBadVoltageL2() {
        return new EndDeviceEventType("0.26.132.40");
    }

    public static EndDeviceEventType getBadVoltageL3() {
        return new EndDeviceEventType("0.26.133.40");
    }

    public static EndDeviceEventType getSignalQualityLow() {
        return new EndDeviceEventType("0.19.0.150");
    }

    public static EndDeviceEventType getMeterAlarmEnd() {
        return new EndDeviceEventType("0.11.46.279");
    }

    public static EndDeviceEventType getReversePower() {
        return new EndDeviceEventType("0.12.48.219");
    }

    public static EndDeviceEventType getInputEvent() {
        return new EndDeviceEventType("0.0.55.242");
    }

    public static EndDeviceEventType getChangeImpulse() {
        return new EndDeviceEventType("0.7.31.13");
    }

    public static EndDeviceEventType getPulsAStored() {
        return new EndDeviceEventType("0.20.31.58");
    }

    public static EndDeviceEventType getParameterRestored() {
        return new EndDeviceEventType("0.17.75.216");
    }

    public static EndDeviceEventType getParameterInitialized() {
        return new EndDeviceEventType("0.17.75.33");
    }

    public static EndDeviceEventType getStatusChanged() {
        return new EndDeviceEventType("0.17.17.24");
    }

    public static EndDeviceEventType getRelayConnected() {
        return new EndDeviceEventType("0.39.91.42");
    }

    public static EndDeviceEventType getRelayDisconnected() {
        return new EndDeviceEventType("0.39.91.68");
    }

    public static EndDeviceEventType getRelayConnectFailed() {
        return new EndDeviceEventType("0.39.91.67");
    }

    public static EndDeviceEventType getRelayDisconnectFailed() {
        return new EndDeviceEventType("0.39.91.84");
    }

    public static EndDeviceEventType getSessionStarted() {
        return new EndDeviceEventType("0.17.129.242");
    }

    public static EndDeviceEventType getConfigurationParameterChanged() {
        return new EndDeviceEventType("0.7.75.24");
    }

    public static EndDeviceEventType getSessionStopped() {
        return new EndDeviceEventType("0.17.129.243");
    }

    public static EndDeviceEventType getSessionTerminated() {
        return new EndDeviceEventType("0.17.129.59");
    }

    public static EndDeviceEventType getSessionExpired() {
        return new EndDeviceEventType("0.17.129.64");
    }

    public static EndDeviceEventType getSessionDisallowed() {
        return new EndDeviceEventType("0.17.129.161");
    }

    public static EndDeviceEventType getSessionSubstituted() {
        return new EndDeviceEventType("0.17.129.56");
    }

    public static EndDeviceEventType getSessionAborted() {
        return new EndDeviceEventType("0.17.129.1");
    }

    public static EndDeviceEventType getBatteryStatusEnabled() {
        return new EndDeviceEventType("0.2.17.76");
    }

    public static EndDeviceEventType getBatteryStatusDisabled() {
        return new EndDeviceEventType("0.2.17.66");
    }

    public static EndDeviceEventType getAssociatedDeviceInputError() {
        return new EndDeviceEventType("0.39.55.79");
    }

    public static EndDeviceEventType getModemSessionFailed() {
        return new EndDeviceEventType("0.19.129.85");
    }

    public static EndDeviceEventType getHighConsumptionOrProductionEnergy() {
        return new EndDeviceEventType("3.21.67.93");
    }

    public static EndDeviceEventType getIndexValuesDecreaseOrReset() {
        return new EndDeviceEventType("3.21.89.215");
    }

    public static EndDeviceEventType getMismatchBetweenTotalAndTariffRegisters() {
        return new EndDeviceEventType("3.20.140.159");
    }
}