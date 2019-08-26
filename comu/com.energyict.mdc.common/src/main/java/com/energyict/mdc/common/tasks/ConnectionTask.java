/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.pluggable.PluggableClassUsage;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.TaskExecutionSummary;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models a task that is able to establish a connection
 * with a device that is designed for communication.<br>
 * A ConnectionTask can be used by multiple devices,
 * allowing those devices to all use the same communication channel.
 * In that case, the device will most likely be a gateway-like device.
 * <p>
 * A ConnectionTask of type X cannot be created against a device if the
 * {@link com.energyict.mdc.device.config.DeviceConfiguration} does not support X.
 * Remember that support for type X on the configuration level is
 * managed with a {@link PartialConnectionTask}. Every ConnectionTask
 * will therefore be linked to the PartialConnectionTask from which
 * it was created and from which it can inherit connection properties.
 * <p>
 * When the {@link ConnectionType} does not support {@link com.energyict.mdc.common.ComWindow}s
 * then the ConnectionTask is NOT required to have a ComWindow.
 * In case there is a ComWindow, the ConnectionTask will only
 * establish a connection when the time isclient zijnde  within the ComWindow.
 * This ComWindow is computed against the device's time zone.
 * <p>
 * Each time a ConnectionTask is executed, a ComSession
 * is created that captures all the details of the communication with the device.
 * That communication overview is very important and should not be deleted
 * easily. Therefore, ConnectionTasks are never deleted but made obsolete.
 * Obsolete ConnectionTasks will not return from {@link DeviceService} finder methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (09:59)
 */
