package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.protocol.api.ConnectionType;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    SUCCESS(1, "Success", "Success"),
    ALL_TASKS_SUCCESSFUL(2, "AllTasksSuccessful", "All tasks successful"),
    SOME_TASKS_FAILED(3, "SomeTasksFailed", "At least one task failed"),
    PENDING(4, "Pending", "Pending"),
    FAILED(5, "Failed", "Failed"),
    BUSY(6, "Busy", "Busy"),
    ON_HOLD(7, "OnHold", "On hold"),
    RETRYING(8, "Retrying", "Retrying"),
    NEVER_COMPLETED(9, "NeverCompleted", "Never completed"),
    WAITING(10, "Waiting", "Waiting"),
    BROKEN(11, "Broken", "Broken"),
    SETUP_ERROR(12, "SetupError", "Setup error"),
    PER_CURRENT_STATE(13, "PerCurrentState", "Per current state"),
    PER_LATEST_RESULT(14, "PerLatestResult", "Per latest result"),
    CONNECTION_ERROR(15, "ConnectionError", "connection error"),
    CONFIGURATION_ERROR(16, "ConfigurationError", "configuration error"),
    CONFIGURATION_WARNING(17, "ConfigurationWarning", "configuration warning"),
    IO_ERROR(18, "IoError", "I/O error"),
    PROTOCOL_ERROR(19, "ProtocolError", "protocol error"),
    OK(20, "OK", "Ok"),
    RESCHEDULED(21, "Rescheduled", "Rescheduled"),
    TIME_ERROR(22, "TimeError", "Time error"),
    UNEXPECTED_ERROR(22, "UnexpectedError", "Unexpected error"),
    PER_COMMUNICATION_POOL(23, "PerCommunicationPool", "Per communication pool"),
    PER_CONNECTION_TYPE(24, "PerConnectionType", "Per connection type"),
    PER_DEVICE_TYPE(25, "PerDeviceType", "Per device type"),
    COMPORTPOOL_FILTER(26, BreakdownOption.comPortPool.name(), "communication port pool"),
    DEVICETYPE_FILTER(27, BreakdownOption.deviceType.name(), "device type"),
    CONNECTIONTYPE_FILTER(28, BreakdownOption.connectionType.name(), "connection type"),
    AT_LEAST_ONE_FAILED(29, "AtLeastOneFailed", "at least one failed"),
    ACTIVE(30, "Active", "active"),
    INACTIVE(31, "Inactive", "inactive"),
    INCOMPLETE(32, "Incomplete", "incomplete"),
    INBOUND(33, ConnectionType.Direction.INBOUND.name(), "inbound"),
    OUTBOUND(34, ConnectionType.Direction.OUTBOUND.name(), "outbound"),
    DEFAULT(35, "default", "default"),
    AS_SOON_AS_POSSIBLE(36, "asSoonAsPossible", "As soon a possible"),
    MINIMIZE_CONNECTIONS(37, "minimizeConnections", "Minimize connections"),

    ;

    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DashboardApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }


}
