/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.common.tasks.history.JournalEntryVisitor;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ComTaskExecutionMessageJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComTaskExecutionMessageJournalEntryImpl
        extends ComTaskExecutionJournalEntryImpl<ComTaskExecutionMessageJournalEntry>
        implements ComTaskExecutionMessageJournalEntry {

    private String message;

    @Override
    public String getMessage () {
        return this.message;
    }

    @Override
    public void accept(JournalEntryVisitor visitor) {
        visitor.visit(this);
    }

    public static ComTaskExecutionMessageJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSessionImpl comTaskExecutionSession, Instant timestamp, String message, String errorDescription, ComServer.LogLevel logLevel) {
        return dataModel.getInstance(ComTaskExecutionMessageJournalEntryImpl.class).init(timestamp, comTaskExecutionSession, logLevel, message, errorDescription);
    }

    private ComTaskExecutionMessageJournalEntryImpl init(Instant timestamp, ComTaskExecutionSessionImpl comTaskExecutionSession, ComServer.LogLevel logLevel, String message, String errorDescription) {
        this.init(comTaskExecutionSession, timestamp, logLevel, errorDescription);
        this.message = message;
        return this;
    }

}
