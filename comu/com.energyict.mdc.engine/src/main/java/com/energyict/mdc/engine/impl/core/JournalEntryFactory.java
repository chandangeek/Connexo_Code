package com.energyict.mdc.engine.impl.core;

/**
 * Defines the behavior of a component that will create
 * {@link com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}
 * for logging purposes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-20 (09:57)
 */
public interface JournalEntryFactory {

    public void createJournalEntry(String message);

}