/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface JournalEntryVisitor {

    void visit(ComCommandJournalEntry entry);

    void visit(ComTaskExecutionMessageJournalEntry entry);

}