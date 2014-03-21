package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskExecutionSummary;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 14:25
 */
public interface ComSession extends IdBusinessObject {

    public enum SuccessIndicator {
        Success,
        SetupError,
        Broken;

        public static Set<SuccessIndicator> unSuccessful (){
            return EnumSet.of(SetupError, Broken);
        }

        public int dbValue() {
            return this.ordinal();
        }

        public static SuccessIndicator valueFromDb(int dbValue) {
            for (SuccessIndicator indicator : SuccessIndicator.values()) {
                if (indicator.dbValue() == dbValue) {
                    return indicator;
                }
            }
            throw new ApplicationException("unknown dbValue: " + dbValue);
        }

    }

    public ConnectionTask getConnectionTask ();

    public ComPort getComPort ();

    public ComPortPool getComPortPool ();

    public ComStatistics getComStatistics ();

    public List<ComSessionJournalEntry> getJournalEntries ();

    public List<ComTaskExecutionSession> getComTaskExecutionSessions ();

    public Date getStartDate ();

    public Date getStopDate ();

    /**
     * Gets the total number of milli seconds for which this ComSession was active.
     *
     * @return The total number of milli seconds
     */
    public long getTotalTime ();

    /**
     * Gets the number of milli seconds take were necessary to setup the connection.
     *
     * @return The number of milli seconds required to setup the connection
     */
    public long getConnectMillis ();

    /**
     * Gets the number of milli seconds that were spent communicating with the device.
     *
     * @return The number of milli seconds that were spent on communication
     */
    public long getTalkMillis ();

    /**
     * Gets the number of milli seconds that were required to
     * store the result of the communication with the device.
     *
     * @return The number of milli seconds required to store the collected device data
     */
    public long getStoreMillis ();

    public SuccessIndicator getSuccessIndicator ();

    /**
     * Gets the {@link TaskExecutionSummary} that provides
     * an overview of the tasks that have executed in this ComSession.
     *
     * @return The TaskExecutionSummary
     */
    public TaskExecutionSummary getTaskExecutionSummary ();

    /**
     * Tests if overall, this ComSession was successful or not.
     * As a minimum the SuccessIndicator will be Success
     * and there were be no failing tasks or tasks that
     * were planned to be execute but in the end were not executed.
     *
     * @return A flag that indicates if this ComSession was successful or not
     */
    public boolean wasSuccessful ();

}