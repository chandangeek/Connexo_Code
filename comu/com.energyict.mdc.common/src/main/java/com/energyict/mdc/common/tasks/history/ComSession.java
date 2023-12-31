/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@ConsumerType
@XmlRootElement
public interface ComSession extends HasId, TaskExecutionSummary {

    void save();

    enum SuccessIndicator {
        Success,
        SetupError,
        Broken,
        Interrupted,
        Not_Executed;

        public static Set<SuccessIndicator> unSuccessful () {
            return EnumSet.of(SetupError, Broken);
        }

        public static SuccessIndicator fromOrdinal(int ordinal) {
            return values()[ordinal];
        }
    }

    public DataModel getDataModel();

    public void setDataModel(DataModel dataModel);

    ConnectionTask getConnectionTask();

    void setConnectionTask(ConnectionTask connectionTask);

    ComPort getComPort();

    ComPortPool getComPortPool();

    ComStatistics getStatistics();

    List<ComSessionJournalEntry> getJournalEntries();

    /**
     * Gets the {@link ComSessionJournalEntry ComSessionJournalEntries} of this session
     * that are of the specified {@link ComServer.LogLevel}.
     *
     * @param levels The LogLevels of interes
     * @return The filtered List of ComSessionJournalEntry
     */
    Finder<ComSessionJournalEntry> getJournalEntries(Set<ComServer.LogLevel> levels);

    /**
     * Gets the {@link ComTaskExecutionJournalEntry ComTaskExecutionJournalEntries} of
     * all the {@link ComTaskExecutionSession}s of this ComSession
     * that are of the specified {@link ComServer.LogLevel}.
     *
     * @param levels The LogLevels of interes
     * @return The filtered List of ComTaskExecutionJournalEntry
     */
    Finder<ComTaskExecutionJournalEntry> getCommunicationTaskJournalEntries(Set<ComServer.LogLevel> levels);

    List<CombinedLogEntry> getAllLogs(Set<ComServer.LogLevel> levels, int start, int pageSize);

    List<ComTaskExecutionSession> getComTaskExecutionSessions();

    Instant getStartDate();

    Instant getStopDate();

    boolean endsAfter(ComSession other);

    /**
     * Gets the total number of milli seconds for which this ComSession was active.
     *
     * @return The total number of milli seconds
     */
    Duration getTotalDuration();

    /**
     * Gets the number of milli seconds take were necessary to setup the connection.
     *
     * @return The number of milli seconds required to setup the connection
     */
    Duration getConnectDuration();

    /**
     * Gets the number of milli seconds that were spent communicating with the device.
     *
     * @return The number of milli seconds that were spent on communication
     */
    Duration getTalkDuration();

    /**
     * Gets the number of milli seconds that were required to
     * store the result of the communication with the device.
     *
     * @return The number of milli seconds required to store the collected device data
     */
    Duration getStoreDuration();

    SuccessIndicator getSuccessIndicator();

    String getSuccessIndicatorDisplayName();

    /**
     * Gets the {@link TaskExecutionSummary} that provides
     * an overview of the tasks that have executed in this ComSession.
     *
     * @return The TaskExecutionSummary
     */
    TaskExecutionSummary getTaskExecutionSummary();

    /**
     * Tests if overall, this ComSession was successful or not.
     * As a minimum the SuccessIndicator will be Success
     * and there were be no failing tasks or tasks that
     * were planned to be execute but in the end were not executed.
     *
     * @return A flag that indicates if this ComSession was successful or not
     */
    boolean wasSuccessful();

    ComTaskExecutionSession createComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Device device, Range<Instant> interval, ComTaskExecutionSession.SuccessIndicator successIndicator);

    ComSessionJournalEntry createJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message);

    void addJournalEntry(ComSessionJournalEntry entry);

    ComSessionJournalEntry createJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause);

    /**
     * Models the combined view on {@link ComSessionJournalEntry} and {@link ComTaskExecutionJournalEntry}.
     */
    interface CombinedLogEntry {

        /**
         * Gets the Date on which this {@link ComSessionJournalEntry} or {@link ComTaskExecutionJournalEntry} was created.
         *
         * @return The Date
         * @see ComSessionJournalEntry#getTimestamp()
         * @see ComTaskExecutionJournalEntry#getTimestamp()
         */
        Instant getTimestamp();

        /**
         * Gets the level at which this {@link ComSessionJournalEntry} or {@link ComTaskExecutionMessageJournalEntry} was logged
         * or {@link ComServer.LogLevel#INFO} for {@link ComCommandJournalEntry}.
         *
         * @return The LogLevel
         * @see ComSessionJournalEntry#getLogLevel()
         * @see ComTaskExecutionMessageJournalEntry#getLogLevel()
         */
        ComServer.LogLevel getLogLevel();

        /**
         * Gets the details of this combined log entry, which will map to either
         * <ul>
         * <li>{@link ComSessionJournalEntry#getMessage()}</li>
         * <li>{@link ComTaskExecutionMessageJournalEntry#getMessage()}</li>
         * <li>{@link ComCommandJournalEntry#getCommandDescription()}</li>
         * </ul>
         * @return The details
         * @see ComSessionJournalEntry#getMessage()
         * @see ComTaskExecutionMessageJournalEntry#getMessage()
         * @see ComCommandJournalEntry#getCommandDescription()
         */
        String getDetail();

        /**
         * Gets the error details of this combined log entry, which will map to either
         * <ul>
         * <li>{@link ComSessionJournalEntry#getStackTrace()}</li>
         * <li>{@link ComTaskExecutionMessageJournalEntry#getErrorDescription()}</li>
         * <li>{@link ComCommandJournalEntry#getCompletionCode()}</li>
         * </ul>
         * @return The details
         * @see ComSessionJournalEntry#getStackTrace()
         * @see ComTaskExecutionMessageJournalEntry#getErrorDescription()
         * @see ComCommandJournalEntry#getCompletionCode()
         */
        String getErrorDetail();

    }

}