package com.energyict.mdc.scheduling.events;

/**
 * Models the different event types that are produced by this "device type and configurations bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:44)
 */
public enum EventType {

    NEXTEXECUTIONSPECS_CREATED("nextexecutionspecs/CREATED"),
    NEXTEXECUTIONSPECS_UPDATED("nextexecutionspecs/UPDATED"),
    NEXTEXECUTIONSPECS_DELETED("nextexecutionspecs/DELETED"),
    COMSCHEDULE_BEFORE_DELETE("comschedule/BEFORE_DELETE"),
    ;

    private static final String NAMESPACE = "com/energyict/mdc/scheduling/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

}