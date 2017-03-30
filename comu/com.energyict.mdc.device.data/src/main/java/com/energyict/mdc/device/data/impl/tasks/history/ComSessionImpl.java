/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ComSessionSuccessIndicatorTranslationKeys;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.HasLastComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.TaskExecutionSummary;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link ComSession} interface.
 * <p/>
 * User: sva
 * Date: 10/05/12
 * Time: 15:31
 */
@LiteralSql
public class ComSessionImpl implements ComSession {

    public enum Fields {
        CONNECTION_TASK("connectionTask"),
        COMPORT("comPort"),
        COMPORT_POOL("comPortPool"),
        NUMBER_OF_BYTES_SENT("numberOfBytesSent"),
        NUMBER_OF_BYTES_READ("numberOfBytesReceived"),
        NUMBER_OF_PACKETS_SENT("numberOfPacketsSent"),
        NUMBER_OF_PACKETS_READ("numberOfPacketsReceived"),
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
        STATUS("status");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    private final DataModel dataModel;
    private Thesaurus thesaurus;
    private final ConnectionTaskService connectionTaskService;
    private Reference<ConnectionTask> connectionTask = ValueReference.absent();
    private Reference<ComPort> comPort = ValueReference.absent();
    private Reference<ComPortPool> comPortPool = ValueReference.absent();

    private Instant startDate;
    private Instant stopDate;
    private long totalMillis;
    private long connectMillis;
    private long talkMillis;
    private long storeMillis;
    private long numberOfBytesSent;
    private long numberOfBytesReceived;
    private long numberOfPacketsSent;
    private long numberOfPacketsReceived;

    private boolean status;
    private ComSession.SuccessIndicator successIndicator;
    private int taskSuccessCount;
    private int taskFailureCount;
    private int taskNotExecutedCount;

    private List<ComSessionJournalEntry> journalEntries = new ArrayList<>();
    private List<ComTaskExecutionSession> comTaskExecutionSessions = new ArrayList<>();

    @Inject
    ComSessionImpl(DataModel dataModel, ConnectionTaskService connectionTaskService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.connectionTaskService = connectionTaskService;
        this.thesaurus = thesaurus;
    }

