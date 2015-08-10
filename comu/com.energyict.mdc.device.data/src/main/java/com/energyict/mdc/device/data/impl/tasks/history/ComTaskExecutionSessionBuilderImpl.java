package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
* Copyrights EnergyICT
* Date: 5/05/2014
* Time: 9:59
*/
class ComTaskExecutionSessionBuilderImpl implements ComTaskExecutionSessionBuilder {
    private final LongCounter sentBytes = Counters.newStrictLongCounter();
    private final LongCounter receivedBytes = Counters.newStrictLongCounter();
    private final LongCounter sentPackets = Counters.newStrictLongCounter();
    private final LongCounter receivedPackets = Counters.newStrictLongCounter();
    private final ComTaskExecution comTaskExecution;
    private final ComTask comTask;
    private final Device device;
    private final Instant startDate;
    private Instant stopDate;
    private ComTaskExecutionSession.SuccessIndicator successIndicator;
    private final ComSessionBuilder parentBuilder;
    private List<JournalEntryBuilder> journalEntryBuilders = new ArrayList<>();

    ComTaskExecutionSessionBuilderImpl(ComSessionBuilder parentBuilder, ComTaskExecution comTaskExecution, ComTask comTask, Device device, Instant startDate) {
        this.parentBuilder = parentBuilder;
        this.comTaskExecution = comTaskExecution;
        this.comTask = comTask;
        this.device = device;
        this.startDate = startDate;
    }

    @Override
    public ComSessionBuilder add(Instant stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.stopDate = stopDate;
        this.successIndicator = successIndicator;
        return parentBuilder;
    }

    @Override
    public ComTaskExecutionSessionBuilder addReceivedBytes(long numberOfBytes) {
        receivedBytes.add(numberOfBytes);
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addReceivedPackets(long numberOfPackets) {
        receivedPackets.add(numberOfPackets);
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addSentBytes(long numberOfBytes) {
        sentBytes.add(numberOfBytes);
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addSentPackets(long numberOfPackets) {
        sentPackets.add(numberOfPackets);
        return this;
    }

    ComTaskExecutionSessionImpl addTo(ComSessionImpl comSession) {
        ComTaskExecutionSessionImpl comTaskExecutionSession = comSession.createComTaskExecutionSession(comTaskExecution, comTask, comTaskExecution.getDevice(), Range.closed(startDate, stopDate), successIndicator);
        for (JournalEntryBuilder journalEntryBuilder : journalEntryBuilders) {
            journalEntryBuilder.build(comTaskExecutionSession);
        }
        comTaskExecutionSession.determineHighestPriorityCompletionCodeAndErrorMessage();
        return comTaskExecutionSession;
    }

    @Override
    public ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String commandDescription) {
        journalEntryBuilders.add(new ComCommandJournalEntryBuilder(timestamp, completionCode, commandDescription));
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription) {
        journalEntryBuilders.add(new ComCommandJournalEntryBuilder(timestamp, completionCode, errorDesciption, commandDescription));
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDesciption) {
        journalEntryBuilders.add(new ComTaskExecutionMessageJournalEntryBuilder(timestamp, logLevel, message, errorDesciption));
        return this;
    }

    @Override
    public void updateSuccessIndicator(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    long getReceivedBytes() {
        return receivedBytes.getValue();
    }

    long getSentBytes() {
        return sentBytes.getValue();
    }

    long getReceivedPackets() {
        return receivedPackets.getValue();
    }

    long getSentPackets() {
        return sentPackets.getValue();
    }

    boolean isFor(ComTaskExecution comTaskExecution) {
        return this.comTaskExecution.getId() == comTaskExecution.getId();
    }

    private interface JournalEntryBuilder {
        ComTaskExecutionJournalEntry build(ComTaskExecutionSession session);
    }

    private class ComCommandJournalEntryBuilder implements JournalEntryBuilder {

        private final Instant timestamp;
        private final CompletionCode completionCode;
        private final String errorDescription;
        private final String commandDescription;

        private ComCommandJournalEntryBuilder(Instant timestamp, CompletionCode completionCode, String commandDescription) {
            this.timestamp = timestamp;
            this.completionCode = completionCode;
            this.errorDescription = null;
            this.commandDescription = commandDescription;
        }

        private ComCommandJournalEntryBuilder(Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
            this.timestamp = timestamp;
            this.completionCode = completionCode;
            this.errorDescription = errorDescription;
            this.commandDescription = commandDescription;
        }

        @Override
        public ComTaskExecutionJournalEntry build(ComTaskExecutionSession session) {
            return session.createComCommandJournalEntry(timestamp, completionCode, errorDescription, commandDescription);
        }
    }

    private class ComTaskExecutionMessageJournalEntryBuilder implements JournalEntryBuilder {

        private final Instant timestamp;
        private final ComServer.LogLevel logLevel;
        private final String message;
        private final String errorDescription;

        private ComTaskExecutionMessageJournalEntryBuilder(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDescription) {
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.message = message;
            this.errorDescription = errorDescription;
        }

        @Override
        public ComTaskExecutionJournalEntry build(ComTaskExecutionSession session) {
            return session.createComTaskExecutionMessageJournalEntry(this.timestamp, this.logLevel, this.message, this.errorDescription);
        }

    }

}