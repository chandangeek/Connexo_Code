/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

import java.util.Objects;

/**
 * Enum containing the mapping between the meter protocol event code and the corresponding CIM {@link EndDeviceEventType}.
 *
 * @author sva
 * @since 3/06/13 - 15:00
 */
public enum EndDeviceEventTypeMapping {

    OTHER                   (0, EndDeviceEventTypeFactory.getOtherEventType()),
    POWERDOWN               (1, EndDeviceEventTypeFactory. getPowerDownEventType()),
    POWERUP                 (2, EndDeviceEventTypeFactory.getPowerUpEventType()),
    WATCHDOGRESET           (3, EndDeviceEventTypeFactory.getWatchdogResetEventType()),
    SETCLOCK_BEFORE         (4, EndDeviceEventTypeFactory.getSetClockBeforeEventType()),
    SETCLOCK_AFTER          (5, EndDeviceEventTypeFactory.getSetClockAfterEventType()),
    SETCLOCK                (6, EndDeviceEventTypeFactory.getSetClockEventType()),
    CONFIGURATIONCHANGE     (7, EndDeviceEventTypeFactory.getConfigurationChangeEventType()),
    RAM_MEMORY_ERROR        (8, EndDeviceEventTypeFactory.getRamMemoryErrorEventType()),
    PROGRAM_FLOW_ERROR      (9, EndDeviceEventTypeFactory.getProgramFlowErrorEventType()),
    REGISTER_OVERFLOW       (10, EndDeviceEventTypeFactory.getRegisterOverflowEventType()),
    FATAL_ERROR             (11, EndDeviceEventTypeFactory.getFatalErrorEventType()),
    CLEAR_DATA              (12, EndDeviceEventTypeFactory.getClearDataEventType()),
    HARDWARE_ERROR          (13, EndDeviceEventTypeFactory.getHardwareErrorEventType()),
    METER_ALARM             (14, EndDeviceEventTypeFactory.getMeterAlarmEventType()),
    ROM_MEMORY_ERROR        (15, EndDeviceEventTypeFactory.getRomMemoryErrorEventType()),
    MAXIMUM_DEMAND_RESET    (16, EndDeviceEventTypeFactory.getMaximumDemandResetEventType()),
    BILLING_ACTION          (17, EndDeviceEventTypeFactory.getBillingActionEventType()),
    APPLICATION_ALERT_START (18, EndDeviceEventTypeFactory.getApplicationAlertStartEventType()),
    APPLICATION_ALERT_STOP  (19, EndDeviceEventTypeFactory.getApplicationAlertStopEventType()),
    PHASE_FAILURE           (20, EndDeviceEventTypeFactory.getPhaseFailureEventType()),
    VOLTAGE_SAG             (21, EndDeviceEventTypeFactory.getVoltageSagEventType()),
    VOLTAGE_SWELL           (22, EndDeviceEventTypeFactory.getVoltageSwellEventType()),
    TAMPER                  (23, EndDeviceEventTypeFactory.getTamperEventType()),
    COVER_OPENED            (24, EndDeviceEventTypeFactory.getCoverOpenedEventType()),
    TERMINAL_OPENED         (25, EndDeviceEventTypeFactory.getTerminalOpenedEventType()),
    REVERSE_RUN             (26, EndDeviceEventTypeFactory.getReverseRunEventType()),
    LOADPROFILE_CLEARED     (27, EndDeviceEventTypeFactory.getLoadProfileClearedEventType()),
    EVENT_LOG_CLEARED       (28, EndDeviceEventTypeFactory.getEventLogClearedEventType()),
    DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED(29,EndDeviceEventTypeFactory.getDaylightSavingTimeEnabledOrDisabledEventType()),
    CLOCK_INVALID           (30, EndDeviceEventTypeFactory.getClockInvalidEventType()),
    REPLACE_BATTERY         (31, EndDeviceEventTypeFactory.getReplaceBatteryEventType()),
    BATTERY_VOLTAGE_LOW     (32, EndDeviceEventTypeFactory.getBatteryVoltageLowEventType()),
    TOU_ACTIVATED           (33, EndDeviceEventTypeFactory.getTouActivatedEventType()),
    ERROR_REGISTER_CLEARED  (34, EndDeviceEventTypeFactory.getErrorRegisterClearedEventType()),
    ALARM_REGISTER_CLEARED  (35, EndDeviceEventTypeFactory.getAlarmRegisterClearedEventType()),
    PROGRAM_MEMORY_ERROR    (36, EndDeviceEventTypeFactory.getProgramMemoryErrorEventType()),
    NV_MEMORY_ERROR         (37, EndDeviceEventTypeFactory.getNvMemoryErrorEventType()),
    WATCHDOG_ERROR          (38, EndDeviceEventTypeFactory.getWatchdogErrorEventType()),
    MEASUREMENT_SYSTEM_ERROR(39, EndDeviceEventTypeFactory.getMeasurementSystemErrorEventType()),
    FIRMWARE_READY_FOR_ACTIVATION(40, EndDeviceEventTypeFactory.getFirmwareReadyForActivationEventType()),
    FIRMWARE_ACTIVATED      (41, EndDeviceEventTypeFactory.getFirmwareActivatedEventType()),
    TERMINAL_COVER_CLOSED   (42, EndDeviceEventTypeFactory.getTerminalCoverClosedEventType()),
    STRONG_DC_FIELD_DETECTED(43, EndDeviceEventTypeFactory.getStrongDCFieldDetectedEventType()),
    NO_STRONG_DC_FIELD_ANYMORE(44, EndDeviceEventTypeFactory.getNoStrongDCFieldAnymoreEventType()),
    METER_COVER_CLOSED      (45, EndDeviceEventTypeFactory.getMeterCoverClosedEventType()),
    N_TIMES_WRONG_PASSWORD  (46, EndDeviceEventTypeFactory.getNTimesWrongPasswordEventType()),
    MANUAL_DISCONNECTION    (47, EndDeviceEventTypeFactory.getManualDisconnectionEventType()),
    MANUAL_CONNECTION       (48, EndDeviceEventTypeFactory.getManualConnectionEventType()),
    REMOTE_DISCONNECTION    (49, EndDeviceEventTypeFactory.getRemoteDisconnectionEventType()),
    REMOTE_CONNECTION       (50, EndDeviceEventTypeFactory.getRemoteConnectionEventType()),
    LOCAL_DISCONNECTION     (51, EndDeviceEventTypeFactory.getLocalDisconnectionEventType()),
    LIMITER_THRESHOLD_EXCEEDED(52, EndDeviceEventTypeFactory.getLimiterThresholdExceededEventType()),
    LIMITER_THRESHOLD_OK    (53, EndDeviceEventTypeFactory.getLimiterThresholdOkEventType()),
    LIMITER_THRESHOLD_CHANGED(54, EndDeviceEventTypeFactory.getLimiterThresholdChangedEventType()),
    COMMUNICATION_ERROR_MBUS(55, EndDeviceEventTypeFactory.getCommunicationErrorMbusEventType()),
    COMMUNICATION_OK_MBUS   (56, EndDeviceEventTypeFactory.getCommunicationOkMbusEventType()),
    REPLACE_BATTERY_MBUS    (57, EndDeviceEventTypeFactory.getReplaceBatteryMbusEventType()),
    FRAUD_ATTEMPT_MBUS      (58, EndDeviceEventTypeFactory.getFraudAttemptMbusEventType()),
    CLOCK_ADJUSTED_MBUS     (59, EndDeviceEventTypeFactory.getClockAdjustedMbusEventType()),
    MANUAL_DISCONNECTION_MBUS(60, EndDeviceEventTypeFactory.getManualDisconnectionMbusEventType()),
    MANUAL_CONNECTION_MBUS  (61, EndDeviceEventTypeFactory.getManualConnectionMbusEventType()),
    REMOTE_DISCONNECTION_MBUS(62, EndDeviceEventTypeFactory.getRemoteDisconnectionMbusEventType()),
    REMOTE_CONNECTION_MBUS  (63, EndDeviceEventTypeFactory.getRemoteConnectionMbusEventType()),
    VALVE_ALARM_MBUS        (64, EndDeviceEventTypeFactory.getValveAlarmMbusEventType()),
    TAMPER_CLEARED          (65, EndDeviceEventTypeFactory.getTamperClearedEventType()),

