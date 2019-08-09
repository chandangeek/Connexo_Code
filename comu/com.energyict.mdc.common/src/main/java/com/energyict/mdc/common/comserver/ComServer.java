/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
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
@ProviderType
public interface ComServer extends HasId, HasName {
    String CHANGES_INTER_POLL_DELAY_RESOURCE_KEY = "comserver.changesInterPollDelay";
    String SCHEDULING_INTER_POLL_DELAY_RESOURCE_KEY = "comserver.schedulingInterPollDelay";

    String EVENT_REGISTRATION_URI_PATTERN = "ws://{0}:{1}/events/registration";
    String QUERY_API_URI_PATTERN = "ws://{0}:{1}/remote/queries";
    String STATUS_URI_ATTERN = "http://{0}:{1}/api/dsr/comserverstatus";

    int DEFAULT_STATUS_PORT_NUMBER = 8080;
    int DEFAULT_EVENT_REGISTRATION_PORT_NUMBER = 8888;
    int DEFAULT_QUERY_API_PORT_NUMBER = 8889;

    /**
     * The minimum TimeDuration that can be used for changes and scheduling interpoll delay.
     */
    TimeDuration MINIMUM_INTERPOLL_DELAY = new TimeDuration(60, TimeDuration.TimeUnit.SECONDS);
    /**
     * The maximum value for the storeTaskQueueSize property.
     */
    int MAXIMUM_STORE_TASK_QUEUE_SIZE = 99999;
    /**
     * The maximum value for the numberOfStoreTaskThreads property.
     */
    int MAXIMUM_NUMBER_OF_STORE_TASK_THREADS = 99;

    int MINIMUM_NUMBER_OF_STORE_TASK_THREADS = 1;
    /**
     * The minimum value for the storeTaskThreadPriority property.
     */
    int MINIMUM_STORE_TASK_THREAD_PRIORITY = Thread.MIN_PRIORITY;
    /**
     * The maximum value for the storeTaskThreadPriority property.
     */
    int MINIMUM_STORE_TASK_QUEUE_SIZE = 1;

    int MAXIMUM_STORE_TASK_THREAD_PRIORITY = Thread.MAX_PRIORITY;

    int MIN_NON_REQUIRED_PORT_RANGE = 0;
    int MIN_REQUIRED_PORT_RANGE = 1;
    int MAX_PORT_RANGE = 65535;

    enum LogLevel {
        /**
         * Shows only error messages that are the result of serious problems
         * that have occured during the process. Typically, there is no
         * recovery from these types of problems and the current
         * process will likely have stopped or been abandonned.
         */
        ERROR("Error") {
            @Override
            public String toString () {
                return "logLevel_error";
            }
        },

        /**
         * Shows only warning messages that are indicators for potential problems
         * that may occur later on in the process.
         */
        WARN("Warning") {
            @Override
            public String toString () {
                return "logLevel_warning";
            }
        },

        /**
         * Shows only informational messages that provide high level
         * understanding of what the process is doing.
         */
        INFO("Information") {
            @Override
            public String toString () {
                return "logLevel_info";
            }
        },


        /**
         * Fairly detailed log level, typically used for diagnosing/debugging problems.
         */
        DEBUG("Debug") {
            @Override
            public String toString () {
                return "logLevel_debug";
            }
        },

        /**
         * The most detailed log level, showing all possible messages.
         */
        TRACE("Trace") {
            @Override
            public String toString () {
                return "logLevel_trace";
            }
        };

        private String nameKey;

        LogLevel(String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum FieldNames {
        NAME("name");
        private final String name;

        FieldNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    void setName(String name);

    /**
     * Gets the timestamp of the last modification applied to this ComServer.
     *
     * @return The timestamp of the last modification
     */
    Instant getModificationDate();

    long getVersion();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link OnlineComServer}.
     * @return <code>true</code> iff this ComServer is actually an OnlineComServer
     */
    boolean isOnline();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link RemoteComServer}.
     * @return <code>true</code> iff this ComServer is actually an RemoteComServer
     */
    boolean isRemote();

