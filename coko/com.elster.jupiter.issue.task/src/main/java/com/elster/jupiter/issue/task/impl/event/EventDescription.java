/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.event;


import com.elster.jupiter.issue.task.event.TaskEvent;

import java.util.List;
import java.util.Map;

public interface EventDescription {
    /**
     * @return class of concrete realization for this type of event
     */
    Class<? extends TaskEvent> getEventClass();
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
     * @return true if event of this type can be used for event aggregation
     */
   // boolean canBeAggregated();
    /**
     * @return string which is a unique key for the specific type of event (for example 'TASK_FAILED' for all events with this type)
     */
    String getUniqueKey();
}
