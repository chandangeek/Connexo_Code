package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.HasLastComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Range;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 25/04/12
 * Time: 17:06
 */
public class ComTaskExecutionSessionImpl extends PersistentIdObject<ComTaskExecution> implements ComTaskExecutionSession, HasId {

    public enum Fields {
        DEVICE("device"),
        SESSION("comSession"),
        COM_TASK("comTask"),
        COM_TASK_EXECUTION("comTaskExecution"),
        STATISTICS("statistics"),
        START_DATE("startDate"),
        STOP_DATE("stopDate"),
        SUCCESS_INDICATOR("successIndicator"),
        HIGHEST_PRIORITY_COMPLETION_CODE("highestPriorityCompletionCode"),
        HIGHEST_PRIORITY_ERROR_DESCRIPTION("highestPriorityErrorDescription"),
        STATUS("status"),
        MODIFICATION_DATE("modDate");

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
    private Reference<ComStatistics> statistics = ValueReference.absent();
    private Reference<ComTaskExecution> comTaskExecution = ValueReference.absent();
    private Reference<ComTask> comTask = ValueReference.absent();

    private Instant startDate;
    private Instant stopDate;
    private SuccessIndicator successIndicator;
    private List<ComTaskExecutionJournalEntry> comTaskExecutionJournalEntries = new ArrayList<>();

    private CompletionCode highestPriorityCompletionCode;
    private String highestPriorityErrorDescription;
    private Instant modDate;

    @Inject
    ComTaskExecutionSessionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
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
        HasLastComTaskExecutionSession comTaskExecution = (HasLastComTaskExecutionSession) this.comTaskExecution.get();
        comTaskExecution.sessionCreated(this);
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
        return statistics.get();
    }

    @Override
    public ComTaskExecution getComTaskExecution () {
        return comTaskExecution.get();
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

    @Override
    public void setStatistics(ComStatistics stats) {
        statistics.set(stats);
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
        highestPriorityCompletionCode = null;
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