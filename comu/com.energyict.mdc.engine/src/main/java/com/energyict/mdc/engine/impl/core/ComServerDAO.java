package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundDAO;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.protocol.api.device.data.TopologyNeighbour;
import com.energyict.mdc.protocol.api.device.data.TopologyPathSegment;
import com.energyict.mdc.protocol.api.device.data.identifiers.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * Models the behavior of a component that provides access to the data
 * that is relevant to the ComServer.
 * Implementation classes will focus on the actual data source containing the data.
 * <p>
 * The implementation classes are allowed to throw com.energyict.comserver.core.interfaces.DataAccessException(s)
 * to report severe problems that relate to the actual data source.
 * <p>
 * Refer to java website for a complete discussion on the
 * <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Object design pattern</a>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-16 (16:14)
 */
public interface ComServerDAO extends InboundDAO, ServerProcess {

    /**
     * Gets the ComServer that relates to the machine where this code is running on.
     *
     * @return The ComServer that relates to this machine or <code>null</code> if the host name of
     *         the machine is <strong>NOT</strong> the name of a registered ComServer.
     */
    public ComServer getThisComServer ();

    /**
     * Gets the ComServer that relates to the machine with the specified host name.
     *
     * @return The ComServer that relates to the machine with the specified host name
     *         or <code>null</code> if there is no such ComServer.
     */
    public ComServer getComServer (String systemName);

    /**
     * Refreshes the specified ComServer, i.e. checks for updates
     * to the object and returns a new version if there were any changes.
     * Returns exactly the same object if there are no changes.
     * Returns <code>null</code> if the ComServer no longer exists because it was deleted or made obsolete
     *
     * @param comServer The ComServer
     * @return The new version of the ComServer,
     *         <code>null</code> if the ComServer was deleted or made obsolete or
     *         exactly the same Comserver if no changes were found
     */
    public ComServer refreshComServer (ComServer comServer);

    /**
     * Refreshes the specified ComPort, i.e. checks for updates
     * to the object and returns a new version if there were any changes.
     * Returns exactly the same object if there are no changes.
     * Returns <code>null</code> if the ComPort no longer exists because it was deleted or made obsolete
     *
     * @param comPort The ComPort
     * @return The new version of the ComPort,
     *         <code>null</code> if the ComPort was deleted or made obsolete or
     *         exactly the same Comport if no changes were found
     */
    public ComPort refreshComPort (ComPort comPort);

    /**
     * Finds and returns all the ComJobs that are ready
     * to be executed by the specified OutboundComPort.
     *
     * @param comPort The OutboundComPort
     * @return The List of ComJobs that represent all the ComTaskExecutions that are ready to be executed
     * @see OutboundComPort
     */
    public List<ComJob> findExecutableOutboundComTasks (OutboundComPort comPort);

    /**
     * Finds and returns all the ComTaskExecutions that are ready
     * to be executed against the specified Device
     * on the specified InboundComPort.
     * Note that when the Device is not configured for inbound communication
     * because it does not have an InboundConnectionTask
     * or when none of the ComTaskExecutions are ready to execute, i.e. their state is not
     * TaskStatus#Pending, an empty list is returned.
     *
     * @param device The device
     * @param comPort The InboundComPort
     * @return The List of ComTaskExecutions that are ready to be executed
     */
    public List<ComTaskExecution> findExecutableInboundComTasks (OfflineDevice device, InboundComPort comPort);

