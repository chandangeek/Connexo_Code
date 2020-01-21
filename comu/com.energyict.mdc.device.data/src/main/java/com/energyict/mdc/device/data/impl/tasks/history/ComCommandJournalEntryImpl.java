/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.common.tasks.history.JournalEntryVisitor;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ComCommandJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComCommandJournalEntryImpl extends ComTaskExecutionJournalEntryImpl<ComCommandJournalEntry> implements ComCommandJournalEntry {

    private CompletionCode completionCode;
    private String commandDescription;

    @Override
    public CompletionCode getCompletionCode () {
        return completionCode;
    }

    @Override
    public String getCommandDescription () {
        return this.commandDescription;
    }

    @Override
    public void accept(JournalEntryVisitor visitor) {
        visitor.visit(this);
    }

    public static ComCommandJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSession comTaskExecutionSession, Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        ComCommandJournalEntryImpl instance = dataModel.getInstance(ComCommandJournalEntryImpl.class);
        return instance.init(comTaskExecutionSession, timestamp, completionCode, errorDescription, commandDescription);
    }

    private ComCommandJournalEntryImpl init(ComTaskExecutionSession comTaskExecutionSession, Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        this.init(comTaskExecutionSession, timestamp, this.logLevelFor(completionCode), errorDescription);
        this.completionCode = completionCode;
        this.commandDescription = commandDescription;
        return this;
    }

    private ComServer.LogLevel logLevelFor(CompletionCode completionCode) {
        switch (completionCode) {
            case NotExecuted:   // Intentional fall-through
            case Ok: {
                return ComServer.LogLevel.INFO;
            }
            case ConfigurationWarning: {
                return ComServer.LogLevel.WARN;
            }
            case ProtocolError:   // Intentional fall-through
            case TimeError:   // Intentional fall-through
            case ConfigurationError:   // Intentional fall-through
            case IOError:   // Intentional fall-through
            case InitError:   // Intentional fall-through
            case TimeoutError:   // Intentional fall-through
            case UnexpectedError:   // Intentional fall-through
            case ConnectionError: {
                return ComServer.LogLevel.ERROR;
            }
            default: {
                return ComServer.LogLevel.INFO;
            }
        }
    }

}
