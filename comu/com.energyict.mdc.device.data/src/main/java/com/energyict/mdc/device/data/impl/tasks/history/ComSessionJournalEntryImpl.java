/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.engine.config.ComServer;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;

/**
 * Provides an implementation for the {@link ComSessionJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2012-07-27 (17:16)
 */
public class ComSessionJournalEntryImpl extends PersistentIdObject<ComSessionJournalEntry> implements ComSessionJournalEntry {

    private Reference<ComSession> comSession = ValueReference.absent();
    private Instant timestamp;
    private ComServer.LogLevel logLevel;
    private String message;
    private String stackTrace;
    private Instant modDate;

    @Inject
    ComSessionJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComSessionJournalEntry.class, dataModel, eventService, thesaurus);
    }

    @Override
    public ComSession getComSession () {
        return comSession.get();
    }

    @Override
    public Instant getTimestamp () {
        return this.timestamp;
    }

    @Override
    public ComServer.LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public String getMessage () {
        return message;
    }

    @Override
    public String getStackTrace () {
        return this.stackTrace;
    }

    @Override
    protected void doDelete() {
        this.dataModel.remove(this);
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate
    }

    public static ComSessionJournalEntryImpl from(DataModel dataModel, ComSessionImpl comSession, Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause) {
        ComSessionJournalEntryImpl entry = dataModel.getInstance(ComSessionJournalEntryImpl.class);
        return entry.init(comSession, timestamp, logLevel, message, cause);
    }

    private ComSessionJournalEntryImpl init(ComSessionImpl comSession, Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause) {
        this.comSession.set(comSession);
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.message = message;
        this.stackTrace = StackTracePrinter.print(cause);
        return this;
    }

    /**
     * Provides printing services for stack traces of exceptions
     * that cause journal entries to be created.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2012-08-07 (17:20)
     */
    private enum StackTracePrinter {
        ;

        /**
         * Prints the stacktrace for the Throwable.
         *
         * @param thrown The Throwable (can be <code>null</code>).
         * @return The stacktrace or <code>null</code> if there was not Throwable.
         */
        public static String print (Throwable thrown) {
            if (thrown == null) {
                return null;
            }
            else {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                thrown.printStackTrace(printWriter);
                return writer.toString();
            }
        }

    }
}