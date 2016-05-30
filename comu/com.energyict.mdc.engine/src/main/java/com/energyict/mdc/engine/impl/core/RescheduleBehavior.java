package com.energyict.mdc.engine.impl.core;

import java.time.Instant;

/**
 * The {@link RescheduleBehavior} provides functionality to deal
 * with retries on Communication or Connection level.
 *
 * Copyrights EnergyICT
 * Date: 22/04/13
 * Time: 11:53
 */
public interface RescheduleBehavior {

    public enum RescheduleReason {
        /**
         * Handle the retry logic for the scenario where the
         * connection could not be correctly set up.
         */
        CONNECTION_SETUP,
        /**
         * Handle the retry logic for the scenario where the
         * connection could be setup correctly, but during the
         * execution of ComTasks, something happened with the connection
         * that lead to unsuccessful communication. Some comTasks
         * may be executed successfully, but this is not required.
         */
        CONNECTION_BROKEN,
        /**
         * Handle the reschedule logic where the connection could be
         * properly set-up, and the execution of the ComTasks
         * was complete (failed or success).
         */
        COMTASKS,
        /**
         * Handle the retry logic for the scenario where the
         * connection was not allowed to be setup because the
         * time of execution was outside the {@link com.energyict.mdc.common.ComWindow}
         * of the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
         */
        OUTSIDE_COM_WINDOW
    }

    public void performRescheduling(RescheduleReason reason);

    public void rescheduleOutsideWindow(Instant startingPoint);

}