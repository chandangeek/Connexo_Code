/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import java.util.Map;

public enum DeviceLifecycleEventDescription {

    TRANSITION_FAILURE("com/energyict/mdc/device/lifecycle/config/transition/FAILED", TransitionFailureEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            return super.matches(map);
        }
    },
    TRANSITION_REMOVED("com/energyict/mdc/device/lifecycle/config/transition/REMOVED", TransitionRemovedEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            return super.matches(map);
        }
    },
    TRANSITION_DONE("com/energyict/mdc/device/lifecycle/config/transition/DONE", TransitionDoneEvent.class) {
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
        String topic = (String) map.get("event.topics");
        return this.topic.equalsIgnoreCase(topic);
    }
}
