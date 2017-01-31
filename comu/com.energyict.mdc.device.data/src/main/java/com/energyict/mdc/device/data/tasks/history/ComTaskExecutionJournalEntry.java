/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.engine.config.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface ComTaskExecutionJournalEntry extends HasId {

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