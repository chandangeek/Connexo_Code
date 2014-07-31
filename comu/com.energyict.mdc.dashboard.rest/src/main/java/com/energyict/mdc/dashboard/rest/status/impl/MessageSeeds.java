package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    SUCCESS(1, "Success", "Success", Level.SEVERE),
    ALL_TASKS_SUCCESSFUL(2, "AllTasksSuccessful", "All tasks successful", Level.SEVERE),
    SOME_TASKS_FAILED(3, "SomeTasksFailed", "At least one task failed", Level.SEVERE),
    PENDING(4, "Pending", "Pending", Level.SEVERE),
    FAILED(5, "Failed", "Failed", Level.SEVERE),
    BUSY(6, "Busy", "Busy", Level.SEVERE),
    ON_HOLD(7, "OnHold", "On hold", Level.SEVERE),
    RETRYING(8, "Retrying", "Retrying", Level.SEVERE),
    NEVER_COMPLETED(9, "NeverCompleted", "Never completed", Level.SEVERE),
    WAITING(10, "Waiting", "Waiting", Level.SEVERE),
    BROKEN(11, "Broken", "Broken", Level.SEVERE),
    SETUP_ERROR(12, "SetupError", "Setup error", Level.SEVERE),
    PER_CURRENT_STATE(13, "PerCurrentState", "Per current state", Level.SEVERE),
    PER_LATEST_RESULT(14, "PerLatestResult", "Per latest result", Level.SEVERE),
    CONNECTION_ERROR(15, "ConnectionError", "connection error", Level.SEVERE),
    CONFIGURATION_ERROR(16, "ConfigurationError", "configuration error", Level.SEVERE),
    CONFIGURATION_WARNING(17, "ConfigurationWarning", "configuration warning", Level.SEVERE),
    IO_ERROR(18, "IoError", "I/O error", Level.SEVERE),
    PROTOCOL_ERROR(19, "ProtocolError", "protocol error", Level.SEVERE),
    OK(20, "OK", "Ok", Level.SEVERE),
    RESCHEDULED(21, "Rescheduled", "Rescheduled", Level.SEVERE),
    TIME_ERROR(22, "TimeError", "Time error", Level.SEVERE),
    UNEXPECTED_ERROR(22, "UnexpectedError", "Unexpected error", Level.SEVERE),
    PER_COMMUNICATION_POOL(23, "PerCommunicationPool", "Per communication pool", Level.SEVERE),
    PER_CONNECTION_TYPE(24, "PerConnectionType", "Per connection type", Level.SEVERE ),
    PER_DEVICE_TYPE(24, "PerDeviceType", "Per device type", Level.SEVERE ),
    COMPORTPOOL_FILTER(25, FilterOption.comPortPool.name(), "communication port pool", Level.SEVERE),
    DEVICETYPE_FILTER(26, FilterOption.deviceType.name(), "device type", Level.SEVERE),
    CONNECTIONTYPE_FILTER(27, FilterOption.connectionType.name(), "connection type", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
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
        return level;
    }


}
