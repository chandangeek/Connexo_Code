package com.energyict.mdc.device.data.tasks.history;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 8/05/2014
 * Time: 16:20
 */
@ProviderType
public interface JournalEntryVisitor {

    void visit(ComCommandJournalEntry entry);

    void visit(ComTaskExecutionMessageJournalEntry entry);
}
