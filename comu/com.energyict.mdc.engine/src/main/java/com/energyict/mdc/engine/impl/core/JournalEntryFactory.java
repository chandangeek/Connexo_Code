/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.common.tasks.history.CompletionCode;

import java.time.Instant;

/**
 * Defines the behavior of a component that will create
 * {@link ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}
 * for logging purposes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-20 (09:57)
 */
public interface JournalEntryFactory {

    void createJournalEntry(ComServer.LogLevel logLevel, String message);

    void createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

}