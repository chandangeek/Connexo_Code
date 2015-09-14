package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.rest.DashboardApplication;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_END_DEVICE_GROUP(1, "NoSuchEndDeviceGroup", "No end device group exists with id ''{0}''"),
    NO_SUCH_CONNECTION_TASK(2, "NoSuchConnectionTask", "No connection task with id {0}"),
    NO_SUCH_LABEL_CATEGORY(3, "NoSuchLabelCategory", "No such label category with key {0}"),
    RUN_CONNECTIONTASK_IMPOSSIBLE(4,"runConTaskImpossible", "Running of this connection task is impossible"),
    NO_SUCH_COMMUNICATION_TASK(5, "NoSuchCommunicationTask", "No communication task with id {0}"),
    NO_SUCH_MESSAGE_QUEUE(6, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    NO_APPSERVER(7, "NoAppServer", "There is no active application server that can handle this request"),
    CONNECTION_TASK_NOT_UNIQUE(8, "NotUniqueConnectionTask", "Only a single connection type can be handled in connection task attributes update"),
    ONE_CONNECTION_TYPE_REQUIRED(9, "OneConnetionTypeRequired", "No connection type could be identified"),
    UNSUPPORTED_KPI_PERIOD(10, "UnsupportedKpiPeriod", "Read-outs are not available for this period"),
    ;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
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