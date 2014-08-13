package com.energyict.mdc.device.data.tasks.history;

/**
 * Copyrights EnergyICT
 * Date: 8/05/2014
 * Time: 16:20
 */
public interface JournalEntryVisitor {

    void visit(ComCommandJournalEntry entry);

    void visit(ComTaskExecutionMessageJournalEntry entry);
}
