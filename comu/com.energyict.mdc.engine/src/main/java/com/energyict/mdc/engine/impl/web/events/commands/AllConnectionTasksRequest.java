/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Modesl a request to widen the interest of
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * related events to all ConnectionTasks.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:46)
 */
class AllConnectionTasksRequest implements Request {

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.widenInterestToAllConnectionTasks(null);
    }

}