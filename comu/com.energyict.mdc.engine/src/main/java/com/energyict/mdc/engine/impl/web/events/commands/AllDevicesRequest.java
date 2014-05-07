package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.comserver.eventsimpl.EventPublisher;

/**
 * Modesl a request to widen the interest of
 * device related events to all devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
public class AllDevicesRequest extends RequestImpl {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllDevices(null);
    }

}