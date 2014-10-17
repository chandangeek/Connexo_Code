package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.engine.model.ComServer;

/**
 * Models a {@link ComTaskExecutionJournalEntry} for a simple message
 * that is logged as part of the execution of a {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (09:32)
 */
public interface ComTaskExecutionMessageJournalEntry extends ComTaskExecutionJournalEntry {


    /**
     * Gets the message that was logged.
     *
     * @return The message
     */
    public String getMessage ();

}