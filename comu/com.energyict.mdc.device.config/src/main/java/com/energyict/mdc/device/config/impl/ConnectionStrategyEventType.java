package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.events.EventType;

/**
 * Subset of {@link EventType}s that relate to
 * {@link com.energyict.mdc.device.config.ComTaskEnablement}
 * and the way they use {@link com.energyict.mdc.device.config.PartialConnectionTask}
 * or the default connection task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:04)
 */
public enum ConnectionStrategyEventType {

    COMTASKENABLEMENT_SWITCH_ON_DEFAULT(EventType.COMTASKENABLEMENT_SWITCH_ON_DEFAULT),
    COMTASKENABLEMENT_SWITCH_OFF_DEFAULT(EventType.COMTASKENABLEMENT_SWITCH_OFF_DEFAULT),
    COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK(EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK),
    COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT(EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT),
    COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS(EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS),
    COMTASKENABLEMENT_START_USING_TASK(EventType.COMTASKENABLEMENT_START_USING_TASK),
    COMTASKENABLEMENT_REMOVE_TASK(EventType.COMTASKENABLEMENT_REMOVE_TASK);

    private EventType eventType;

    ConnectionStrategyEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}