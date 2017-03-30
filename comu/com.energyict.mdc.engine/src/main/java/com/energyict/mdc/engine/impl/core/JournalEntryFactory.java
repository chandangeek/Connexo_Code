/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;

import java.time.Instant;

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

    void createJournalEntry(ComServer.LogLevel logLevel, String message);

    void createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

}