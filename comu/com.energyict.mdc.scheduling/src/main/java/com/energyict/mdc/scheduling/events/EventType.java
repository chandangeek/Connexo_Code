/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.events;

import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;

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
    COMSCHEDULES_UPDATED("comschedules/UPDATED"),
    COMSCHEDULES_DELETED("comschedules/DELETED"),
    COMSCHEDULES_BEFORE_OBSOLETE("comschedules/BEFORE_OBSOLETE"),
    COMSCHEDULES_OBSOLETED("comschedules/OBSOLETED"),
    COMTASK_WILL_BE_ADDED_TO_SCHEDULE("comschedules/COMTASK_WILL_BE_ADDED") {
        public EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
//            return eventTypeBuilder.withProperty("comScheduleId", ValueType.LONG, "comScheduleId").withProperty("comTaskId", ValueType.LONG, "comTaskId");
            return eventTypeBuilder;
        }

    },
    ;

    private static final String NAMESPACE = "com/energyict/mdc/scheduling/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    public EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
    }


}