/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest;

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

    public static final String ACTION_CLASS_RETRY_COMMUNICATION = "com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskAction";
    public static final String ACTION_CLASS_RETRY_COMMUNICATION_NOW = "com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskNowAction";
    public static final String ACTION_CLASS_RETRY_CONNECTION = "com.energyict.mdc.issue.datacollection.impl.actions.RetryConnectionTaskAction";
}
