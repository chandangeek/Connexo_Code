package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum TranslationKeys implements TranslationKey {

    DEFAULT("default", "default"),
    SUCCESS("Success", "Success"),
    ALL_TASKS_SUCCESSFUL("AllTasksSuccessful", "Success, all tasks successful"),
    SUCCESS_WITH_FAILED_TASKS("SomeTasksFailed", "Success, with failed tasks"),
    PER_CURRENT_STATE("PerCurrentState", "Per current state"),
    PER_LATEST_RESULT("PerLatestResult", "Per latest result"),
    OK("OK", "Ok"),
    PER_COMMUNICATION_POOL("PerCommunicationPool", "Per communication port pool"),
    PER_CONNECTION_TYPE("PerConnectionType", "Per connection type"),
    PER_DEVICE_TYPE("PerDeviceType", "Per device type"),
    COMPORTPOOL_FILTER(HeatMapBreakdownOption.comPortPools.name(), "communication port pool"),
    DEVICETYPE_FILTER(HeatMapBreakdownOption.deviceTypes.name(), "device type"),
    CONNECTIONTYPE_FILTER(HeatMapBreakdownOption.connectionTypes.name(), "connection type"),
    ACTIVE("active", "active"),
    INACTIVE("inactive", "inactive"),
    INCOMPLETE("incomplete", "incomplete"),
    INBOUND(ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    PER_COMMUNICATION_TASK("PerCommunicationTask", "Per communication task"),
    PER_COMMUNICATION_SCHEDULE("PerCommunicationSchedule", "Per communication schedule"),
    INDIVIDUAL("Individual", "Individual"),
    FAILURE("Failure", "Failure"),
    NOT_APPLICABLE("NotApplicable", "Not applicable"),
    NO_RESTRICTIONS("NoRestrictions", "No restrictions"),
    ONLINE("Online", "Online"),
    REMOTE("Remote", "Remote"),
    MOBILE("Mobile", "Mobile"),
    ONGOING("Ongoing", "Ongoing" ),
    TARGET("Target", "Target"),

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