@ConsumerType
public interface ConnectionTask<CPPT extends ComPortPool, PCTT extends PartialConnectionTask>
    extends
        ConnectionProvider,
        ConnectionTaskPropertyProvider,
        PluggableClassUsage<ConnectionType, ConnectionTypePluggableClass, ConnectionTaskProperty>,
        ConnectionTaskExecutionAspects,
        HasId,
        HasName {

    enum Type {
        /**
         * For inbound connections.
         */
        INBOUND,
        /**
         * For outbound connections.
         */
        OUTBOUND,
        /**
         * For initiation of a connection.
         */
        CONNECTION_INITIATION
    }

    /**
     * Indicates the overall success of a ConnectionTask,
     * relating to the overall success of the last {@link ComSession}.
     */
    enum SuccessIndicator {
        /**
         * Indicates that the last {@link ComSession} was successful.
         */
        SUCCESS,

        /**
         * Indicates that the last {@link ComSession} was <strong>NOT</strong> successful.
         */
        FAILURE,

        /**
         * Indicates that there is no last {@link ComSession}.
         */
        NOT_APPLICABLE

    }

    /**
     * Represents the lifecycle state of a ConnectionTask.
     */
    enum ConnectionTaskLifecycleStatus {

        /**
         * Active means the ConnectionTask is completely validated and ready to be used by the ComServer.
         * KeyAccessors or their actual value might still be missing on the device.
         */
        ACTIVE,
        /**
         * InActive means the ConnectionTask is completely validated but not ready to be used by the ComServer (onhold/paused).
         */
        INACTIVE,
        /**
         * The ConnectionTask is created but not valid and can not be used by the ComServer to execute tasks yet.
         * This means that some properties may not be present yet. Even if a KeyAccessorType property is present, the
         * presence of a KeyAccessor or it's actual value is not covered by this state.
         */
        INCOMPLETE
    }

    /**
     * Gets the {@link ConnectionType} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionType
     */
    ConnectionType getConnectionType();

    /**
     * Gets the {@link Device} that this ConnectionTask is going
     * to connect to when executing.
     *
     * @return The Device that will be connected to
     */
    Device getDevice();

    /**
     * Gets the {@link PartialConnectionTask} from which this ConnectionTask
     * was created and from which it will inherit properties.
     * Note that this is read-only and will typically be set at creation time.
     *
     * @return The PartialConnectionTask
     */
    PCTT getPartialConnectionTask();

    /**
     * Gets the list of {@link ConnectionTaskProperty ConnectionTaskProperties}
     * that will currently be used to connect to devices.<br>
     * Note that properties that are inherited from higher levels
     * such as e.g. the {@link ConnectionTypePluggableClass}
     * are also returned. Use the {@link ConnectionTaskProperty#isInherited()} method
     * to distinguish between local and inherited values should that be necessary.
     *
     * @return The List of ConnectionTaskProperties
     */
    List<ConnectionTaskProperty> getProperties();

    /**
     * Gets the list of {@link ConnectionTaskProperty ConnectionTaskProperties}
     * that are active on the specified Date to connect to devices.<br>
     * Note that properties that are inherited from higher levels
     * such as e.g. the {@link ConnectionTypePluggableClass}
     * are also returned. Use the {@link ConnectionTaskProperty#isInherited()} method
     * to distinguish between local and inherited values should that be necessary.
     *
     * @param date The Date on which the ConnectionTaskProperty should be active
     * @return The List of ConnectionTaskProperties
     */
    List<ConnectionTaskProperty> getProperties(Instant date);

    /**
     * Sets the value of the property with the specified name.
     *
     * @param propertyName the name of the property
     * @param value        The property value
     */
    void setProperty(String propertyName, Object value);

    /**
     * Removes the property with the specified name.
     * In case the property was inherited from the {@link ConnectionTypePluggableClass}
     * then this actually means that this ConnectionTask is reverting
     * the setting of the property back to the ConnectionTypePluggableClass level.
     *
     * @param propertyName The name of the property
     */
    void removeProperty(String propertyName);

    /**
     * Tests if this ConnectionTask is paused.
     * A ConnectionTask that is paused, will not be executed
     * by the ComServer. In addition, all {@link ComTaskExecution}s
     * that are linked to it will not be executed.
     * Remember that a ScheduledComTask can be linked directly to a ConnectionTask
     * or it can be linked to the default ConnectionTask of the device hieararchy.
     *
     * @return A flag that indicates if this ConnectionTask is paused
     */
    ConnectionTaskLifecycleStatus getStatus();

    /**
     * Makes this ConnectionTask obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceService} finder methods.
     */
    void makeObsolete();

    /**
     * Tests if this ConnectionTask is obsolete.
     *
     * @return A flag that indicates if this ConnectionTask is obsolete
     */
    boolean isObsolete();

    /**
     * Gets the date on which this ConnectionTask was made obsolete.
     *
     * @return The date when this ConnectionTask was made obsolete
     * or <code>null</code> when this ConnectionTask is not obsolete at all.
     */
    Instant getObsoleteDate();

    /**
     * Tests if this ConnectionTask is the default that should be used
     * by {@link ComTaskExecution}s that are configured to use the default.
     *
     * @return A flag that indicates if this is the default
     */
    boolean isDefault();

    /**
     * Returns if the {@link ConnectionType} allows simultaneous
     * connections to be created or not.
     *
     * @return <code>true</code> iff the ConnectionType allows simultaneous connections
     * @see ConnectionType#allowsSimultaneousConnections()
     */
    boolean allowsSimultaneousConnections();

    /**
     * Gets the ComPortPool from which a {@link ComPort} will
     * be pulled when a connection needs to be established for an outbound task
     * or the ComPortPool that contains the ComPorts that will be used by
     * the physical device to connect to the platform for an inbound task.
     *
     * @return The ComPortPool
     */
    CPPT getComPortPool();

    void setComPortPool(CPPT comPortPool);

    /**
     * Tests if this ConnectionTask has a {@link ComPortPool}.
     *
     * @return A flag that indicates if this ConnectionTask has a ComPortPool
     */
    boolean hasComPortPool();

    /**
     * Keeps track of the last time a connection was started for this ConnectionTask.
     *
     * @return the date of the last connection start.
     */

    Instant getLastCommunicationStart();

    /**
     * Keeps track of the last time a SUCCESSFUL connection was ended, independent whether or not the ConnectionTask has failed.
     *
     * @return the date of the last SUCCESSFUL connection end.
     */
    Instant getLastSuccessfulCommunicationEnd();

    /**
     * Gets this ConnectionTask's last {@link ComSession} or <code>absent</code>
     * if there are no ComSessions yet.
     *
     * @return The last ComSession or <code>null</code>
     */
    Optional<ComSession> getLastComSession();

    /**
     * Gets the {@link SuccessIndicator} of this ConnectionTask.
     *
     * @return The SuccessIndicator
     */
    SuccessIndicator getSuccessIndicator();

    String getSuccessIndicatorDisplayName();

    /**
     * Gets the {@link ComSession.SuccessIndicator} of this ConnectionTask's
     * last {@link ComSession} or <code>absent</code> if this ConnectionTask
     * has never before been executed and therefore no ComSession exists.
     *
     * @return The SuccessIndicator of the last ComSession or <code>null</code>
     * if there is no ComSession yet
     */
    Optional<ComSession.SuccessIndicator> getLastSuccessIndicator();

    /**
     * Gets the {@link TaskExecutionSummary} of this ConnectionTask's
     * last {@link ComSession} or <code>absent</code> if this ConnectionTask
     * has never before been executed and therefore no ComSession exists.
     *
     * @return The TaskExecutionSummary of the last ComSession or <code>null</code>
     * if there is no ComSession yet
     */
    Optional<TaskExecutionSummary> getLastTaskExecutionSummary();

    /**
     * Tests if this ConnectionTask is currently executing.
     * Convenience (and possibly faster) for <code>getExecutingComServer() != null</code>.
     *
     * @return <code>true</code> iff this ConnectionTask is executing, i.e. if the executing ComServer is not null
     */
    boolean isExecuting();

    /**
     * Gets the {@link ComServer} that is currently
     * executing this ConnectionTask or <code>null</code>
     * if this ConnectionTask is not executing at this moment.
     *
     * @return The ComServer or <code>null</code>
     */
    ComServer getExecutingComServer();

    /**
     * Pauses this connectionTask, i.e. temporarily disables its execution.
     * The reverse operation is {@link #activate()}
     */
    void deactivate();

    /**
     * Resumes the ability to execute this ConnectionTask.
     * This is the reverse operation of {@link #deactivate()}
     */
    void activate();

    void save();

    long getVersion();

    /**
     * Checks if the ConnectionTask is active
     * @return true if the ConnectionTask is active,
     *          false if the ConnectionTask is inactive
     */
    boolean isActive();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties}.
     */
    ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

}