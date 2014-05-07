package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.comserver.eventsimpl.EventPublisher;

/**
 * Models a request to widen the interest of
 * {@link com.energyict.mdc.engine.model.ComPortPool}
 * related events to all ComPortPools.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
public class AllComPortPoolsRequest extends RequestImpl {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllComPortPools(null);
    }

}