    /**
     * Returns <code>true</code> iff this ComServer is
     * actually an {@link OfflineComServer}.
     * @return <code>true</code> iff this ComServer is actually an OfflineComServer
     */
    boolean isOffline();

    /**
     * Tests if this ComServer is active.
     * Only active ComServer's can be started and communicate with devices.
     *
     * @return A flag that indicates if this ComServer is active (<code>true</code>) or inactive (<code>false</code>).
     */
    boolean isActive();

    /**
     * Gets the LogLevel that is used for global server processes.
     *
     * @return The LogLevel that is used for global server processes.
     */
    LogLevel getServerLogLevel();

    void setServerLogLevel(LogLevel serverLogLevel);

    /**
     * Gets the LogLevel that is used for processes that focus
     * on communication with devices.
     *
     * @return The LogLevel that is used for communication.
     */
    LogLevel getCommunicationLogLevel();

    void setCommunicationLogLevel(LogLevel communicationLogLevel);

    /**
     * Gets the {@link TimeDuration} between each poll for changes
     * that were applied to all objects that relate to this ComServer.
     *
     * @return The TimeDuration between polls to detect changes
     */
    TimeDuration getChangesInterPollDelay();

    void setChangesInterPollDelay(TimeDuration changesInterPollDelay);

    /**
     * Gets the {@link TimeDuration} between each poll for communication
     * work that needs to be done.
     *
     * @return The TimeDuration between polls to find communication tasks
     */
    TimeDuration getSchedulingInterPollDelay();

    void setSchedulingInterPollDelay(TimeDuration schedulingInterPollDelay);

    /**
     * Gets url of the com server
     *
     * @return url of the com server
     */
    String getServerMonitorUrl();

    void setServerMonitorUrl(String serverUrl);

    /**
     * Gets the list of {@link ComPort}s that are owned by this ComServer.
     *
     * @return The list of ComPorts
     */
    List<ComPort> getComPorts();

    /**
     * Makes this ComServer obsolete, i.e. it will no longer execute
     * nor will it be returned by ComServerFactory finder methods.
     * This will also make all {@link ComPort}s obsolete.
     */
    void makeObsolete();

    /**
     * Indicates if this ComServer is obsolete.
     *
     * @return A flag that indicates if this ComServer is obsolete
     */
    boolean isObsolete();

    /**
     * Gets the date on which this ComServer was made obsolete.
     *
     * @return The date when this ComServer was made obsolete
     *         or <code>null</code> when this ComServer is not obsolete at all.
     */

    Instant getObsoleteDate();

    void setActive(boolean active);

    OutboundComPort.OutboundComPortBuilder newOutboundComPort(String name, int numberOfSimultaneousConnections);
    ServletBasedInboundComPort.ServletBasedInboundComPortBuilder newServletBasedInboundComPort(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber);
    ModemBasedInboundComPort.ModemBasedInboundComPortBuilder newModemBasedInboundComport(
            String name, int ringCount, int maximumDialErrors,
            TimeDuration connectTimeout, TimeDuration atCommandTimeout,
            SerialPortConfiguration serialPortConfiguration);
    TCPBasedInboundComPort.TCPBasedInboundComPortBuilder newTCPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber);
    UDPBasedInboundComPort.UDPBasedInboundComPortBuilder newUDPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber);

    void removeComPort(long id);

    void delete();

    void update();

    void saved(ComPort comPort);
    /**
     * Gets the URI on which the event registration mechanism runs
     * if that mechanism is supported.
     *
     * @return The URI
     */
    String getEventRegistrationUriIfSupported();

    /**
     * Gets the URI on which the remote query api runs if that is supported.
     *
     * @return The URI
     */
    String getQueryApiPostUriIfSupported();

    @ProviderType
    interface ComServerBuilder<CS extends ComServer, CSB extends ComServerBuilder> {
        CSB name(String comServerName);
        CSB changesInterPollDelay(TimeDuration changesInterPollDelay);
        CSB schedulingInterPollDelay(TimeDuration schedulingInterPollDelay);
        CSB communicationLogLevel(LogLevel logLevel);
        CSB serverLogLevel(LogLevel logLevel);
        CSB serverMonitorUrl(String comServerUrl);
        CSB active(boolean active);
        CS create();
    }

}