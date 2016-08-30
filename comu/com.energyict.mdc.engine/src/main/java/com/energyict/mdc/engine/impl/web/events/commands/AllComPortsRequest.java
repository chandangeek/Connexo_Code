package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Modesl a request to widen the interest of
 * {@link com.energyict.mdc.engine.config.ComPort}
 * related events to all ComPorts.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
class AllComPortsRequest implements Request {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllComPorts(null);
    }

}