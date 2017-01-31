/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Modesl a request to widen the interest of
 * {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}
 * related events to all ComTaskExecutions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
class AllComTaskExecutionsRequest implements Request {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllComTaskExecutions(null);
    }

}