/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Provides an implementation for the {@link ScheduledJobExecutionEventListener} interface
 * that can be used for inbound communication where we are not really interested in the notifications.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-30 (11:18)
 */
public class InboundScheduledJobExecutionEventListener implements ScheduledJobExecutionEventListener {

    @Override
    public void executionStarted (ScheduledJob job) {
        // Ok got it!
    }

}