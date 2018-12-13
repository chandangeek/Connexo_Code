/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;

import java.time.Instant;
import java.util.List;

public interface RescheduleBehavior {

    void reschedule(CommandRoot commandRoot);

    void rescheduleOutsideComWindow(List<ComTaskExecution> comTaskExecutions, Instant startingPoint);

    enum RescheduleReason {
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
}