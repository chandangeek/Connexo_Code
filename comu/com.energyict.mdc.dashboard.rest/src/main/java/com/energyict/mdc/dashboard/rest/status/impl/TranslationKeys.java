/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.protocol.ConnectionType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum TranslationKeys implements TranslationKey {

    DEFAULT("default", "default"),
    SUCCESS("Successful", "Successful"),
    ALL_TASKS_SUCCESSFUL("AllTasksSuccessful", "Successful"),
    SUCCESS_WITH_FAILED_TASKS("SomeTasksFailed", "Successful, with failed tasks"),
    PER_CURRENT_STATE("PerCurrentState", "Per status"),
    PER_LATEST_RESULT("PerLatestResult", "Per last result"),
    OK("OK", "Successful"),
    PER_COMMUNICATION_POOL("PerCommunicationPool", "Per communication port pool"),
    PER_CONNECTION_TYPE("PerConnectionType", "Per connection type"),
    PER_DEVICE_TYPE("PerDeviceType", "Per device type"),
    COMPORTPOOL_FILTER(HeatMapBreakdownOption.comPortPools.name(), "communication port pool"),
    DEVICETYPE_FILTER(HeatMapBreakdownOption.deviceTypes.name(), "device type"),
    CONNECTIONTYPE_FILTER(HeatMapBreakdownOption.connectionTypes.name(), "connection type"),
    ACTIVE("active", "active"),
    INACTIVE("inactive", "inactive"),
    INCOMPLETE("incomplete", "incomplete"),
    INBOUND(ConnectionType.ConnectionTypeDirection.INBOUND.name(), "Inbound"),
    OUTBOUND(ConnectionType.ConnectionTypeDirection.OUTBOUND.name(), "Outbound"),
    PER_COMMUNICATION_TASK("PerCommunicationTask", "Per communication task"),
    PER_COMMUNICATION_SCHEDULE("PerCommunicationSchedule", "Per communication schedule"),
    INDIVIDUAL("Individual", "Individual"),
    FAILURE("Failure", "Failed"),
    NOT_APPLICABLE("NotApplicable", "Not applicable"),
    NO_RESTRICTIONS("NoRestrictions", "No restrictions"),
    ONLINE("Online", "Online"),
    REMOTE("Remote", "Remote"),
    MOBILE("Mobile", "Mobile"),
    ONGOING("Ongoing", "Ongoing" ),
    TARGET("Target", "Target"),
    CONNECTION_FUNCTION("Connection.function", "''{0}'' function"),
    NONE("None", "None"),
    NEVER_STARTED_COMPLETION_CODE("NeverStarted", "Never started")
    ;

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}