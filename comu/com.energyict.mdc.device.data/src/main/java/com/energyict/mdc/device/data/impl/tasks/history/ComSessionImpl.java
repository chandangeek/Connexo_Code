package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.TaskExecutionSummary;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import org.joda.time.Duration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides an implementation for the {@link ComSession} interface.
 * <p/>
 * User: sva
 * Date: 10/05/12
 * Time: 15:31
 */
public class ComSessionImpl implements ComSession {

    public enum Fields {
        CONNECTION_TASK("connectionTask"),
        COMPORT("comPort"),
        COMPORT_POOL("comPortPool"),
        STATISTICS("statistics"),
        START_DATE("startDate"),
        STOP_DATE("stopDate"),
        TOTAL_TIME("totalMillis"),
        CONNECT_MILLIS("connectMillis"),
        TALK_MILLIS("talkMillis"),
        STORE_MILLIS("storeMillis"),
        SUCCESS_INDICATOR("successIndicator"),
        TASK_SUCCESS_COUNT("taskSuccessCount"),
        TASK_FAILURE_COUNT("taskFailureCount"),
        TASK_NOT_EXECUTED_COUNT("taskNotExecutedCount"),
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

    private long id;

    private DataModel dataModel;

    private Reference<ConnectionTask> connectionTask = ValueReference.absent();

    private Reference<ComPort> comPort = ValueReference.absent();

    private Reference<ComPortPool> comPortPool = ValueReference.absent();

    private Reference<ComStatistics> statistics = ValueReference.absent();

    private UtcInstant startDate;
    private UtcInstant stopDate;
    private long totalMillis;
    private long connectMillis;
    private long talkMillis;
    private long storeMillis;

    private boolean status;
    private ComSession.SuccessIndicator successIndicator;
    private int taskSuccessCount;
    private int taskFailureCount;
    private int taskNotExecutedCount;
    private Date modDate;

    private List<ComSessionJournalEntry> journalEntries = new ArrayList<>();
    private List<ComTaskExecutionSession> comTaskExecutionSessions = new ArrayList<>();

    @Inject
    ComSessionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    void setStatistics(ComStatistics statistics) {
        this.statistics.set(statistics);
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return connectionTask.get();
    }

    @Override
    public ComPort getComPort() {
        return comPort.get();
    }

    @Override
    public ComPortPool getComPortPool() {
        return comPortPool.get();
    }

    @Override
    public ComStatistics getStatistics() {
        return statistics.get();
    }

    @Override
    public List<ComSessionJournalEntry> getJournalEntries() {
        return Collections.unmodifiableList(this.journalEntries);
    }

    @Override
    public List<ComTaskExecutionSession> getComTaskExecutionSessions() {
        return Collections.unmodifiableList(comTaskExecutionSessions);
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
    public Duration getTotalDuration() {
        return Duration.millis(totalMillis);
    }

    @Override
    public Duration getConnectDuration() {
        return new Duration(connectMillis);
    }

    @Override
    public Duration getTalkDuration() {
        return new Duration(talkMillis);
    }

    @Override
    public Duration getStoreDuration() {
        return new Duration(storeMillis);
    }

    @Override
    public boolean wasSuccessful() {
        return this.status;
    }

    @Override
    public ComSession.SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    @Override
    public TaskExecutionSummary getTaskExecutionSummary() {
        return this;
    }

    @Override
    public int getNumberOfSuccessFulTasks() {
        return this.taskSuccessCount;
    }

    @Override
    public int getNumberOfFailedTasks() {
        return this.taskFailureCount;
    }

    @Override
    public int getNumberOfPlannedButNotExecutedTasks() {
        return this.taskNotExecutedCount;
    }

    void setTalkDuration(Duration duration) {
        this.talkMillis = duration.getMillis();
    }

    void setStoreDuration(Duration duration) {
        this.storeMillis = duration.getMillis();
    }

    void setSuccessful(boolean successful) {
        this.status = successful;
    }

    void setStopTime(Date stopTime) {
        this.setStopTime(new UtcInstant(stopTime));
    }

    private void setStopTime(UtcInstant stopTime) {
        this.stopDate = stopTime;
    }

    void setSuccessIndicator(SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    void setSuccessfulTasks(int successfulTasks) {
        this.taskSuccessCount = successfulTasks;
    }

    void setNotExecutedTasks(int notExecutedTasks) {
        this.taskNotExecutedCount = notExecutedTasks;
    }

    void setFailedTasks(int failedTasks) {
        this.taskFailureCount = failedTasks;
    }

    void setConnectDuration(Duration duration) {
        this.connectMillis = duration.getMillis();
    }

    @Override
    public ComTaskExecutionSessionImpl createComTaskExecutionSession(ComTaskExecution comTaskExecution, Device device, Interval interval, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        ComTaskExecutionSessionImpl executionSession = ComTaskExecutionSessionImpl.from(dataModel, this, comTaskExecution, device, interval, successIndicator);
        comTaskExecutionSessions.add(executionSession);
        return executionSession;
    }

    @Override
    public ComSessionJournalEntry createJournalEntry(Date timestamp, String message, Throwable cause) {
        ComSessionJournalEntryImpl entry = ComSessionJournalEntryImpl.from(dataModel, this, timestamp, message, cause);
        journalEntries.add(entry);
        return entry;
    }

    @Override
    public void save() {
        this.calculateTotalMillis();
        if (this.id == 0) {
            this.dataModel.mapper(ComSession.class).persist(this);
        }
        else {
            this.dataModel.mapper(ComSession.class).update(this);
        }
    }

    private void calculateTotalMillis() {
        if (this.startDate != null && this.stopDate != null) {
            this.totalMillis = (this.stopDate.getTime() - this.startDate.getTime());
        }
    }

    public static ComSessionImpl from(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
        return dataModel.getInstance(ComSessionImpl.class).init(connectionTask, comPortPool, comPort, new UtcInstant(startTime));
    }

    private ComSessionImpl init(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, UtcInstant startTime) {
        this.connectionTask.set(connectionTask);
        this.comPortPool.set(comPortPool);
        this.comPort.set(comPort);
        this.startDate = startTime;
        return this;
    }

}