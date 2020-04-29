/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl;

public final class ModuleConstants {
    private ModuleConstants() {
    }

    public static final String REASON_UNKNOWN_INBOUND_DEVICE = "reason.unknown.inbound.device";
    public static final String REASON_UNKNOWN_OUTBOUND_DEVICE = "reason.unknown.outbound.device";
    public static final String REASON_FAILED_TO_COMMUNICATE = "reason.failed.to.communicate";
    public static final String REASON_CONNECTION_SETUP_FAILED = "reason.connection.setup.failed";
    public static final String REASON_CONNECTION_FAILED = "reason.connection.failed";
    public static final String REASON_POWER_OUTAGE = "reason.power.outage";
    public static final String REASON_TYME_SYNC_FAILED = "reason.tyme.sync.failed";
    public static final String REASON_UNREGISTERED_DEVICE = "reason.unregistered.device";

    public static final String AQ_DATA_COLLECTION_EVENT_SUBSC = "IssueCreationDC";
    public static final String AQ_DATA_COLLECTION_EVENT_DISPLAYNAME = "Create data collection issues";
    public static final String AQ_DELAYED_ISSUE_SUBSC = "DelayedIssueDC";
    public static final String AQ_DELAYED_ISSUE_DISPLAYNAME = "Create data collection issues with a delay";

    public static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    public static final String MASTER_DEVICE_IDENTIFIER = "masterDeviceId";
    public static final String GATEWAY_IDENTIFIER = "gatewayIdentifier";
    public static final String EVENT_IDENTIFIER = "eventIdentifier";
    public static final String FAILED_TASK_IDS = "failedTaskIDs";
    public static final String SKIPPED_TASK_IDS = "skippedTaskIDs";
    public static final String SUCCESS_TASK_IDS = "successTaskIDs";
    public static final String CONNECTION_TASK_ID = "connectionTaskId";
    public static final String COM_SESSION_ID = "comSessionId";
    public static final String EVENT_TIMESTAMP = "timestamp";
    public static final long MDC_AMR_SYSTEM_ID = 1L;

    public static final String DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES = "deviceLifecycleInDeviceStates";
    public static final String EVENT_TEMPORAL_THRESHOLD = "eventTemporalThreshold";

    public static final String ISSUE_RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.issue";

}
