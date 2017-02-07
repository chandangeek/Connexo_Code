/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Models a request to widen the interest of
 * {@link com.energyict.mdc.engine.config.ComPortPool}
 * related events to all ComPortPools.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
class AllComPortPoolsRequest implements Request {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllComPortPools(null);
    }

}