    @Override
    public long getId() {
        return id;
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
    public List<ComSessionJournalEntry> getJournalEntries() {
        return Collections.unmodifiableList(this.journalEntries);
    }

    @Override
    public Finder<ComSessionJournalEntry> getJournalEntries(Set<ComServer.LogLevel> levels) {
        return DefaultFinder.of(
                ComSessionJournalEntry.class,
                where("logLevel").in(new ArrayList<>(levels)).and(where("comSession").isEqualTo(this)),
                this.dataModel).
                defaultSortColumn("timestamp", false);
    }

    @Override
    public Finder<ComTaskExecutionJournalEntry> getCommunicationTaskJournalEntries(Set<ComServer.LogLevel> levels) {
        return DefaultFinder.of(
                ComTaskExecutionJournalEntry.class,
                where("comTaskExecutionSession.comSession").isEqualTo(this)
                        .and(where("logLevel").in(new ArrayList<>(levels))),
                this.dataModel,
                ComTaskExecutionSession.class).
                defaultSortColumn("timestamp", false);
    }

    @Override
    public List<CombinedLogEntry> getAllLogs(Set<ComServer.LogLevel> levels, int start, int pageSize) {
        SqlBuilder sqlBuilder = new SqlBuilder("select '-1', timestamp, loglevel, message, to_clob(''), 0, stacktrace from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSIONJOURNALENTRY.name());
        sqlBuilder.append(" where comsession =");
        sqlBuilder.addLong(this.getId());
        sqlBuilder.append("and loglevel in (");
        this.appendLogLevels(levels, sqlBuilder);
        sqlBuilder.append(") union all select cteje.discriminator, cteje.timestamp, cteje.loglevel, cteje.COMMANDDESCRIPTION, cteje.MESSAGE, cteje.COMPLETIONCODE, cteje.ERRORDESCRIPTION from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECJOURNALENTRY.name());
        sqlBuilder.append(" cteje join ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes on cteje.COMTASKEXECSESSION = ctes.id where ctes.comsession =");
        sqlBuilder.addLong(this.getId());
        sqlBuilder.append("and loglevel in (");
        this.appendLogLevels(levels, sqlBuilder);
        sqlBuilder.append(") order by timestamp desc");
        sqlBuilder.asPageBuilder(start, start + pageSize - 1);
        List<CombinedLogEntry> logEntries = new ArrayList<>();
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int discriminator = resultSet.getInt(1);
                    switch (discriminator) {
                        case -1: {
                            logEntries.add(new ComSessionJournalEntryAsCombinedLogEntry(resultSet));
                            break;
                        }
                        case 0: {
                            logEntries.add(new ComCommandJournalEntryAsCombinedLogEntry(resultSet));
                            break;
                        }
                        case 1: {
                            logEntries.add(new ComTaskExecutionMessageJournalEntryAsCombinedLogEntry(resultSet));
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return logEntries;
    }

    private void appendLogLevels(Set<ComServer.LogLevel> levels, SqlBuilder sqlBuilder) {
        Iterator<ComServer.LogLevel> levelIterator = levels.iterator();
        while (levelIterator.hasNext()) {
            ComServer.LogLevel logLevel = levelIterator.next();
            sqlBuilder.addInt(logLevel.ordinal());
            if (levelIterator.hasNext()) {
                sqlBuilder.append(",");
            }
        }
    }

    @Override
    public List<ComTaskExecutionSession> getComTaskExecutionSessions() {
        return Collections.unmodifiableList(comTaskExecutionSessions);
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public Instant getStopDate() {
        return stopDate;
    }

    @Override
    public boolean endsAfter(ComSession other) {
        return this.getStopDate().isAfter(other.getStopDate());
    }

    @Override
    public Duration getTotalDuration() {
        return Duration.ofMillis(totalMillis);
    }

    @Override
    public Duration getConnectDuration() {
        return Duration.ofMillis(connectMillis);
    }

    @Override
    public Duration getTalkDuration() {
        return Duration.ofMillis(talkMillis);
    }

    @Override
    public Duration getStoreDuration() {
        return Duration.ofMillis(storeMillis);
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
    public String getSuccessIndicatorDisplayName() {
        return ComSessionSuccessIndicatorTranslationKeys.translationFor(this.successIndicator, this.thesaurus);
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
        this.talkMillis = duration.toMillis();
    }

    void setStoreDuration(Duration duration) {
        this.storeMillis = duration.toMillis();
    }

    void setStopTime(Instant stopTime) {
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
        this.connectMillis = duration.toMillis();
    }

    @Override
    public ComTaskExecutionSessionImpl createComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Device device, Range<Instant> interval, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        ComTaskExecutionSessionImpl executionSession = ComTaskExecutionSessionImpl.from(dataModel, this, comTaskExecution, comTask, device, interval, successIndicator);
        comTaskExecutionSessions.add(executionSession);
        return executionSession;
    }

    @Override
    public ComSessionJournalEntry createJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message) {
        return this.createJournalEntry(timestamp, logLevel, message, null);
    }

    @Override
    public void addJournalEntry(ComSessionJournalEntry entry) {
        journalEntries.add(entry);
    }

    @Override
    public ComSessionJournalEntry createJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause) {
        ComSessionJournalEntryImpl entry = ComSessionJournalEntryImpl.from(dataModel, this, timestamp, logLevel, message, cause);
        journalEntries.add(entry);
        return entry;
    }

    @Override
    public void save() {
        this.calculateTotalMillis();
        this.calculateStatus();
        if (this.id == 0) {
            this.dataModel.mapper(ComSession.class).persist(this);
            /* Session may have been started a long time ago
             * so we will refresh the connection task to avoid
             * optimistic locking issues. */

            try {
                ((HasLastComSession) this.connectionTask.get()).sessionCreated(this);
            } catch (OptimisticLockException e) { // trying to limit the amount of finders for ConnectionTask
                HasLastComSession connectionTaskAsHasLastComSession = (HasLastComSession) this.connectionTaskService.findConnectionTask(this.connectionTask.get().getId()).get();
                connectionTaskAsHasLastComSession.sessionCreated(this);
            }
            this.comTaskExecutionSessions.forEach(this::notifyCreated);
        } else {
            this.dataModel.mapper(ComSession.class).update(this);
        }
    }

    private void notifyCreated(ComTaskExecutionSession comTaskExecutionSession) {
        this.notifyCreated((ComTaskExecutionSessionImpl) comTaskExecutionSession);
    }

    private void notifyCreated(ComTaskExecutionSessionImpl comTaskExecutionSession) {
        comTaskExecutionSession.created();
    }

    private void calculateTotalMillis() {
        if (this.startDate != null && this.stopDate != null) {
            this.totalMillis = (this.stopDate.toEpochMilli() - this.startDate.toEpochMilli());
        }
    }

    private void calculateStatus() {
        this.status = this.getSuccessIndicator().equals(SuccessIndicator.Success)
                && this.isZero(this.getNumberOfFailedTasks())
                && this.isZero(this.getNumberOfPlannedButNotExecutedTasks());
    }

    private boolean isZero(int aCounter) {
        return aCounter == 0;
    }

    public static ComSessionImpl from(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        return dataModel.getInstance(ComSessionImpl.class).init(connectionTask, comPortPool, comPort, startTime);
    }

    private ComSessionImpl init(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        this.connectionTask.set(connectionTask);
        this.comPortPool.set(comPortPool);
        this.comPort.set(comPort);
        this.startDate = startTime;
        return this;
    }

    private class ComSessionJournalEntryAsCombinedLogEntry implements CombinedLogEntry {
        private final Instant timestamp;
        private final ComServer.LogLevel logLevel;
        private final String message;
        private final String stacktrace;

        private ComSessionJournalEntryAsCombinedLogEntry(ResultSet resultSet) throws SQLException {
            this(
                    Instant.ofEpochMilli(resultSet.getLong(2)),
                    ComServer.LogLevel.values()[resultSet.getInt(3)],
                    resultSet.getString(4),
                    resultSet.getString(7));
        }

        private ComSessionJournalEntryAsCombinedLogEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String stacktrace) {
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.message = message;
            this.stacktrace = stacktrace;
        }

        @Override
        public Instant getTimestamp() {
            return this.timestamp;
        }

        @Override
        public ComServer.LogLevel getLogLevel() {
            return this.logLevel;
        }

        @Override
        public String getDetail() {
            return this.message;
        }

        @Override
        public String getErrorDetail() {
            return this.stacktrace;
        }
    }

    private class ComCommandJournalEntryAsCombinedLogEntry implements CombinedLogEntry {
        private final Instant timestamp;
        private final ComServer.LogLevel logLevel;
        private final String commandDescription;
        private final CompletionCode completionCode;

        private ComCommandJournalEntryAsCombinedLogEntry(ResultSet resultSet) throws SQLException {
            this(
                    Instant.ofEpochMilli(resultSet.getLong(2)),
                    ComServer.LogLevel.values()[resultSet.getInt(3)],
                    resultSet.getString(4),
                    CompletionCode.values()[resultSet.getInt(6)]);
        }

        private ComCommandJournalEntryAsCombinedLogEntry(Instant timestamp, ComServer.LogLevel logLevel, String commandDescription, CompletionCode completionCode) {
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.commandDescription = commandDescription;
            this.completionCode = completionCode;
        }

        @Override
        public Instant getTimestamp() {
            return this.timestamp;
        }

        @Override
        public ComServer.LogLevel getLogLevel() {
            return this.logLevel;
        }

        @Override
        public String getDetail() {
            return this.commandDescription;
        }

        @Override
        public String getErrorDetail() {
            if (CompletionCode.Ok.equals(this.completionCode)) {
                return "";
            } else {
                return this.completionCode.name();    // Todo: needs translation
            }
        }
    }

    private class ComTaskExecutionMessageJournalEntryAsCombinedLogEntry implements CombinedLogEntry {
        private final Instant timestamp;
        private final ComServer.LogLevel logLevel;
        private final String message;
        private final String errorDescription;

        private ComTaskExecutionMessageJournalEntryAsCombinedLogEntry(ResultSet resultSet) throws SQLException {
            this(
                    Instant.ofEpochMilli(resultSet.getLong(2)),
                    ComServer.LogLevel.values()[resultSet.getInt(3)],
                    resultSet.getString(5),
                    resultSet.getString(7));
        }

        private ComTaskExecutionMessageJournalEntryAsCombinedLogEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDescription) {
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.message = message;
            this.errorDescription = errorDescription;
        }

        @Override
        public Instant getTimestamp() {
            return this.timestamp;
        }

        @Override
        public ComServer.LogLevel getLogLevel() {
            return this.logLevel;
        }

        @Override
        public String getDetail() {
            return this.message;
        }

        @Override
        public String getErrorDetail() {
            return this.errorDescription;
        }
    }

}