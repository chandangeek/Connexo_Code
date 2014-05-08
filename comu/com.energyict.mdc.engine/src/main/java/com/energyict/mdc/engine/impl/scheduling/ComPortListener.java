package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.InboundComPort;

/**
 * Models the behavior of a component that will listen for
 * communication that is started by a remote device
 * via an InboundComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:23)
 */
public interface ComPortListener extends ComPortServerProcess {

    /**
     * Gets the {@link InboundComPort} for which this ComPortListener is working.
     *
     * @return The InboundComPort
     */
    public InboundComPort getComPort ();

    /**
     * Receives notification from the ComServer that the
     * changes interpoll delay changed.
     *
     * @param changesInterpollDelay The new scheduling interpoll delay
     * @see com.energyict.mdc.engine.model.ComServer#getChangesInterPollDelay()
     */
    public void changesInterpollDelayChanged (TimeDuration changesInterpollDelay);

    /**
     * Receives notification from the ComServer that the
     * scheduling interpoll delay changed.
     *
     * @param schedulingInterpollDelay The new scheduling interpoll delay
     * @see com.energyict.mdc.engine.model.ComServer#getSchedulingInterPollDelay()
     */
    public void schedulingInterpollDelayChanged (TimeDuration schedulingInterpollDelay);

}