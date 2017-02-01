/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Modesl a request to widen the interest of
 * device related events to all devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
class AllDevicesRequest implements Request {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllDevices(null);
    }

}