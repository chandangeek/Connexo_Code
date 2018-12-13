/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComServer;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Map;

public abstract class ComTaskExecutionJournalEntryImpl<T extends ComTaskExecutionJournalEntry> extends PersistentIdObject<T> implements ComTaskExecutionJournalEntry {

    public static long ComCommandJournalEntryImplDiscriminator = 0;
    public static long ComTaskExecutionMessageJournalEntryImplDiscriminator = 1;

    public static final Map<String, Class<? extends ComTaskExecutionJournalEntry>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComTaskExecutionJournalEntry>>of(
                    String.valueOf(ComCommandJournalEntryImplDiscriminator), ComCommandJournalEntryImpl.class,
                    String.valueOf(ComTaskExecutionMessageJournalEntryImplDiscriminator), ComTaskExecutionMessageJournalEntryImpl.class);

    enum Fields {
        ComTaskExecutionSession("comTaskExecutionSession"),
        LogLevel("logLevel"),
        timestamp("timestamp");
        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private Reference<ComTaskExecutionSession> comTaskExecutionSession = ValueReference.absent();
    private Instant timestamp;
    private ComServer.LogLevel logLevel;
    private String errorDescription;
    private Instant modDate;

    ComTaskExecutionJournalEntryImpl(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    protected void init(Instant timestamp, ComServer.LogLevel logLevel, String errorDescription) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.errorDescription = errorDescription;
    }

    @Override
    public ComTaskExecutionSession getComTaskExecutionSession () {
        return this.comTaskExecutionSession.get();
    }

    protected void setComTaskExecutionSession(ComTaskExecutionSession comTaskExecutionSession) {
        this.comTaskExecutionSession.set(comTaskExecutionSession);
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
    public String getErrorDescription () {
        return errorDescription;
    }

}