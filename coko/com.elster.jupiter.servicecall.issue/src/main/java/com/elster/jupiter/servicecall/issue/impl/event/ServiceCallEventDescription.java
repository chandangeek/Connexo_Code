/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import org.osgi.service.event.EventConstants;

import java.util.Map;

public enum ServiceCallEventDescription {

    CANNOT_ESTIMATE_DATA("com/elster/jupiter/estimation/estimationblock/FAILURE", ServiceCallStateChangedEvent.class),
//    READINGQUALITY_DELETED("com/elster/jupiter/metering/readingquality/DELETED", SuspectDeletedEvent.class) {
//        @Override
//        public boolean matches(Map<?, ?> map) {
//            // TODO: refactor to choose proper quality code system when issue management becomes available for MDM
//            return super.matches(map)
//                    && ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT).getCode()
//                    .equals(map.get("readingQualityTypeCode"));
//        }
//    }
    ;

    private String topic;
    private Class<? extends ServiceCallStateChangedEvent> eventClass;

    private ServiceCallEventDescription(String topic, Class<? extends ServiceCallStateChangedEvent> eventClass) {
        this.topic = topic;
        this.eventClass = eventClass;
    }

    public String getTopic() {
        return topic;
    }

    public Class<? extends ServiceCallStateChangedEvent> getEventClass() {
        return eventClass;
    }

    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return this.topic.equalsIgnoreCase(topic);
    }
}
