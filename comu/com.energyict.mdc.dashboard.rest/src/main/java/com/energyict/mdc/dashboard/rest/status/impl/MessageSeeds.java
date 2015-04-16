package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.protocol.api.ConnectionType;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    SUCCESS(1, "Success", "Success"),
    ALL_TASKS_SUCCESSFUL(2, "AllTasksSuccessful", "Success, all tasks successful"),
    SUCCESS_WITH_FAILED_TASKS(3, "SomeTasksFailed", "Success, with failed tasks"),
    PENDING(4, "Pending", "Pending"),
    FAILED(5, "Failed", "Failed"),
    BUSY(6, "Busy", "Busy"),
    ON_HOLD(7, "OnHold", "Inactive"),
    RETRYING(8, "Retrying", "Retrying"),
    NEVER_COMPLETED(9, "NeverCompleted", "Never completed"),
    WAITING(10, "Waiting", "Waiting"),
    BROKEN(11, "Broken", "Broken"),
    SETUP_ERROR(12, "SetupError", "Setup error"),
    PER_CURRENT_STATE(13, "PerCurrentState", "Per current state"),
    PER_LATEST_RESULT(14, "PerLatestResult", "Per latest result"),
    CONNECTION_ERROR(15, "ConnectionError", "Connection error"),
    CONFIGURATION_ERROR(16, "ConfigurationError", "Configuration error"),
    CONFIGURATION_WARNING(17, "ConfigurationWarning", "Configuration warning"),
    IO_ERROR(18, "IoError", "I/O error"),
    PROTOCOL_ERROR(19, "ProtocolError", "Protocol error"),
    OK(20, "OK", "Ok"),
    RESCHEDULED(21, "Rescheduled", "Rescheduled"),
    TIME_ERROR(22, "TimeError", "Time error"),
    PER_COMMUNICATION_POOL(23, "PerCommunicationPool", "Per communication port pool"),
    PER_CONNECTION_TYPE(24, "PerConnectionType", "Per connection type"),
    PER_DEVICE_TYPE(25, "PerDeviceType", "Per device type"),
    COMPORTPOOL_FILTER(26, HeatMapBreakdownOption.comPortPools.name(), "communication port pool"),
    DEVICETYPE_FILTER(27, HeatMapBreakdownOption.deviceTypes.name(), "device type"),
    CONNECTIONTYPE_FILTER(28, HeatMapBreakdownOption.connectionTypes.name(), "connection type"),
    ACTIVE(30, "active", "active"),
    INACTIVE(31, "inactive", "inactive"),
    INCOMPLETE(32, "incomplete", "incomplete"),
    INBOUND(33, ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(34, ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    DEFAULT(35, "default", "default"),
    AS_SOON_AS_POSSIBLE(36, "asSoonAsPossible", "As soon as possible"),
    MINIMIZE_CONNECTIONS(37, "minimizeConnections", "Minimize connections"),
    PER_COMMUNICATION_TASK(38, "PerCommunicationTask", "Per communication task"),
    PER_COMMUNICATION_SCHEDULE(39, "PerCommunicationSchedule", "Per communication schedule"),
    INDIVIDUAL(40, "Individual", "Individual"),
    FAILURE(41, "Failure", "Failure"),
    NOT_APPLICABLE(42, "NotApplicable", "Not applicable"),
    NO_RESTRICTIONS(43, "NoRestrictions", "No restrictions"),
    ONLINE(44, "Online", "Online"),
    REMOTE(45, "Remote", "Remote"),
    MOBILE(46, "Mobile", "Mobile"),
    UNSUPPORTED_KPI_PERIOD(47, "UnsupportedKpiPeriod", "Read-outs are not available for this period"),
    ONGOING(48, "Ongoing", "Ongoing" ),
    UNEXPECTED_ERROR(49, "UnexpectedError", "Unexpected error"),
    TARGET(50, "Target", "Target"),
    NO_SUCH_END_DEVICE_GROUP(51, "NoSuchEndDeviceGroup", "No end device group exists with id ''{0}''"),
    NO_SUCH_CONNECTION_TASK(52, "NoSuchConnectionTask", "No connection task with id {0}"),
    NO_SUCH_LABEL_CATEGORY(53, "NoSuchLabelCategory", "No such label category with key {0}"),
    RUN_CONNECTIONTASK_IMPOSSIBLE(54,"runConTaskImpossible", "Running of this connection task is impossible"),
    NO_SUCH_COMMUNICATION_TASK(55, "NoSuchCommunicationTask", "No communication task with id {0}"),
    NO_SUCH_MESSAGE_QUEUE(56, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    NO_APPSERVER(57, "NoAppServer", "There is no active application server that can handle this request"),
    CONNECTION_TASK_NOT_UNIQUE(58, "NotUniqueConnectionTask", "Only a single connection type can be handled in connection task attributes update"),
    ONE_CONNECTION_TYPE_REQUIRED(59, "OneConnetionTypeRequired", "No connection type could be identified");

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
