/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import com.energyict.mdc.common.comserver.ComServer;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;

@ConsumerType
public interface ComTaskExecutionJournalEntry {
    ComTaskExecutionSession getComTaskExecutionSession();

    Instant getTimestamp();

    String getErrorDescription();

    void accept(JournalEntryVisitor visitor);

    /**
     * Gets the level at which this message journal entry was logged.
     *
     * @return The LogLevel
     */
    ComServer.LogLevel getLogLevel();
}
