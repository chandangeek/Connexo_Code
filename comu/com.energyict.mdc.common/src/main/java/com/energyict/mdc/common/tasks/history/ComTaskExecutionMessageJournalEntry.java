/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models a {@link ComTaskExecutionJournalEntry} for a simple message
 * that is logged as part of the execution of a {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (09:32)
 */
@ConsumerType
public interface ComTaskExecutionMessageJournalEntry extends ComTaskExecutionJournalEntry {


    /**
     * Gets the message that was logged.
     *
     * @return The message
     */
    String getMessage();

}