    //DSMR - ESMR Communication events
    EVENT_METROLOGICAL_MAINTENANCE  (71, EndDeviceEventTypeFactory.getMetrologicalMaintenanceEventType()),
    EVENT_TECHNICAL_MAINTENANCE     (72, EndDeviceEventTypeFactory.getTechnicalMaintenanceEventType()),
    EVENT_RETRIEVE_METER_READINGS_E (73, EndDeviceEventTypeFactory.getRetrieveEmeterReadingsElectricityEventType()),
    EVENT_RETRIEVE_METER_READINGS_G (74, EndDeviceEventTypeFactory.getRetrieveEmeterReadingsGasEventType()),
    EVENT_RETRIEVE_INTERVAL_DATA_E  (75, EndDeviceEventTypeFactory.getRetrieveEmeterIntervalElectricityEventType()),
    EVENT_RETRIEVE_INTERVAL_DATA_G  (76, EndDeviceEventTypeFactory.getRetrieveEmeterIntervalGasEventType()),

    EVENT_EVENT_LOG_CLEARED         (255, EndDeviceEventTypeFactory.getClearedEventType()),

    CLEARED                                 (100001, EndDeviceEventTypeFactory.getClearedEventType()),
    POWER_MANAGEMENT_SWITCH_LOW_POWER       (100002, EndDeviceEventTypeFactory.getPowerManagementSwitchLowPowerEventType()),
    POWER_MANAGEMENT_SWITCH_FULL_POWER      (100003, EndDeviceEventTypeFactory.getPowerManagementSwitchFullPowerEventType()),
    POWER_MANAGEMENT_SWITCH_REDUCED_POWER   (100004, EndDeviceEventTypeFactory.getPowerManagementSwitchReducedPowerEventType()),
    POWER_MANAGEMENT_MAINS_LOST             (100005, EndDeviceEventTypeFactory.getPowerManagementMainsLostEventType()),
    POWER_MANAGEMENT_MAINS_RECOVERED        (100006, EndDeviceEventTypeFactory.getPowerManagementMainsRecoveredEventType()),
    POWER_MANAGEMENT_LAST_GASP              (100007, EndDeviceEventTypeFactory.getPowerManagementLastGaspEventType()),
    POWER_MANAGEMENT_BATTERY_CHARGE_START   (100008, EndDeviceEventTypeFactory.getPowerManagementBatteryChargeStartEventType()),
    POWER_MANAGEMENT_BATTERY_CHARGE_STOP    (100009, EndDeviceEventTypeFactory.getPowerManagementBatteryChargeStopEventType()),
    IDIS_METER_DISCOVERY                    (100010, EndDeviceEventTypeFactory.getIdisMeterDiscoveryEventType()),
    IDIS_METER_ACCEPTED                     (100011, EndDeviceEventTypeFactory.getIdisMeterAcceptedEventType()),
    IDIS_METER_REJECTED                     (100012, EndDeviceEventTypeFactory.getIdisMeterRejectedEventType()),
    IDIS_METER_ALARM                        (100013, EndDeviceEventTypeFactory.getIdisMeterAlarmEventType()),
    IDIS_ALARM_CONDITION                    (100014, EndDeviceEventTypeFactory.getIdisAlarmConditionEventType()),
    IDIS_MULTI_MASTER                       (100015, EndDeviceEventTypeFactory.getIdisMultiMasterEventType()),
    IDIS_PLC_EQUIPMENT_IN_STATE_NEW         (100016, EndDeviceEventTypeFactory.getIdisPlcEquipmentInStateNewEventType()),
    IDIS_EXTENDED_ALARM_STATUS              (100017, EndDeviceEventTypeFactory.getIdisExtendedAlarmStatusEventType()),
    IDIS_METER_DELETED                      (100018, EndDeviceEventTypeFactory.getIdisMeterDeletedEventType()),
    IDIS_STACK_EVENT                        (100019, EndDeviceEventTypeFactory.getIdisStackEventEventType()),
    PLC_PRIME_RESTARTED                     (100020, EndDeviceEventTypeFactory.getPlcPrimeRestartedEventType()),
    PLC_PRIME_STACK_EVENT                   (100021, EndDeviceEventTypeFactory.getPlcPrimeStackEventEventType()),
    PLC_PRIME_REGISTER_NODE                 (100022, EndDeviceEventTypeFactory.getPlcPrimeRegisterNodeEventType()),
    PLC_PRIME_UNREGISTER_NODE               (100023, EndDeviceEventTypeFactory.getPlcPrimeUnregisterNodeEventType()),
    PLC_G3_RESTARTED                        (100024, EndDeviceEventTypeFactory.getPlcG3RestartedEventType()),
    PLC_G3_STACK_EVENT                      (100025, EndDeviceEventTypeFactory.getPlcG3StackEventEventType()),
    PLC_G3_REGISTER_NODE                    (100026, EndDeviceEventTypeFactory.getPlcG3RegisterNodeEventType()),
    PLC_G3_UNREGISTER_NODE                  (100027, EndDeviceEventTypeFactory.getPlcG3UnregisterNodeEventType()),
    PLC_G3_EVENT_RECEIVED                   (100028, EndDeviceEventTypeFactory.getPlcG3EventReceivedEventType()),
    PLC_G3_JOIN_REQUEST_NODE                (100029, EndDeviceEventTypeFactory.getPlcG3JoinRequestNodeEventType()),
    PLC_G3_UPPERMAC_STOPPED                 (100030, EndDeviceEventTypeFactory.getPlcG3UppermacStoppedEventType()),
    PLC_G3_UPPERMAC_STARTED                 (100031, EndDeviceEventTypeFactory.getPlcG3UppermacStartedEventType()),
    PLC_G3_JOIN_FAILED                      (100032, EndDeviceEventTypeFactory.getPlcG3JoinFailedEventType()),
    PLC_G3_AUTH_FAILURE                     (100033, EndDeviceEventTypeFactory.getPlcG3AuthFailedEventType()),
    DLMS_SERVER_SESSION_ACCEPTED            (100034, EndDeviceEventTypeFactory.getDlmsServerSessionAcceptedEventType()),
    DLMS_SERVER_SESSION_FINISHED            (100035, EndDeviceEventTypeFactory.getDlmsServerSessionFinishedEventType()),
    DLMS_OTHER                              (100036, EndDeviceEventTypeFactory.getDlmsOtherEventType()),
    DLMS_UPSTREAM_TEST                      (100037, EndDeviceEventTypeFactory.getDlmsUpstreamTestEventType()),
    MODEM_WDG_PPPD_RESET                    (100038, EndDeviceEventTypeFactory.getModemWdgPppdResetEventType()),
    MODEM_WDG_HW_RESET                      (100039, EndDeviceEventTypeFactory.getModemWdgHwResetEventType()),
    MODEM_WDG_REBOOT_REQUESTED              (100040, EndDeviceEventTypeFactory.getModemWdgRebootRequestedEventType()),
    MODEM_CONNECTED                         (100041, EndDeviceEventTypeFactory.getModemConnectedEventType()),
    MODEM_DISCONNECTED                      (100042, EndDeviceEventTypeFactory.getModemDisconnectedEventType()),
    MODEM_WAKE_UP                           (100043, EndDeviceEventTypeFactory.getModemWakeUpEventType()),
    PROTOCOL_PRELIMINARY_TASK_COMPLETED     (100044, EndDeviceEventTypeFactory.getProtocolPreliminaryTaskCompletedEventType()),
    PROTOCOL_PRELIMINARY_TASK_FAILED        (100045, EndDeviceEventTypeFactory.getProtocolPreliminaryTaskFailedEventType()),
    PROTOCOL_CONSECUTIVE_FAILURE            (100046, EndDeviceEventTypeFactory.getProtocolConsecutiveFailureEventType()),
    FIRMWARE_UPGRADE                        (100047, EndDeviceEventTypeFactory.getFirmwareUpgradeEventType()),
    FIRMWARE_MODIFIED                       (100048, EndDeviceEventTypeFactory.getFirmwareModifiedEventType()),
    CPU_OVERLOAD                            (100049, EndDeviceEventTypeFactory.getCpuOverloadEventType()),
    RAM_TOO_HIGH                            (100050, EndDeviceEventTypeFactory.getRamTooHighEventType()),
    DISK_USAGE_TOO_HIGH                     (100051, EndDeviceEventTypeFactory.getDiskUsageTooHighEventType()),
    PACE_EXCEPTION                          (100052, EndDeviceEventTypeFactory.getPaceExceptionEventType()),
    SSH_LOGIN                               (100053, EndDeviceEventTypeFactory.getSshLoginEventType()),
    FACTORY_RESET                           (100054, EndDeviceEventTypeFactory.getFactoryResetEventType()),
    WEBPORTAL_LOGIN                         (100055, EndDeviceEventTypeFactory.getWebportalLoginEventType()),
    WEBPORTAL_ACTION                        (100056, EndDeviceEventTypeFactory.getWebportalActionEventType()),
    WEBPORTAL_FAILED_LOGIN                  (100057, EndDeviceEventTypeFactory.getWebportalFailedLoginEventType()),
    WEBPORTAL_LOCKED_USER                   (100058, EndDeviceEventTypeFactory.getWebportalLockedUserEventType()),
    METER_MULTICAST_UPGRADE_START           (100059, EndDeviceEventTypeFactory.getMeterMulticastUpgradeStartEventType()),
    METER_MULTICAST_UPGRADE_COMPLETED       (100060, EndDeviceEventTypeFactory.getMeterMulticastUpgradeCompletedEventType()),
    METER_MULTICAST_UPGRADE_FAILED          (100061, EndDeviceEventTypeFactory.getMeterMulticastUpgradeFailedEventType()),
    METER_MULTICAST_UPGRADE_INFO            (100062, EndDeviceEventTypeFactory.getMeterMulticastUpgradeInfoEventType()),
    GENERAL_SECURITY_ERROR                  (100063, EndDeviceEventTypeFactory.getGeneralSecurityErrorEventType()),
    WRAP_KEY_ERROR                          (100064, EndDeviceEventTypeFactory.getWrapKeyErrorEventType()),
    DLMS_AUTHENTICATION_LEVEL_UPDATED       (100065, EndDeviceEventTypeFactory.getDlmsAuthenticationLevelUpdatedEventType()),
    DLMS_SECURITY_POLICY_UPDATED            (100066, EndDeviceEventTypeFactory.getDlmsSecurityPolicyUpdatedEventType()),
    DLMS_SECURITY_SUITE_UPDATED             (100067, EndDeviceEventTypeFactory.getDlmsSecuritySuiteUpdatedEventType()),
    DLMS_KEYS_UPDATED                       (100069, EndDeviceEventTypeFactory.getDlmsKeysUpdatedEventType()),
    DLMS_ACCESS_VIOLATION                   (100070, EndDeviceEventTypeFactory.getDlmsAccessViolationEventType()),
    DLMS_AUTHENTICATION_FAILURE             (100071, EndDeviceEventTypeFactory.getDlmsAuthenticationFailureEventType()),
    DLMS_CIPHERING_ERROR                    (100072, EndDeviceEventTypeFactory.getDlmsCipheringErrorEventType()),
    UNKNOWN_REGISTER                        (100073, EndDeviceEventTypeFactory.getUnknownRegisterEventType()),
    PLC_G3_BLACKLIST                        (100074, EndDeviceEventTypeFactory.getPlcG3BlacklistEventType()),
    PLC_G3_NODE_LINK_LOST                   (100075, EndDeviceEventTypeFactory.getPlcG3NodeLinkLostEventType()),
    PLC_G3_NODE_LINK_RECOVERED              (100076, EndDeviceEventTypeFactory.getPlcG3NodeLinkRecoveredEventType()),
    PLC_G3_PAN_ID                           (100077, EndDeviceEventTypeFactory.getPlcG3PanIdEventType()),
    PLC_G3_TOPOLOGY_UPDATE                  (100078, EndDeviceEventTypeFactory.getPlcG3TopologyUpdateEventType()),
    MODEM_NEW_SIM                           (100079, EndDeviceEventTypeFactory.getModemNewSimEventType()),
    MODEM_NEW_EQUIPMENT                     (100080, EndDeviceEventTypeFactory.getModemNewEquipmentEventType()),
    CHECK_DATA_CONCENTRATOR_CONFIG          (100081, EndDeviceEventTypeFactory.getCheckDataConcentratorConfigEventType()),
    LINK_UP                                 (100082, EndDeviceEventTypeFactory.getLinkUpEventType()),
    LINK_DOWN                               (100083, EndDeviceEventTypeFactory.getLinkDownEventType()),
    USB_ADD                                 (100084, EndDeviceEventTypeFactory.getUsbAddEventType()),
    USB_REMOVE                              (100085, EndDeviceEventTypeFactory.getUsbRemoveEventType()),
    FILE_TRANSFER_COMPLETED                 (100086, EndDeviceEventTypeFactory.getFileTransferCompletedEventType()),
    FILE_TRANSFER_FAILED                    (100087, EndDeviceEventTypeFactory.getFileTransferFailedEventType()),
    SCRIPT_EXECUTION_STARTED                (100088, EndDeviceEventTypeFactory.getScriptExecutionStartedEventType()),
    SCRIPT_EXECUTION_COMPLETED              (100089, EndDeviceEventTypeFactory.getScriptExecutionCompletedEventType()),
    SCRIPT_EXECUTION_FAILED                 (100090, EndDeviceEventTypeFactory.getScriptExecutionFailedEventType()),
    SCRIPT_EXECUTION_SCHEDULED              (100091, EndDeviceEventTypeFactory.getScriptExecutionScheduledEventType()),
    SCRIPT_EXECUTION_DESCHEDULED            (100092, EndDeviceEventTypeFactory.getScriptExecutionDescheduledEventType()),
    WEBPORTAL_CSRF_ATTACK                   (100093, EndDeviceEventTypeFactory.getWebportalCsrfAttackEventType()),
    SNMP_OTHER                              (100094, EndDeviceEventTypeFactory.getSnmpOtherEventType()),
    SNMP_INFO                               (100095, EndDeviceEventTypeFactory.getSnmpInfoEventType()),
    SNMP_UNSUPPORTED_VERSION                (100096, EndDeviceEventTypeFactory.getSnmpUnsupportedVersionEventType()),
    SNMP_UNSUPPORTED_SEC_MODEL              (100097, EndDeviceEventTypeFactory.getSnmpUnsupportedSecModelEventType()),
    SNMP_INVALID_USER_NAME                  (100098, EndDeviceEventTypeFactory.getSnmpInvalidUserNameEventType()),
    SNMP_INVALID_ENGINE_ID                  (100099, EndDeviceEventTypeFactory.getSnmpInvalidEngineIdEventType()),
    SNMP_AUTHENTICATION_FAILURE             (100100, EndDeviceEventTypeFactory.getSnmpAuthenticationFailureEventType()),
    SNMP_KEYS_UPDATED                       (100101, EndDeviceEventTypeFactory.getSnmpKeysUpdatedEventType()),
    CRL_UPDATED                             (100102, EndDeviceEventTypeFactory.getCrlUpdatedEventType()),
    CRL_UPDATE_REJECTED                     (100103, EndDeviceEventTypeFactory.getCrlUpdateRejectedEventType()),
    KEY_UPDATE_REQUEST                      (100104, EndDeviceEventTypeFactory.getKeyUpdateRequestEventType()),
    CRL_REMOVED                             (100105, EndDeviceEventTypeFactory.getCrlRemovedEventType()),
    DOT1X_SUCCESS                           (100106, EndDeviceEventTypeFactory.getDot1xSuccessEventType()),
    DOT1X_FAILURE                           (100107, EndDeviceEventTypeFactory.getDot1xFailureEventType()),
    REPLAY_ATTACK                           (100108, EndDeviceEventTypeFactory.getReplayAttackEventType()),
    CERTIFICATE_ADDED                       (100109, EndDeviceEventTypeFactory.getCertificateAddedEventType()),
    CERTIFICATE_REMOVED                     (100110, EndDeviceEventTypeFactory.getCertificateRemovedEventType()),
    CERTIFICATE_EXPIRED                     (100111, EndDeviceEventTypeFactory.getCertificateExpiredEventType()),
    POWER_DOWN_POWER_LOST                   (100112, EndDeviceEventTypeFactory.getPowerDownPowerLostEventType()),
    POWER_DOWN_USER_REQUEST                 (100113, EndDeviceEventTypeFactory.getPowerDownUserRequestEventType()),
    POWER_DOWN_SOFTWARE_FAULT               (100114, EndDeviceEventTypeFactory.getPowerDownSoftwareFaultEventType()),
    POWER_DOWN_HARDWARE_FAULT               (100115, EndDeviceEventTypeFactory.getPowerDownHardwareFaultEventType()),
    POWER_DOWN_NETWORK_INACTIVITY           (100116, EndDeviceEventTypeFactory.getPowerDownNetworkInactivityEventType()),
    POWER_DOWN_FIRMWARE_UPGRADE             (100117, EndDeviceEventTypeFactory.getPowerDownFirmwareUpgradeEventType()),
    POWER_DOWN_FIRMWARE_ROLLBACK            (100118, EndDeviceEventTypeFactory.getPowerDownFirmwareRollbackEventType()),
    POWER_DOWN_DISK_ERROR                   (100119, EndDeviceEventTypeFactory.getPowerDownDiskErrorEventType()),
    POWER_DOWN_CONFIGURATION_ERROR          (100120, EndDeviceEventTypeFactory.getPowerDownConfigurationErrorEventType()),
    POWER_DOWN_FACTORY_RESET                (100121, EndDeviceEventTypeFactory.getPowerDownFactoryResetEventType()),
    POWER_DOWN_TAMPERING                    (100122, EndDeviceEventTypeFactory.getPowerDownTamperingEventType()),
    POWER_DOWN_TEMPERATURE                  (100123, EndDeviceEventTypeFactory.getPowerDownTemperatureEventType()),
    POWER_DOWN_SYSTEM_WATCHDOG              (100124, EndDeviceEventTypeFactory.getPowerDownSystemWatchdogEventType()),
    POWER_DOWN_WWAN_MODEM_WATCHDOG          (100125, EndDeviceEventTypeFactory.getPowerDownWwanModemWatchdogEventType()),
    POWER_DOWN_SECURE_ELEMENT_WATCHDOG      (100126, EndDeviceEventTypeFactory.getPowerDownSecureElementWatchdogEventType()),
    POWER_DOWN_EXTERNAL_WATCHDOG            (100127, EndDeviceEventTypeFactory.getPowerDownExternalWatchdogEventType()),
    PROTOCOL_LOG_CLEARED                    (100128, EndDeviceEventTypeFactory.getProtocolLogClearedEventType()),
    METER_CLOCK_INVALID                     (100129, EndDeviceEventTypeFactory.getMeterClockInvalidEventType());

    private final int eisCode;
    private final EndDeviceEventType eventType;

    private EndDeviceEventTypeMapping(int eisCode, EndDeviceEventType eventType) {
        this.eisCode = eisCode;
        this.eventType = eventType;
    }

    public int getEisCode() {
        return eisCode;
    }

    public EndDeviceEventType getEventType() {
        return eventType;
    }

    public static EndDeviceEventType getEventTypeCorrespondingToEISCode(int eisCode) {
        for (EndDeviceEventTypeMapping each : EndDeviceEventTypeMapping.values()) {
            if (each.getEisCode() == eisCode) {
                return each.getEventType();
            }
        }
        return OTHER.getEventType();
    }

    public static int getEISCodeCorrespondingToEventType(EndDeviceEventType endDeviceEventType) {
        for (EndDeviceEventTypeMapping each : EndDeviceEventTypeMapping.values()) {
            if (Objects.equals(each.getEventType().toString(), endDeviceEventType.toString())) {
                return each.getEisCode();
            }
        }
        return OTHER.getEisCode();
    }
}
