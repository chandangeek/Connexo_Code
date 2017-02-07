/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.CompletionCodeTranslationKeys;
import com.energyict.mdc.device.data.impl.tasks.HasLastComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

public class ComTaskExecutionSessionImpl extends PersistentIdObject<ComTaskExecution> implements ComTaskExecutionSession, HasId {

    public enum Fields {
        DEVICE("device"),
        SESSION("comSession"),
        COM_TASK("comTask"),
        COM_TASK_EXECUTION("comTaskExecution"),
        NUMBER_OF_BYTES_SENT("numberOfBytesSent"),
        NUMBER_OF_BYTES_READ("numberOfBytesReceived"),
        NUMBER_OF_PACKETS_SENT("numberOfPacketsSent"),
        NUMBER_OF_PACKETS_READ("numberOfPacketsReceived"),
        START_DATE("startDate"),
        STOP_DATE("stopDate"),
        SUCCESS_INDICATOR("successIndicator"),
        HIGHEST_PRIORITY_COMPLETION_CODE("highestPriorityCompletionCode"),
        HIGHEST_PRIORITY_ERROR_DESCRIPTION("highestPriorityErrorDescription"),
        STATUS("status");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Device> device = ValueReference.absent();
    private Reference<ComSession> comSession = ValueReference.absent();
    private Reference<ComTaskExecution> comTaskExecution = ValueReference.absent();
    private Reference<ComTask> comTask = ValueReference.absent();

    private long numberOfBytesSent;
    private long numberOfBytesReceived;
    private long numberOfPacketsSent;
    private long numberOfPacketsReceived;
    private Instant startDate;
    private Instant stopDate;
    private SuccessIndicator successIndicator;
    private List<ComTaskExecutionJournalEntry> comTaskExecutionJournalEntries = new ArrayList<>();

    private CompletionCode highestPriorityCompletionCode;
    private String highestPriorityErrorDescription;

    private CommunicationTaskService communicationTaskService;

    @Inject
    ComTaskExecutionSessionImpl(DataModel dataModel,
                                CommunicationTaskService communicationTaskService,
                                EventService eventService,
                                Thesaurus thesaurus) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.communicationTaskService = communicationTaskService;
    }

    static ComTaskExecutionSessionImpl from(DataModel dataModel, ComSession comSession, ComTaskExecution comTaskExecution, ComTask comTask, Device device, Range<Instant> interval, SuccessIndicator successIndicator) {
        return dataModel.getInstance(ComTaskExecutionSessionImpl.class).init(comSession, comTaskExecution, comTask, device, interval, successIndicator);
    }

    private List<ComTaskExecutionJournalEntry> getServerComTaskExecutionJournalEntries () {
         return Collections.unmodifiableList(this.comTaskExecutionJournalEntries);
    }

    /**
     * Receive notification from the owning ComSession
     * that this ComTaskExecutionSession has been saved.
     */
    void created() {
        // Make sure we have the latest version of the comTaskExecution
        communicationTaskService.findComTaskExecution(this.comTaskExecution.get().getId()).
                ifPresent(x -> ((HasLastComTaskExecutionSession) x).sessionCreated(this));
    }

    @Override
    public Device getDevice () {
        return device.get();
    }

    @Override
    public ComSession getComSession() {
        return comSession.get();
    }

    @Override
    public ComStatistics getStatistics() {
        return new ComStatisticsImpl(this.numberOfBytesSent, this.numberOfBytesReceived, this.numberOfPacketsSent, this.numberOfPacketsReceived);
    }

    void setNumberOfBytesSent(long numberOfBytesSent) {
        this.numberOfBytesSent = numberOfBytesSent;
    }

    void setNumberOfBytesReceived(long numberOfBytesReceived) {
        this.numberOfBytesReceived = numberOfBytesReceived;
    }

    void setNumberOfPacketsSent(long numberOfPacketsSent) {
        this.numberOfPacketsSent = numberOfPacketsSent;
    }

    void setNumberOfPacketsReceived(long numberOfPacketsReceived) {
        this.numberOfPacketsReceived = numberOfPacketsReceived;
    }

    @Override
    public ComTaskExecution getComTaskExecution () {
        return comTaskExecution.get();
    }

