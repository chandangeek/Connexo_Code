/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface JournalEntryVisitor {

    void visit(ComCommandJournalEntry entry);

    void visit(ComTaskExecutionMessageJournalEntry entry);

}