/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.ProfileData;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
public interface ComServerDAO extends com.energyict.mdc.upl.InboundDAO, ServerProcess {

    /**
     * Gets the ComServer that relates to the machine where this code is running on.
     *
     * @return The ComServer that relates to this machine or <code>null</code> if the host name of
     * the machine is <strong>NOT</strong> the name of a registered ComServer.
     */
    ComServer getThisComServer();

    /**
     * Fetch some information about the users from the database (user name, allowedToUseMobileComServer, salt and hash of the password)
     */
    List<OfflineUserInfo> getUsersCredentialInformation();

    default Optional<OfflineUserInfo> checkAuthentication(String loginPassword) {
        return Optional.empty();
    }

    /**
     * Gets the {@link DeviceProtocolSecurityPropertySet} that has been
     * created against the Device that is currently connected to the ComServer
     * via the specified {@link InboundComPort}.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @param inboundComPort   The InboundComPort
     * @return The DeviceProtocolSecurityPropertySet or null if the Device is not ready for inbound communication
     */
    DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);


    com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort);


    List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId);

    /**
     * Returns the dialect properties of the first comtask of a given device or <code>null</code>.
     */
    com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);

    /**
     * Gets the {@link TypedProperties} that have been
     * created against the {@link ConnectionTask}
     * that is currently used to connect the Device to the ComServer
     * via the specified {@link InboundComPort}.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @param inboundComPort   The InboundComPort
     * @return The TypedProperties or <code>null</code> if the Device is not ready for inbound communication
     */
    com.energyict.mdc.upl.properties.TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);

    /**
     * Finds the {@link com.energyict.mdc.upl.meterdata.Device} that is uniquely identified
     * by the specified {@link DeviceIdentifier}.
     *
     * @param identifier The DeviceIdentifier
     * @return The offline version of the Device that is identified by the DeviceIdentifier
     */
    Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier);

    /**
     * Gets the ComServer that relates to the machine with the specified host name.
     *
     * @return The ComServer that relates to the machine with the specified host name
     * or <code>null</code> if there is no such ComServer.
     */
    ComServer getComServer(String systemName);

    /**
     * Refreshes the specified ComServer, i.e. checks for updates
     * to the object and returns a new version if there were any changes.
     * Returns exactly the same object if there are no changes.
     * Returns <code>null</code> if the ComServer no longer exists because it was deleted or made obsolete
     *
     * @param comServer The ComServer
     * @return The new version of the ComServer,
     * <code>null</code> if the ComServer was deleted or made obsolete or
     * exactly the same Comserver if no changes were found
     */
    ComServer refreshComServer(ComServer comServer);

    /**
     * Refreshes the specified {@link ComPort}, i.e. checks for updates
     * to the object and returns a new version if there were any changes.
     * Returns exactly the same object if there are no changes.
     * Returns <code>null</code> if the ComPort no longer exists because it was deleted or made obsolete
     *
     * @param comPort The ComPort
     * @return The new version of the ComPort,
     * <code>null</code> if the ComPort was deleted or made obsolete or
     * exactly the same Comport if no changes were found
     */
    ComPort refreshComPort(ComPort comPort);

    /**
     * Finds and returns all the ComJobs that are in
     * pending state by the specified OutboundComPort.
     *
     * @param comPort The OutboundComPort
     * @return The List of ComJobs that represent all the ComTaskExecutions that are ready to be executed
     * @see OutboundComPort
     */
    List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort);

    /**
     * Finds and returns all the ComJobs that are ready
     * to be executed by the specified OutboundComPort.
     * Depends on the simultaneous connection number
     *
     * @param comPort The OutboundComPort
     * @return The List of ComJobs that represent all the ComTaskExecutions that are ready to be executed
     * @see OutboundComPort
     */
    List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort);

    /**
     * Finds and returns all the ComTasks that are ready
     * to be executed by the specified ComServer.
     *
     * @param comServer The ComServer
     * @param delta     The delta time between now and nextExecutionTime
     * @param limit     The limit of entries to return. If limit =0 not limit will be applied
     * @param skip      The nr of entries to be skipped. Useful just if limit > 0
     * @return The List of ComTaskExecutions that are ready to be executed
     * @see ComServer
     */
    default List<ComTaskExecution> findExecutableOutboundComTasks(ComServer comServer, Duration delta, long limit, long skip) {
        return Lists.newArrayList();
    }

    /**
     * Finds and returns the {@link HighPriorityComJob}s that are ready
     * to be executed by the specified {@link ComServer}.<br/>
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param comServer                             The ComServer
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @return The List of {@link ComJob}s that represent all the ComTaskExecutions that are ready to be executed
     */
    List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool);

    /**
     * Finds and returns the {@link HighPriorityComJob}s that are ready
     * to be executed by the specified {@link ComServer} on the specified date.<br/>
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param comServer                             The ComServer
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @param date                                  the date for when {@link HighPriorityComJob}s should be searched
     * @return The List of {@link ComJob}s that represent all the ComTaskExecutions that are ready to be executed
     */
    List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date);

    /**
     * Finds and returns all the ComTaskExecutions that are ready
     * to be executed against the specified Device
     * on the specified InboundComPort.
     * Note that when the Device is not configured for inbound communication
     * because it does not have an InboundConnectionTask
     * or when none of the ComTaskExecutions are ready to execute, i.e. their state is not
     * TaskStatus#Pending, an empty list is returned.
     *
     * @param device  The device
     * @param comPort The InboundComPort
     * @return The List of ComTaskExecutions that are ready to be executed
     */
    List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice device, InboundComPort comPort);

    /**
     * Gets the {@link PropertyValueType} of the protocol property of the {@link Device}
     * that is uniquely identified by the specified {@link DeviceIdentifier}.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param propertyName     The name of the protocol property
     * @return The PropertyValueType
     */
    PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName);

    /**
     * Finds the {@link ConnectionTaskProperty connection properties}
     * for the specified {@link ConnectionTask}.
     *
     * @param connectionTask The ConnectionTask
     * @return The List of ConnectionTaskProperty
     */
    List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask);

    /**
     * Finds the ComTaskEnablement that enables the execution of the specified ComTask against the given Device.
     *
     * @param deviceIdentifier The identifier of the device
     * @param comTaskId        The ID of the ComTask
     */
    ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId);

    /**
     * Finds all the {@link SecurityPropertySet}s owned by the specified {@link Device}
     *
     * @param deviceIdentifier The identifier of the device to search the SecurityPropertySets for
     * @return all SecurityPropertySets owned by the Device
     */
    List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier);

    /**
     * Gets the protocol dialect properties  for the given {@link ComTaskExecution}
     * Note that for a slave comtask, the dialect of the master connection is used.
     *
     * @param comTaskExecutionId The ID of the ComTaskExecution
     * @return The TypedProperties of the protocol dialect properties
     */
    TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId);

    /**
     * Attempts to lock the ScheduledConnectionTask for
     * its execution on the specified ComPort
     * and returns the locked ScheduledConnectionTask when the lock succeeds
     * and <code>null</code> when the lock fails.
     * Make sure to release the lock in a finally block
     * to avoid that the ScheduledConnectionTask remains
     * locked for ever as that would mean it will
     * never execute again.
     *
     * @param connectionTask The OutboundConnectionTask
     * @param comPort        The ComPort
     * @return <code>true</code> iff the lock succeeds
     */
    ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort);

    boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort);

    /**
     * Unlocks the OutboundConnectionTask, basically undoing the effect
     * of the attemptLock method providing that was successful.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    void unlock(OutboundConnectionTask connectionTask);

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
     * @param comPort          The ComPort
     * @return <code>true</code> iff the lock succeeds
     */
    boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort);

    /**
     * Attempts to lock the specified {@link PriorityComTaskExecutionLink}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing it.
     *
     * @param comTaskExecution The HighPriorityComTaskExecution
     * @return A flag that indicates a successful locking of the HighPriorityComTaskExecution
     */
    boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort);

    /**
     * Unlocks the ComTaskExecution, basically undoing the effect
     * of the attemptLock method providing that was successful.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    void unlock(ComTaskExecution comTaskExecution);

    /**
     * Notifies that execution of the specified OutboundConnectionTask
     * was started by the specified ComPort.
     *
     * @param connectionTask The OutboundConnectionTask
     * @param comPort        The ComPort that started the execution
     */
    ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort);

    /**
     * Notifies that execution of the specified OutboundConnectionTask completed.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask);

    /**
     * Notifies that execution of the specified OutboundConnectionTask failed.
     *
     * @param connectionTask The OutboundConnectionTask
     * @return the updated connectiontask
     */
    ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask);

    /**
     * Notifies that execution of the specified OutboundConnectionTask was interrupted
     * and needs to be rescheduled.
     *
     * @param connectionTask The OutboundConnectionTask
     */
    ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask);

    /**
     * Notifies that execution of the specified ComTaskExecution has been started
     * on the specified ComPort.
     *
     * @param comTaskExecution     The ComTaskExecution
     * @param comPort              The ComPort that has started the execution of the ComTaskExecution
     * @param executeInTransaction A flag that indicates if a transaction is needed or not
     */
    void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction);

    /**
     * Notifies that execution of the specified ComTaskExecution completed.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    void executionCompleted(ComTaskExecution comTaskExecution);

    /**
     * Notifies that the execution of the specified ComTaskExecution was postponed and needs to be rescheduled
     *
     * @param comTaskExecution the ComTaskExecution
     * @param rescheduleDate   The timestamp on which the task should be rescheduled for execution
     */
    void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate);

    /**
     * Notifies that the execution of the specified ComTaskExecution was postponed and needs to be rescheduled based on the
     * communication window start date.
     * The number of tries is not incremented.
     *
     * @param comTaskExecution   the ComTaskExecution
     * @param comWindowStartDate the communication window start date on which the task should be rescheduled (additional restrictions can be applicable)
     */
    void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate);

    /**
     * Notifies that execution of the specified ComTaskExecutions completed.
     *
     * @param comTaskExecutions The List of completed ComTaskExecution
     */
    void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions);

    /**
     * Notifies that execution of the specified ComTaskExecution failed.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    void executionFailed(ComTaskExecution comTaskExecution);

    void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry);

    /**
     * Notifies that execution of the specified ComTaskExecution failed.
     *
     * @param comTaskExecutions The List of failed ComTaskExecution
     */
    void executionFailed(List<? extends ComTaskExecution> comTaskExecutions);

    /**
     * Cleans up any marker flags on ComTaskExecutions
     * that were not properly cleaned because the ComPort they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComPort from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comPort The ComPort that is currently starting up.
     */
    void releaseInterruptedTasks(ComPort comPort);

    /**
     * Cleans up any marker flags on ComTaskExecutions
     * that are running longer than the task execution timeout
     * that is specified on the OutboundComPortPool
     * of the OutboundComPort it is running on.
     * Leaving the marker flags, prohibits the ComPort from
     * picking up the tasks again.
     * This is intended to be called frequently by the ComPort
     * and returns the best time between calls.
     *
     * @param comPort The ComPort
     * @return The best time to wait before making another call
     */
    TimeDuration releaseTimedOutTasks(ComPort comPort);

    /**
     * Release the ComTasks and the ConnectionTasks which are locked by the given ComPort
     *
     * @param comPort the comport for which the tasks should be unlocked
     */
    void releaseTasksFor(ComPort comPort);

    /**
     * Creates a new ComSession from the specifications laid out
     * in the ComSessionShadow for an outbound communication session.
     *
     * @param builder The ComSessionShadow
     * @return The newly created ComSession
     */
    ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator);

    /**
     * Creates a new {@link UpdatedDeviceCache} from the specifications laid
     * out in the {@link DeviceProtocolCache} or updates the DeviceCache
     * that already exists for the {@link Device}
     * with the specified identifier.
     *
     * @param deviceIdentifier
     * @param cache            The DeviceProtocolCache
     */
    void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache);

    /**
     * Stores the given list of Reading readings on the Meter.
     *
     * @param deviceIdentifier the identifier of the Device
     * @param meterReading     the readings to store
     */
    void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading);

    /**
     * Stores the collected {@link ProfileData} in the {@link LoadProfile}
     * which is specified by the given {@link LoadProfileIdentifier}
     *
     * @param loadProfileIdentifier The LoadProfileIdentifier which uniquely identifies the LoadProfile
     * @param collectedLoadProfile  The collectedLoadProfile, containing the collected ProfileData
     * @param currentDate
     */

    void storeLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate);

    /**
     * Stores the {@link CollectedLogBook} in the specified {@link  LogBook}
     * which is uniquely identified by the given {@link LogBookIdentifier}
     *
     * @param logBookIdentifier The LogBookIdentifier which uniquely identifies the LogBook
     * @param collectedLogBook  The CollectedLogBook, containing the list of collected MeterProtocolEvents
     * @param currentDate
     */
    void storeLogBookData(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate);

    /**
     * Updates the last reading date of a {@link LogBook} which is uniquely identified by
     * the given {@link LogBookIdentifier}
     *
     * @param logBookIdentifier
     * @param lastExecutionStartTimestamp
     */
    void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date lastExecutionStartTimestamp);

    /**
     * Updates the last reading date of a {@link LogBook} which is uniquely identified by
     * the given {@link LogBookIdentifier} with the start time from the com task execution identified by its id
     *
     * @param logBookIdentifier
     * @param comTaskExecutionId
     */
    void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, long comTaskExecutionId);

    /**
     * Finds the OfflineDevice that is uniquely identified
     * by the specified {@link DeviceIdentifier}.
     *
     * @param identifier           The DeviceIdentifier
     * @param offlineDeviceContext the offlineContext identifying what needs to be offline
     * @return The offline version of the Device that is identified by the DeviceIdentifier
     */
    Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext);

    /**
     * Finds the Register that is uniquely identified
     * by the specified RegisterIdentifier.
     *
     * @param identifier The RegisterIdentifier
     * @return The offline version of the Register that is identified by the RegisterIdentifier
     */
    Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when);

    Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier);

    /**
     * Finds the <b>offline</b> version of the {@link DeviceMessage}
     * that is uniquely identified by the specified {@link com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier}.
     *
     * @param identifier The MessageIdentifier
     * @return The <b>offline</b> version of the DeviceMessage that is identified by the MessageIdentifier
     * or null if the DeviceMessage could not be found
     */
    Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier);

    /**
     * Updates the connection task property of the Device device
     * that is configured in the specified ConnectionTask
     * only when the value has actually changed.
     *
     * @param propertyValue              The new property value
     * @param connectionTask             The ConnectionTask
     * @param connectionTaskPropertyName The name of the ConnectionTask's property that holds the ip address
     */
    void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName);

    /**
     * Updates the connection task property of the Device device
     * that is configured in the specified ConnectionTask
     * only when the value has actually changed.
     *
     * @param connectionTask             The ConnectionTask
     * @param connectionPropertyNameAndValue The map of ConnectionTask's properties that holds the ip address
     */
    void updateConnectionTaskProperties(ConnectionTask connectionTask, Map<String, Object> connectionPropertyNameAndValue);

    /**
     * Updates a protocol property of the Device
     * that is uniquely identified by the specified identifier with the given value.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param propertyName     The name of the generic communication property
     * @param propertyValue    The new property value
     */
    void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    /**
     * Updates a dialect property of the Device
     * that is uniquely identified by the specified identifier with the given value.
     * <p>
     * Note that, if multiple dialects contain the given propertyName, both properties will be updated.
     */
    void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    /**
     * Updates a security property of the Device
     * that is uniquely identified by the specified identifier with the given value.
     * <p>
     * Note that, if multiple security sets contain the given propertyName, both properties will be updated.
     */
    void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    void addTrustedCertificates(List<CollectedCertificateWrapper> collectedCertificates);

    /**
     * Add/update the given sub-CA or root-CA certificate in the persisted DLMS trust store, for the given alias.
     */
    void addCACertificate(CertificateWrapper certificateWrapper);

    /**
     * Add the given server end-device certificate as a certificate wrapper.
     * Returns the database ID of the created {@link com.energyict.mdc.upl.security.CertificateWrapper}
     */
    long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper);

    void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution);

    void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution);

    /**
     * Updates the gateway device of the Device device
     * that is uniquely identified by the specified identifier.
     *
     * @param deviceIdentifier        The DeviceIdentifier
     * @param gatewayDeviceIdentifier The device identifier of the new gateway device or null (to be used in case no gateway is present)
     */
    void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier);

    /**
     * Store configuration information of a Device device
     * in a UserFile alongside that device,
     * i.e. the UserFile will be stored
     * in the same parent folder.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param timeStampFormat  The preferred DateFormat that should be used for the
     *                         current date and time when that should be necessary
     *                         to create a unique name for the UserFile name.
     * @param fileName         The name of the UserFile
     * @param fileExtension    The extension for the UserFile
     * @param contents         The contents of the UserFile   @see UserFile#getExtension()
     */
    void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents);

    /**
     * Signals the occurrence of an event.
     *
     * @param topic  The name of the event topic where the event will be published
     * @param source The source that produced the event and that also holds the event data that will be published
     */
    void signalEvent(String topic, Object source);

    /**
     * Updates the new DeviceStatus and protocolInformation
     * of the DeviceMessage DeviceMessage
     * which is identified by the given MessageIdentifier.
     *
     * @param messageIdentifier      the messageIdentifier
     * @param newDeviceMessageStatus the status to update the message to
     * @param sentDate               the date&time the message was sent to the device - if this message was not yet sent to the device, this could be null
     * @param protocolInformation    the protocolInformation to add to the DeviceMessage
     */
    void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation);

    /**
     * Tests if the ComTaskExecution that is uniquely identified
     * by the specified id, is still TaskStatus#Pending.
     *
     * @param comTaskExecutionId The ComTaskExecution
     * @return A flag that indicates if the ComTaskExecution is still pending
     */
    boolean isStillPending(long comTaskExecutionId);

    /**
     * Tests if all of the ComTaskExecutions are still TaskStatus#Pending.
     *
     * @param comTaskExecutionIds The collection of ComTaskExecution identifiers
     * @return A flag that indicates if all of the ComTaskExecution are still pending
     */
    boolean areStillPending(Collection<Long> comTaskExecutionIds);

    /**
     * Tests if all of the {@link PriorityComTaskExecutionLink}s are still {@link com.energyict.mdc.common.tasks.TaskStatus#Pending}.
     *
     * @param priorityComTaskExecutionLinkIds The collection of HighPriorityComTaskExecution identifiers
     * @return A flag that indicates if all of the HighPriorityComTaskExecution are still pending
     */
    boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionLinkIds);

    /**
     * Executes the given Transaction.
     *
     * @param transaction the transaction to execute
     */
    <T> T executeTransaction(Transaction<T> transaction);

    DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier);

    DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier);

    /**
     * Resolve a given deviceIdentifier using the deviceService
     */
    Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier);

    /**
     * Return all devices matching the given deviceIdentifier
     */
    List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier);

    void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading);

    void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook);

    void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> lastReadings, Map<LogBookIdentifier, Instant> lastLogBooks);

    void storePathSegments(List<TopologyPathSegment> topologyPathSegments);

    void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours);

    void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation);

    void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions);

    void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired);

    void updateCreditAmount(CollectedCreditAmount collectedCreditAmount, boolean registerUpdateRequired, boolean tableUpdateRequired);

    void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr);

    void updateCalendars(CollectedCalendar collectedCalendar);

    /**
     * Gets the onHold property for the inbound com task
     * created against the Device that is currently connected to the ComServerc
     * via the specified {@link InboundComPort}.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @param inboundComPort   The InboundComPort
     * @return the onHold property vale
     */
    Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);

    /**
     * Fetch the lookup table "comServerMobile_completionCodes"
     */
    List<LookupEntry> getCompletionCodeLookupEntries();

    /**
     * Request cleanup of all outdated {@link ComTaskExecutionTrigger}s<br/>
     * More specific, all ComTaskExecutionTriggers who have a trigger date more than 1 day in the past will be removed from the database
     */
    void cleanupOutdatedComTaskExecutionTriggers();

    List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile loadProfile, String readingTypeMRID, Range<Instant> dataPeriod);

    User getComServerUser();

    List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort);

    void updateUmiwanStructure(ComTaskExecution comTaskExecution, Map<String, Object> properties, String cas);
}