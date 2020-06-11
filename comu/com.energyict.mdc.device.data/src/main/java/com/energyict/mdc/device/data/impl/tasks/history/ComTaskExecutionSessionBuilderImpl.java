/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.impl.ComTaskDefinedByUserImpl;

import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ComTaskExecutionSessionBuilderImpl implements ComTaskExecutionSessionBuilder {
    private LongCounter sentBytes = Counters.newStrictLongCounter();
    private LongCounter receivedBytes = Counters.newStrictLongCounter();
    private LongCounter sentPackets = Counters.newStrictLongCounter();
    private LongCounter receivedPackets = Counters.newStrictLongCounter();
    private ComTaskExecution comTaskExecution;
    private ComTask comTask;
    private Instant startDate;
    private Instant stopDate;
    private ComTaskExecutionSession.SuccessIndicator successIndicator;
    @XmlTransient
    private ComSessionBuilder parentBuilder;
    private List<JournalEntryBuilder> journalEntryBuilders = new ArrayList<>();

    public ComTaskExecutionSessionBuilderImpl() {
        super();
    }

    ComTaskExecutionSessionBuilderImpl(ComSessionBuilder parentBuilder, ComTaskExecution comTaskExecution, ComTask comTask, Instant startDate) {
        this.parentBuilder = parentBuilder;
        this.comTaskExecution = comTaskExecution;
        this.comTask = comTask;
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

    @Override
    public ComTaskExecutionSessionBuilder resetReceivedBytes() {
        receivedBytes = Counters.newStrictLongCounter();
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder resetReceivedPackets() {
        receivedPackets = Counters.newStrictLongCounter();
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder resetSentBytes() {
        sentBytes = Counters.newStrictLongCounter();
        return this;
    }

    @Override
    public ComTaskExecutionSessionBuilder resetSentPackets() {
        sentPackets = Counters.newStrictLongCounter();
        return this;
    }

    @XmlElement(type = ComTaskExecutionImpl.class)
    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    @XmlElement(type = ComTaskDefinedByUserImpl.class)
    public ComTask getComTask() {
        return comTask;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getStopDate() {
        return stopDate;
    }

    public void setComTaskExecution(ComTaskExecution comTaskExecution) {
        this.comTaskExecution = comTaskExecution;
    }

    public void setComTask(ComTask comTask) {
        this.comTask = comTask;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public void setStopDate(Instant stopDate) {
        this.stopDate = stopDate;
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

    public ComTaskExecutionSession.SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    @Override
    public void setSuccessIndicator(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    public long getReceivedBytes() {
        return receivedBytes.getValue();
    }

    public long getSentBytes() {
        return sentBytes.getValue();
    }

    public long getReceivedPackets() {
        return receivedPackets.getValue();
    }

    public long getSentPackets() {
        return sentPackets.getValue();
    }

    public void setSentBytes(long sentBytes) {
        this.sentBytes = Counters.newStrictLongCounter();
        this.sentBytes.add(sentBytes);
    }

    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = Counters.newStrictLongCounter();
        this.receivedBytes.add(receivedBytes);
    }

    public void setSentPackets(long sentPackets) {
        this.sentPackets = Counters.newStrictLongCounter();
        this.sentPackets.add(sentPackets);
    }

    public void setReceivedPackets(long receivedPackets) {
        this.receivedPackets = Counters.newStrictLongCounter();
        this.receivedPackets.add(receivedPackets);
    }

    boolean isFor(ComTaskExecution comTaskExecution) {
        return this.comTaskExecution.getId() == comTaskExecution.getId();
    }

    public List<JournalEntryBuilder> getJournalEntryBuilders() {
        return journalEntryBuilders;
    }

    public void setJournalEntryBuilders(List<JournalEntryBuilder> journalEntryBuilders) {
        this.journalEntryBuilders = journalEntryBuilders;
    }

    public static class JournalEntryBuilder {

        public JournalEntryBuilder() {
        }

        protected Instant timestamp;
        protected CompletionCode completionCode;
        protected String errorDescription;
        protected String commandDescription;

        protected ComServer.LogLevel logLevel;
        protected String message;

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public CompletionCode getCompletionCode() {
            return completionCode;
        }

        public void setCompletionCode(CompletionCode completionCode) {
            this.completionCode = completionCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public void setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
        }

        public String getCommandDescription() {
            return commandDescription;
        }

        public void setCommandDescription(String commandDescription) {
            this.commandDescription = commandDescription;
        }

        public ComServer.LogLevel getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(ComServer.LogLevel logLevel) {
            this.logLevel = logLevel;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        ComTaskExecutionJournalEntry build(ComTaskExecutionSession session) {
            if (completionCode != null) {
                return session.createComCommandJournalEntry(timestamp, completionCode, errorDescription, commandDescription);
            } else {
                return session.createComTaskExecutionMessageJournalEntry(this.timestamp, this.logLevel, this.message, this.errorDescription);
            }
        }
    }

    private class ComCommandJournalEntryBuilder extends JournalEntryBuilder {

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

    private class ComTaskExecutionMessageJournalEntryBuilder extends JournalEntryBuilder {

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