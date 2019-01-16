/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;

import org.osgi.service.event.EventConstants;

import java.util.Map;

public enum DeviceLifecycleEventDescription {

    TRANSITION_FAILURE("com/energyict/mdc/device/lifecycle/transition/FAILED", TransitionFailureEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            return super.matches(map);
        }
    }
    ;
    
    private String topic;
    private Class<? extends DeviceLifecycleEvent> eventClass;
    
    private DeviceLifecycleEventDescription(String topic, Class<? extends DeviceLifecycleEvent> eventClass) {
        this.topic = topic;
        this.eventClass = eventClass;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public Class<? extends DeviceLifecycleEvent> getEventClass() {
        return eventClass;
    }
    
    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return this.topic.equalsIgnoreCase(topic);
    }
}
