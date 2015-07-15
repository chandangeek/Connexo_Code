package com.energyict.mdc.device.data.impl.events;

/**
 * Models an event that is published for a-synchronous handling
 * for the purpose of checking that all connection tasks
 * that depend on a partial connection task that had
 * at least one of its required properties removed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-14 (16:58)
 */
public class RevalidateAllConnectionTasksAfterPropertyRemoval {

    private final long partialConnectionTaskId;

    public RevalidateAllConnectionTasksAfterPropertyRemoval(long partialConnectionTaskId) {
        super();
        this.partialConnectionTaskId = partialConnectionTaskId;
    }

    public long getPartialConnectionTaskId() {
        return partialConnectionTaskId;
    }

}