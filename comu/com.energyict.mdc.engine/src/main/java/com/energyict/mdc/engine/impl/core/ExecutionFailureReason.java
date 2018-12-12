/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Models the different reasons for the execution of a {@link ScheduledJob}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (16:00)
 */
public enum ExecutionFailureReason {

    /**
     * Setting up the {@link com.energyict.mdc.device.data.tasks.ConnectionTask connection} failed.
     */
    CONNECTION_SETUP {
        @Override
        public RescheduleBehavior.RescheduleReason toRescheduleReason() {
            return RescheduleBehavior.RescheduleReason.CONNECTION_SETUP;
        }
    },

    /**
     * The {@link com.energyict.mdc.device.data.tasks.ConnectionTask connection}
     * was setup correctly but then broke off due to some technical error or failure.
     */
    CONNECTION_BROKEN {
        @Override
        public RescheduleBehavior.RescheduleReason toRescheduleReason() {
            return RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN;
        }
    },

    /**
     * Setting up the connection was not allowed to be setup because the
     * time of execution was outside the {@link com.energyict.mdc.common.ComWindow}
     * of the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
     */
    OUTSIDE_COM_WINDOW {
        @Override
        public RescheduleBehavior.RescheduleReason toRescheduleReason() {
            return RescheduleBehavior.RescheduleReason.OUTSIDE_COM_WINDOW;
        }
    };

    public abstract RescheduleBehavior.RescheduleReason toRescheduleReason();

}