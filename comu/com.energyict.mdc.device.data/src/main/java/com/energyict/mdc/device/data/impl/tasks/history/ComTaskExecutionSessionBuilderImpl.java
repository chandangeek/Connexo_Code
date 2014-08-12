package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.ArrayList;
import java.util.Date;
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
    private final Device device;
    private final Date startDate;
    private Date stopDate;
    private ComTaskExecutionSession.SuccessIndicator successIndicator;
    private final ComSessionBuilder parentBuilder;
    private List<JournalEntryBuilder> journalEntryBuilders = new ArrayList<>();

    ComTaskExecutionSessionBuilderImpl(ComSessionBuilder parentBuilder, ComTaskExecution comTaskExecution, Device device, Date startDate) {
        this.parentBuilder = parentBuilder;
        this.comTaskExecution = comTaskExecution;
        this.device = device;
        this.startDate = startDate;
    }

    @Override
    public ComSessionBuilder add(Date stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator) {
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

    ComTaskExecutionSession addTo(ComSessionImpl comSession) {
        ComTaskExecutionSessionImpl comTaskExecutionSession = comSession.createComTaskExecutionSession(comTaskExecution, device, new Interval(startDate, stopDate), successIndicator);
        for (JournalEntryBuilder journalEntryBuilder : journalEntryBuilders) {
            journalEntryBuilder.build(comTaskExecutionSession);
        }
        comTaskExecutionSession.determineHighestPriorityCompletionCodeAndErrorMessage();
        return comTaskExecutionSession;
    }

    @Override
    public ComTaskExecutionSessionBuilder addComCommandJournalEntry(Date timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription) {
        journalEntryBuilders.add(new ComCommandJournalEntryBuilder(timestamp, completionCode, errorDesciption, commandDescription));
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Date timestamp, String errorDesciption, String message) {
        journalEntryBuilders.add(new ComTaskExecutionMessageJournalEntryBuilder(timestamp, errorDesciption, message));
        return this;
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

        private final Date timestamp;
        private final CompletionCode completionCode;
        private final String errorDescription;
        private final String commandDescription;

        private ComCommandJournalEntryBuilder(Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
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

        private final Date timestamp;
        private final String errorDescription;
        private final String message;

        private ComTaskExecutionMessageJournalEntryBuilder(Date timestamp, String errorDescription, String message) {
            this.timestamp = timestamp;
            this.errorDescription = errorDescription;
            this.message = message;
        }

        @Override
        public ComTaskExecutionJournalEntry build(ComTaskExecutionSession session) {
            return session.createComTaskExecutionMessageJournalEntry(timestamp, errorDescription, message);
        }
    }
}
