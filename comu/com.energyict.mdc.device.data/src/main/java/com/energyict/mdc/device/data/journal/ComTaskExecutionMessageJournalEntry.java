package com.energyict.mdc.device.data.journal;

/**
 * Models a {@link ComTaskExecutionJournalEntry} for a simple message
 * that is logged as part of the execution of a {@link com.energyict.mdc.tasks.ComTaskExecution}}.
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