    /**
     * Finds the {@link ConnectionTaskProperty connection properties}
     * for the specified {@link ConnectionTask}.
     *
     * @param connectionTask The ConnectionTask
     * @return The List of ConnectionTaskProperty
     */
    public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask);

    /**
     * Attempts to lock the ScheduledConnectionTask for
     * its execution on the specified ComServer
     * and returns the locked ScheduledConnectionTask when the lock succeeds
     * and <code>null</code> when the lock fails.
     * Make sure to release the lock in a finally block
     * to avoid that the ScheduledConnectionTask remains
     * locked for ever as that would mean it will
     * never execute again.
     *
     * @param connectionTask The OutboundConnectionTask
     * @param comServer The ComServer
     * @return <code>true</code> iff the lock succeeds
     */
    public ScheduledConnectionTask attemptLock (ScheduledConnectionTask connectionTask, ComServer comServer);

    public boolean attemptLock(OutboundConnectionTask connectionTask, ComServer comServer);

    /**
     * Unlocks the ScheduledConnectionTask, basically undoing the effect
     * of the attemptLock method providing that was successful.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    public void unlock (ScheduledConnectionTask connectionTask);

    /**
     * Unlocks the OutboundConnectionTask, basically undoing the effect
     * of the attemptLock method providing that was successful.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    public void unlock (OutboundConnectionTask connectionTask);

    /**
     * Attempts to lock the ComTaskExecution for
     * its execution on the specified ComPort
     * and returns <code>true</code> when the lock succeeds
     * and <code>false</code> when the lock fails.
     * Make sure to release the lock in a finally block
     * to avoid that the ComTaskExecution remains
     * locked for ever as that would mean it will
     * never execute again.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param comPort The ComPort
     * @return <code>true</code> iff the lock succeeds
     */
    public boolean attemptLock (ComTaskExecution comTaskExecution, ComPort comPort);

    /**
     * Unlocks the ComTaskExecution, basically undoing the effect
     * of the attemptLock method providing that was successful.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void unlock (ComTaskExecution comTaskExecution);

    /**
     * Notifies that execution of the specified OutboundConnectionTask
     * was started by the specified ComServer.
     *
     * @param connectionTask The OutboundConnectionTask
     * @param comServer The ComServer that started the execution
     */
    public void executionStarted (ConnectionTask connectionTask, ComServer comServer);

    /**
     * Notifies that execution of the specified OutboundConnectionTask completed.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    public void executionCompleted (ConnectionTask connectionTask);

    /**
     * Notifies that execution of the specified OutboundConnectionTask failed.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    public void executionFailed (ConnectionTask connectionTask);

    /**
    * Notifies that execution of the specified ComTaskExecution has been started
    * on the specified ComPort.
    *  @param comTaskExecution The ComTaskExecution
    * @param comPort The ComPort that has started the execution of the ComTaskExecution
     * @param executeInTransaction
     */
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction);

    /**
     * Notifies that execution of the specified ComTaskExecution completed.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void executionCompleted (ComTaskExecution comTaskExecution);

    /**
     * Notifies that execution of the specified ComTaskExecutions completed.
     *
     * @param comTaskExecutions The List of completed ComTaskExecution
     */
    public void executionCompleted (List<? extends ComTaskExecution> comTaskExecutions);

    /**
     * Notifies that execution of the specified ComTaskExecution failed.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void executionFailed (ComTaskExecution comTaskExecution);

    /**
     * Notifies that execution of the specified ComTaskExecution failed.
     *
     * @param comTaskExecutions The List of failed ComTaskExecution
     */
    public void executionFailed (List<? extends ComTaskExecution> comTaskExecutions);

    /**
     * Cleans up any marker flags on ComTaskExecutions
     * that were not properly cleaned because the ComServer they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comServer The ComServer that is currently starting up.
     */
    public void releaseInterruptedTasks (ComServer comServer);

    /**
     * Cleans up any marker flags on ComTaskExecutions
     * that are running longer than the task execution timeout
     * that is specified on the OutboundComPortPool
     * of the OutboundComPort it is running on.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called frequently by the ComServer
     * and returns the best time between calls.
     *
     * @param comServer The ComServer
     * @return The best time to wait before making another call
     */
    public TimeDuration releaseTimedOutTasks (ComServer comServer);


    /**
     * Creates a new ComSession from the specifications laid out
     * in the ComSessionShadow for an outbound communication session.
     *
     * @param builder The ComSessionShadow
     * @return The newly created ComSession
     */
    public ComSession createComSession(ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator);

    /**
     * Stores the given list of Reading readings on the Meter.
     *
     * @param deviceIdentifier the identifier of the Device
     * @param meterReading the readings to store
     */
    public void storeMeterReadings(DeviceIdentifier<Device> deviceIdentifier, MeterReading meterReading);

    /**
     * Finds the OfflineDevice that is uniquely identified
     * by the specified {@link DeviceIdentifier}.
     *
     * @param identifier The DeviceIdentifier
     * @param offlineDeviceContext  the offlineContext identifying what needs to be offline
     * @return The offline version of the Device that is identified by the DeviceIdentifier
     */
    public OfflineDevice findOfflineDevice(DeviceIdentifier<?> identifier, OfflineDeviceContext offlineDeviceContext);

    /**
     * Finds the BaseRegister that is uniquely identified
     * by the specified RegisterIdentifier.
     *
     * @param identifier The RegisterIdentifier
     * @return The offline version of the Register that is identified by the RegisterIdentifier
     */
    public OfflineRegister findOfflineRegister(RegisterIdentifier identifier);

    public OfflineLoadProfile findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    public OfflineLogBook findOfflineLogBook(LogBookIdentifier logBookIdentifier);

    /**
     * Updates the ip address of the BaseDevice device
     * that is configured in the specified ConnectionTask
     * only when the value has actually changed.
     *
     * @param ipAddress The new ip address
     * @param connectionTask The ConnectionTask
     * @param connectionTaskPropertyName The name of the ConnectionTask's property that holds the ip address
     */
    public void updateIpAddress (String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName);

    /**
     * Updates a protocol property of the BaseDevice
     * that is uniquely identified by the specified identifier with the given value
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param propertyName     The name of the generic communication property
     * @param propertyValue    The new property value
     */
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    /**

    /**
     * Updates the gateway device of the BaseDevice device
     * that is uniquely identified by the specified identifier
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param gatewayDeviceIdentifier The device identifier of the new gateway device or null (to be used in case no gateway is present)
     */
    public void updateGateway (DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier);

    /**
     * Store configuration information of a BaseDevice device
     * in a UserFile alongside that device,
     * i.e. the UserFile will be stored
     * in the same parent folder.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param timeStampFormat The preferred DateFormat that should be used for the
     *                        current date and time when that should be necessary
     *                        to create a unique name for the UserFile name.
     * @param fileExtension The extension for the UserFile
     * @param contents The contents of the UserFile   @see UserFile#getExtension()
     */
    public void storeConfigurationFile (DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileExtension, byte[] contents);

    /**
     * Signals the occurrence of an event.
     *
     * @param topic The name of the event topic where the event will be published
     * @param source The source that produced the event and that also holds the event data that will be published
     */
    public void signalEvent (String topic, Object source);

    /**
     * Updates the new DeviceStatus and protocolInformation
     * of the DeviceMessage DeviceMessage
     * which is identified by the given MessageIdentifier.
     *
     * @param messageIdentifier        the messageIdentifier
     * @param newDeviceMessageStatus the status to update the message to
     * @param protocolInformation    the protocolInformation to add to the DeviceMessage
     */
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, String protocolInformation);

    /**
     * Tests if the ComTaskExecution that is uniquely identified
     * by the specified id, is still TaskStatus#Pending.
     *
     * @param comTaskExecutionId The ComTaskExecution
     * @return A flag that indicates if the ComTaskExecution is still pending
     */
    public boolean isStillPending (long comTaskExecutionId);

    /**
     * Tests if all of the ComTaskExecutions are still TaskStatus#Pending.
     *
     * @param comTaskExecutionIds The collection of ComTaskExecution identifiers
     * @return A flag that indicates if all of the ComTaskExecution are still pending
     */
    public boolean areStillPending (Collection<Long> comTaskExecutionIds);

    /**
     * Executes the given Transaction
     *
     * @param transaction the transaction to execute
     */
    public <T> T executeTransaction(Transaction<T> transaction);

    public DeviceIdentifier<Device> getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier);

    public DeviceIdentifier<Device> getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier);

    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading);

    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook);

    public void storePathSegments(DeviceIdentifier sourceDeviceIdentifier, List<TopologyPathSegment> topologyPathSegments);

    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours);

    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation);

    void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions);
}
