/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Models the data that is involved in events that relate to
 * {@link ComTaskEnablement}s
 * and the way they use {@link PartialConnectionTask}
 * or the default connection task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:21)
 */
public abstract class ConnectionStrategyChangeEventData {

    private final long comTaskEnablementId;

    protected ConnectionStrategyChangeEventData(long comTaskEnablementId) {
        super();
        this.comTaskEnablementId = comTaskEnablementId;
    }

    protected abstract ConnectionStrategyEventType getEventType();

    public void publish (EventService eventService) {
        eventService.postEvent(this.getEventType().topic(), this);
    }

    public long getComTaskEnablementId() {
        return comTaskEnablementId;
    }

}