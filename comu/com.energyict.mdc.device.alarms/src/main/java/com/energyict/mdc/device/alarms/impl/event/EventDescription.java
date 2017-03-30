/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;

import java.util.List;
import java.util.Map;

public interface EventDescription {
    /**
     * @return class of concrete realization for this type of event
     */
    Class<? extends DeviceAlarmEvent> getEventClass();
    /**
     * @param map
     * @return true if the input map can produce this type of events
     */
    boolean validateEvent(Map<?, ?> map);
    /**
     This method should be called when a single input value may produce several events.
     @return properties map which can produce only single event
    */
    List<Map<?, ?>> splitEvents(Map<?, ?> map);
    /**
     * @return string which is a unique key for the specific type of event
     */
    String getUniqueKey();
}
