package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.issues.IssueService;

/**
 * Provides factory services for {@link com.energyict.mdc.engine.impl.core.ScheduledComPort}s,
 * will know exactly when to create a single threaded or a
 * multi threaded implementation class for a certain {@link OutboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:16)
 */
public interface ScheduledComPortFactory {

    /**
     * Creates a new {@link com.energyict.mdc.engine.impl.core.ScheduledComPort} for the specified {@link OutboundComPort}
     * if it is necessary to schedule that OutboundComPort.
     * It would not be necessary to schedule the OutboundComPort if it is not active
     * or it if has no simultaneous connections.
     *
     * @return The ScheduledComPort or <code>null</code> if it was not necessary to schedule
     *         the OutboundComPort
     */
    public ScheduledComPort newFor(OutboundComPort comPort, IssueService issueService);

}