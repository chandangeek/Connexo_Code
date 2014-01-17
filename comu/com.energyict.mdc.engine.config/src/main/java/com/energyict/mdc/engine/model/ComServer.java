package com.energyict.mdc.engine.model;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.impl.ComServerImpl;
import java.util.Date;
import java.util.List;

/**
 * Models a server that will schedule communication with devices.
 * The communication is done through {@link ComPort}s.
 * By convention, the name of a ComServer is the name
 * of the physical machine the server is running on.<br>
 * Once started, a ComServer will poll the database to detect
 * changes that were applied to all objects that relate to the ComServer.
 * As such, it will detect the following:
 * <ul>
 * <li>Changing active/inactive flag of the ComServer</li>
 * <li>Changing server log level of the ComServer</li>
 * <li>Changing communication log level of the ComServer</li>
 * <li>Changing the changesInterPollDelay of the ComServer</li>
 * <li>Changing active/inactive flags of related ComPorts</li>
 * </ul>
 * <p/>
 * Each time a ComTaskExecution is executed,
 * one of the ComPorts of the ComServer that executes the ScheduledComTask
 * will be used to connect to the device.
 * A ComSession is created that captures all the details
 * of the communication with the device. That communication overview
 * is obviously very imported and should not be deleted easily.
 * Therefore, objects that relate to the communication overview
 * are never deleted but made obsolete instead.
 * Obsolete ComServers will not return from ComServerFactory
 * finder methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (16:42)
 */
public interface ComServer {
    static final String CHANGES_INTER_POLL_DELAY_RESOURCE_KEY = "comserver.changesInterPollDelay";
    static final String SCHEDULING_INTER_POLL_DELAY_RESOURCE_KEY = "comserver.schedulingInterPollDelay";
    /**
     * The minimum TimeDuration that can be used for changes and scheduling interpoll delay.
     */
    static final TimeDuration MINIMUM_INTERPOLL_DELAY = new TimeDuration(60, TimeDuration.SECONDS);
    /**
     * The maximum value for the storeTaskQueueSize property.
     */
    public static final int MAXIMUM_STORE_TASK_QUEUE_SIZE = 99999;
    /**
     * The maximum value for the numberOfStoreTaskThreads property.
     */
    public static final int MAXIMUM_NUMBER_OF_STORE_TASK_THREADS = 99;

    public static final int MINIMUM_NUMBER_OF_STORE_TASK_THREADS = 1;
    /**
     * The minimum value for the storeTaskThreadPriority property.
     */
    public static final int MINIMUM_STORE_TASK_THREAD_PRIORITY = Thread.MIN_PRIORITY;
    /**
     * The maximum value for the storeTaskThreadPriority property.
     */

    public static final int MINIMUM_STORE_TASK_QUEUE_SIZE = 1;


    public static final int MAXIMUM_STORE_TASK_THREAD_PRIORITY = Thread.MAX_PRIORITY;

    public enum LogLevel {
        /**
         * Shows only error messages that are the result of serious problems
         * that have occured during the process. Typically, there is no
         * recovery from these types of problems and the current
         * process will likely have stopped or been abandonned.
         */
        ERROR {
            @Override
            public String toString () {
                return "logLevel_error";
            }
        },

        /**
         * Shows only warning messages that are indicators for potential problems
         * that may occur later on in the process.
         */
        WARN {
            @Override
            public String toString () {
                return "logLevel_warning";
            }
        },

        /**
         * Shows only informational messages that provide high level
         * understanding of what the process is doing.
         */
        INFO {
            @Override
            public String toString () {
                return "logLevel_info";
            }
        },


        /**
         * Fairly detailed log level, typically used for diagnosing/debugging problems.
         */
        DEBUG {
            @Override
            public String toString () {
                return "logLevel_debug";
            }
        },

        /**
         * The most detailed log level, showing all possible messages.
         */
        TRACE {
            @Override
            public String toString () {
                return "logLevel_trace";
            }
        };

    }

    public long getId();

    public String getName();

    public void setName(String name);

    /**
     * Gets the timestamp of the last modification applied to this ComServer.
     *
     * @return The timestamp of the last modification
     */
    public UtcInstant getModificationDate();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link OnlineComServer}.
     * @return <code>true</code> iff this ComServer is actually an OnlineComServer
     */
    public boolean isOnline ();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link RemoteComServer}.
     * @return <code>true</code> iff this ComServer is actually an RemoteComServer
     */
    public boolean isRemote ();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link OfflineComServer}.
     * @return <code>true</code> iff this ComServer is actually an OfflineComServer
     */
    public boolean isOffline ();

    /**
     * Tests if this ComServer is active.
     * Only active ComServer's can be started and communicate with devices.
     *
     * @return A flag that indicates if this ComServer is active (<code>true</code>) or inactive (<code>false</code>).
     */
    public boolean isActive ();

    /**
     * Gets the LogLevel that is used for global server processes.
     *
     * @return The LogLevel that is used for global server processes.
     */
    public LogLevel getServerLogLevel ();

    public void setServerLogLevel(LogLevel serverLogLevel);

    /**
     * Gets the LogLevel that is used for processes that focus
     * on communication with devices.
     *
     * @return The LogLevel that is used for communication.
     */
    public LogLevel getCommunicationLogLevel ();

    public void setCommunicationLogLevel(LogLevel communicationLogLevel);

    /**
     * Gets the {@link TimeDuration} between each poll for changes
     * that were applied to all objects that relate to this ComServer.
     *
     * @return The TimeDuration between polls to detect changes
     */
    public TimeDuration getChangesInterPollDelay ();

    public void setChangesInterPollDelay(TimeDuration changesInterPollDelay);

    /**
     * Gets the {@link TimeDuration} between each poll for communication
     * work that needs to be done.
     *
     * @return The TimeDuration between polls to find communication tasks
     */
    public TimeDuration getSchedulingInterPollDelay ();

    public void setSchedulingInterPollDelay(TimeDuration schedulingInterPollDelay);

    /**
     * Gets the list of {@link ComPort}s that are owned by this ComServer.
     *
     * @return The list of ComPorts
     */
    public List<ComPort> getComPorts ();

    public void setComPorts(List<ComPort> comPorts);
    /**
     * Makes this ComServer obsolete, i.e. it will no longer execute
     * nor will it be returned by ComServerFactory finder methods.
     * This will also make all {@link ComPort}s obsolete.
     */
    public void makeObsolete ();

    /**
     * Indicates if this ComServer is obsolete
     *
     * @return A flag that indicates if this ComServer is obsolete
     */
    public boolean isObsolete ();

    /**
     * Gets the date on which this ComServer was made obsolete.
     *
     * @return The date when this ComServer was made obsolete
     *         or <code>null</code> when this ComServer is not obsolete at all.
     */

    public Date getObsoleteDate ();

    public void setActive(boolean active);

    public String getType();

    public OutboundComPort.OutboundComPortBuilder newOutboundComPort();
    public ComServerImpl.ServletBasedComPortBuilder newServletBasedInboundComPort();
    public ComServerImpl.ModemBasedComPortBuilder newModemBasedInboundComport();
    public ComServerImpl.TCPBasedComPortBuilder newTCPBasedInboundComPort();
    public ComServerImpl.UDPBasedComPortBuilder newUDPBasedInboundComPort();

    public void delete();

    public void save();

}