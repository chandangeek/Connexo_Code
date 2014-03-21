package com.energyict.mdc.device.data.journal;

/**
 * Models a {@link ComTaskExecutionJournalEntry} for a command that was executed
 * against the device in the context of a {@link com.energyict.mdc.device.data.ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (09:13)
 */
public interface ComCommandJournalEntry extends ComTaskExecutionJournalEntry {

    /**
     * Gets the {@link CompletionCode} of the command that was executed.
     *
     * @return The CompletionCode
     */
    public CompletionCode getCompletionCode ();

    /**
     * Gets the human readable description of the command that was executed.
     *
     * @return The human readable description of the command that was executed
     */
    public String getCommandDescription ();

}