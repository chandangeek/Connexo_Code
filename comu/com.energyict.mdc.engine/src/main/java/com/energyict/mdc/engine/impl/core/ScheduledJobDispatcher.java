package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Models the behavior of a component that will dispatch {@link com.energyict.mdc.engine.impl.core.ScheduledJob}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-04 (11:40)
 */
public interface ScheduledJobDispatcher {

    /**
     * Returns the next {@link com.energyict.mdc.engine.impl.core.ScheduledJob} that is waiting for execution.
     *
     * @return The ScheduledJob or <code>null</code> if there are no more ScheduledJobs waiting to be executed
     */
    public ScheduledJob nextJob ();

}