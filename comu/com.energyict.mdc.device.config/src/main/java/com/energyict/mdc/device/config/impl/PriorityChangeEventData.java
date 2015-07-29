package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.events.EventType;

/**
 * Models the data that is involved in events that relate to
 * changes of {@link com.energyict.mdc.device.config.ComTaskEnablement}'s priority.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:21)
 */
public class PriorityChangeEventData {
    private final long comTaskEnablementId;
    private final int oldPriority;
    private final int newPriority;

    public PriorityChangeEventData(ComTaskEnablement comTaskEnablement, int oldPriority, int newPriority) {
        super();
        this.comTaskEnablementId = comTaskEnablement.getId();
        this.oldPriority = oldPriority;
        this.newPriority = newPriority;
    }

    public void publish (EventService eventService) {
        eventService.postEvent(EventType.COMTASKENABLEMENT_PRIORITY_UPDATED.topic(), this);
    }

    public long getComTaskEnablementId() {
        return comTaskEnablementId;
    }

    public int getOldPriority() {
        return oldPriority;
    }

    public int getNewPriority() {
        return newPriority;
    }

}