    @Override
    public ComTask getComTask() {
        return comTask.get();
    }

    @Override
    public List<ComTaskExecutionJournalEntry> getComTaskExecutionJournalEntries () {
        return this.getServerComTaskExecutionJournalEntries();
    }

    @Override
    public Finder<ComTaskExecutionJournalEntry> findComTaskExecutionJournalEntries(Set<ComServer.LogLevel> levels) {
        return DefaultFinder
                .of(ComTaskExecutionJournalEntry.class,
                         where(ComTaskExecutionJournalEntryImpl.Fields.ComTaskExecutionSession.fieldName()).isEqualTo(this)
                    .and(where(ComTaskExecutionJournalEntryImpl.Fields.LogLevel.fieldName()).in(new ArrayList<>(levels))), this.dataModel)
                .sorted(ComTaskExecutionJournalEntryImpl.Fields.timestamp.fieldName(), false);
    }

    @Override
    public Instant getStartDate() {
        return this.startDate;
    }

    @Override
    public Instant getStopDate() {
        return this.stopDate;
    }

    @Override
    public boolean endsAfter(ComTaskExecutionSession other) {
        return this.getStopDate().isAfter(other.getStopDate());
    }

    @Override
    public SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    @Override
    public ComCommandJournalEntry createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        ComCommandJournalEntryImpl journalEntry = ComCommandJournalEntryImpl.from(dataModel, this, timestamp, completionCode, errorDescription, commandDescription);
        comTaskExecutionJournalEntries.add(journalEntry);
        return journalEntry;
    }

    @Override
    public ComTaskExecutionMessageJournalEntry createComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDescription) {
        ComTaskExecutionMessageJournalEntryImpl journalEntry = ComTaskExecutionMessageJournalEntryImpl.from(this.dataModel, this, timestamp, message, errorDescription, logLevel);
        comTaskExecutionJournalEntries.add(journalEntry);
        return journalEntry;
    }

    @Override
    public CompletionCode getHighestPriorityCompletionCode() {
        return highestPriorityCompletionCode;
    }

    @Override
    public String getHighestPriorityCompletionCodeDisplayName() {
        return CompletionCodeTranslationKeys.translationFor(highestPriorityCompletionCode, getThesaurus());
    }

    @Override
    public String getHighestPriorityErrorDescription() {
        return highestPriorityErrorDescription;
    }

    @Override
    protected void doDelete() {
        //TODO automatically generated method body, provide implementation.
    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.
    }

    private ComTaskExecutionSessionImpl init(ComSession comSession, ComTaskExecution comTaskExecution, ComTask comTask, Device device, Range<Instant> interval, SuccessIndicator successIndicator) {
        this.comSession.set(comSession);
        this.comTaskExecution.set(comTaskExecution);
        this.comTask.set(comTask);
        this.device.set(device);
        this.startDate = interval.lowerEndpoint();
        this.stopDate = interval.upperEndpoint();
        this.successIndicator = successIndicator;
        return this;
    }

    void determineHighestPriorityCompletionCodeAndErrorMessage(){
        highestPriorityCompletionCode = CompletionCode.Ok; // optimistic, but this will also solve the fact that we will log ok it the loglevel was higher then INFO
        highestPriorityErrorDescription = null;
        CheckAndUpdatePriorityJournalEntryVisitor visitor = new CheckAndUpdatePriorityJournalEntryVisitor();
        this.comTaskExecutionJournalEntries.forEach(je -> je.accept(visitor));
    }

    private void checkAndUpdatePriority(ComCommandJournalEntry entry) {
        if(entry.getCompletionCode().hasPriorityOver(highestPriorityCompletionCode)){
            highestPriorityCompletionCode = entry.getCompletionCode();
            highestPriorityErrorDescription = entry.getErrorDescription();
        }
    }

    private class CheckAndUpdatePriorityJournalEntryVisitor implements JournalEntryVisitor {
        @Override
        public void visit(ComCommandJournalEntry entry) {
            checkAndUpdatePriority(entry);
        }

        @Override
        public void visit(ComTaskExecutionMessageJournalEntry entry) {
            // has no CompletionCode, so do nothing
        }
    }

}