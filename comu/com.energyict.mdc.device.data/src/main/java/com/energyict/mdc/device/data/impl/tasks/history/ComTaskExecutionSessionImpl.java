package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    private UtcInstant startDate;
    private UtcInstant stopDate;
    private SuccessIndicator successIndicator;
    private List<ComTaskExecutionJournalEntry> comTaskExecutionJournalEntries = new ArrayList<>();

    private CompletionCode highestPriorityCompletionCode;
    private String highestPriorityErrorDescription;
    private Date modDate;

    @Inject
    ComTaskExecutionSessionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
    }

    static ComTaskExecutionSessionImpl from(DataModel dataModel, ComSession comSession, ComTaskExecution comTaskExecution, Device device, Interval interval, SuccessIndicator successIndicator) {
        return dataModel.getInstance(ComTaskExecutionSessionImpl.class).init(comSession, comTaskExecution, device, interval, successIndicator);
    }

     private List<ComTaskExecutionJournalEntry> getServerComTaskExecutionJournalEntries () {
         return Collections.unmodifiableList(this.comTaskExecutionJournalEntries);
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
        List<ComTaskExecutionJournalEntry> journalEntries = new ArrayList<ComTaskExecutionJournalEntry>();
        for (ComTaskExecutionJournalEntry journalEntry : this.getServerComTaskExecutionJournalEntries()) {
            journalEntries.add(journalEntry);
        }
        return journalEntries;
    }

    @Override
    public Date getStartDate() {
        return startDate.toDate();
    }

    @Override
    public Date getStopDate() {
        return stopDate.toDate();
    }

    @Override
    public SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    @Override
    public ComCommandJournalEntry createComCommandJournalEntry(Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        ComCommandJournalEntryImpl journalEntry = ComCommandJournalEntryImpl.from(dataModel, this, timestamp, completionCode, errorDescription, commandDescription);
        comTaskExecutionJournalEntries.add(journalEntry);
        return journalEntry;
    }

    @Override
    public ComTaskExecutionMessageJournalEntry createComTaskExecutionMessageJournalEntry(Date timestamp, String errorDescription, String message) {
        ComTaskExecutionMessageJournalEntryImpl journalEntry = ComTaskExecutionMessageJournalEntryImpl.from(dataModel, this, timestamp, errorDescription, message);
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

    private ComTaskExecutionSessionImpl init(ComSession comSession, ComTaskExecution comTaskExecution, Device device, Interval interval, SuccessIndicator successIndicator) {
        this.comSession.set(comSession);
        this.comTaskExecution.set(comTaskExecution);
        this.device.set(device);
        this.startDate = new UtcInstant(interval.getStart());
        this.stopDate = new UtcInstant(interval.getEnd());
        this.successIndicator = successIndicator;
        return this;
    }

    void determineHighestPriorityCompletionCodeAndErrorMessage(){
        highestPriorityCompletionCode = null;
        highestPriorityErrorDescription = null;
        for (ComTaskExecutionJournalEntry comTaskExecutionJournalEntry : comTaskExecutionJournalEntries) {
            comTaskExecutionJournalEntry.accept(new JournalEntryVisitor() {
                @Override
                public void visit(ComCommandJournalEntry entry) {
                    checkAndUpdatePriority(entry);
                }

                @Override
                public void visit(ComTaskExecutionMessageJournalEntry entry) {
                    // has no CompletionCode, so do nothing
                }
            });
        }
    }

    private void checkAndUpdatePriority(ComCommandJournalEntry entry) {
        if(entry.getCompletionCode().hasPriorityOver(highestPriorityCompletionCode)){
            highestPriorityCompletionCode = entry.getCompletionCode();
            highestPriorityErrorDescription = entry.getErrorDescription();
        }
    }

}