/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionGroup;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.impl.core.offline.OfflineActionExecuter;
import com.energyict.mdc.engine.impl.core.offline.OfflineActions;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
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

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that will post a JSon representation of the query
 * to a companion {@link OnlineComServer}
 * that has a servlet running that will listen for these queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:56)
 */
public class OfflineComServerDAOImpl implements ComServerDAO {

    /**
     * The comserver business object that is configured in Connexo and represents the mobile application
     */
    private ComServer comServer;
    /**
     * The service provider for the offline comserver
     */
    private final ServiceProvider serviceProvider;
    /**
     * Offline actions executer
     */
    OfflineActionExecuter offlineActionExecuter;
    /**
     * The login user
     */
    private final User comServerUser;
    /**
     * The server process for the offline comserver
     */
    private ServerProcess serverProcess;
    /**
     * Status of this DAO
     */
    private ServerProcessStatus status = ServerProcessStatus.STARTING;
    /**
     * The model that holds all information relevant to the (offline) execution of one comjob
     * Null means that currently no comjobs should be executed by the running comserver
     */
    private ComJobExecutionModel comJobExecutionModel = null;
    /**
     * Blocking queue that holds the pending work.
     * When a user chooses to execute a comjob in the UI, it is added to this queue.
     * After that, it is immediately taken from the queue by the running comserver.
     */
    private final BlockingQueue<ComJob> jobQueue = new ArrayBlockingQueue<>(1);
    /**
     * Blocking queue that holds the finished work
     */
    private final BlockingQueue<ComJobExecutionModel> finishedQueue = new ArrayBlockingQueue<>(1);
    /**
     * Mark is the status of the event monitor
     */
    private boolean eventMonitorIsStarted = false;

    public OfflineComServerDAOImpl(ComServer comServer, ServiceProvider serviceProvider, User comServerUser, OfflineActionExecuter offlineActionExecuter) {
        this.comServer = comServer;
        this.serviceProvider = serviceProvider;
        this.comServerUser = comServerUser;
        this.offlineActionExecuter = offlineActionExecuter;
    }

    public void setComServer(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public ComServer getThisComServer() {
        return comServer;
    }

    @Override
    public ComServer getComServer(String hostName) {
        if (getThisComServer().getName().equals(hostName)) {
            return getThisComServer();
        }
        return null;
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        return comServer;
    }


    public List<OfflineUserInfo> getUsersCredentialInformation() {
        return Collections.emptyList(); //Not used in offline DAO
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        return comPort;
    }

    public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
        if (eventMonitorIsStarted) {
            try {
                if (!getJobQueue().isEmpty()) {
                    ComJob work = getJobQueue().take();
                    return Arrays.asList(work);
                } else {
                    return Collections.emptyList();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        if (eventMonitorIsStarted) {
            try {
                if (!getJobQueue().isEmpty()) {
                    ComJob work = getJobQueue().take();
                    return Arrays.asList(work);
                } else {
                    return Collections.emptyList();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
        return null;
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
        return null;
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice device, InboundComPort comPort) {
        return Collections.emptyList();
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask) {
        return connectionTask.getProperties();
    }

    @Override
    public List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId) {
        return Collections.emptyList();
    }

    @Override
    public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId) {
        return null;
    }

    @Override
    public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
        return Collections.emptyList();
    }

    @Override
    public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
        return getComJobExecutionModel().findProtocolDialectPropertiesFor(comTaskExecutionId);
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort) {
        return connectionTask;
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
        return true;
    }

    @Override
    public void unlock(OutboundConnectionTask connectionTask) {
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        return true;
    }

    @Override
    public boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort) {
        return true;
    }

    @Override
    public void unlock(ComTaskExecution comTaskExecution) {
    }

    @Override
    public ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort) {
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask) {
        comJobExecutionModel.setResult(ComJobResult.Success);
        comJobExecutionModel.setConnectionTaskSuccess(true);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask) {
        return connectionTask;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return transaction.perform();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return loadProfileIdentifier.getDeviceIdentifier();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return logBookIdentifier.getDeviceIdentifier();
    }

    @Override
    public PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName) {
        return null;
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
    }

    @Override
    public void addCACertificate(CertificateWrapper certificateWrapper) {
    }

    @Override
    public void addTrustedCertificates(List<CollectedCertificateWrapper> collectedCertificates) {
    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return 0;
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution) {
    }

    @Override
    public void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution) {
    }

    @Override
    public Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        getComJobExecutionModel().updateLogBookLastReading(logBookIdentifier, Date.from(lastLogBook));
    }

    @Override
    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> lastReadings, Map<LogBookIdentifier, Instant> lastLogBooks) {
        getComJobExecutionModel().addLoadProfileUpdate(lastReadings);
        getComJobExecutionModel().addLogBookUpdate(lastLogBooks);
    }

    @Override
    public void storePathSegments(List<TopologyPathSegment> topologyPathSegment) {
    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
    }

    @Override
    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {
    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {
    }

    @Override
    public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {
    }

    @Override
    public void updateCreditAmount(CollectedCreditAmount collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {
    }

    @Override
    public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {
    }

    @Override
    public void updateCalendars(CollectedCalendar collectedCalendar) {
    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        return false;
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {
    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        getComJobExecutionModel().setComTaskExecutionStartTime(comTaskExecution, convertToDateViaSqlTimestamp(LocalDateTime.now()));
    }

    @Override
    public void executionCompleted(ComTaskExecution comTaskExecution) {
        comJobExecutionModel.setResult(ComJobResult.Success);
        comJobExecutionModel.addSuccessfulComTaskExecution(comTaskExecution, false);
    }

    @Override
    public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
        comJobExecutionModel.setResult(ComJobResult.Failed);
        comJobExecutionModel.setConnectionTaskSuccess(true);
        return connectionTask;
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        comJobExecutionModel.setResult(ComJobResult.Failed);
        comJobExecutionModel.addFailedComTaskExecution(comTaskExecution, false);
    }

    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        comJobExecutionModel.setResult(ComJobResult.Failed);
        comJobExecutionModel.addFailedComTaskExecution(comTaskExecution, false);
    }

    @Override
    public void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comJobExecutionModel.setResult(ComJobResult.Success);
            comJobExecutionModel.addSuccessfulComTaskExecution(comTaskExecution, false);
        }
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution) {
        comJobExecutionModel.setResult(ComJobResult.Failed);
        comJobExecutionModel.addFailedComTaskExecution(comTaskExecution, false);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry) {
        executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed(List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comJobExecutionModel.setResult(ComJobResult.Failed);
            comJobExecutionModel.addFailedComTaskExecution(comTaskExecution, false);
        }
    }

    @Override
    public void releaseInterruptedTasks(ComPort comPort) {
    }

    @Override
    public TimeDuration releaseTimedOutTasks(ComPort comPort) {
        return new TimeDuration(0);
    }

    @Override
    public void releaseTasksFor(ComPort comPort) {
    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        getComJobExecutionModel().createComSession(builder, stopDate, successIndicator);
        jobIsFinished();
        return null;
    }

    /**
     * Notify the GUI that the comjob is finished.
     */
    private void jobIsFinished() {
        try {
            getFinishedQueue().put(comJobExecutionModel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //Add an action for the worker thread to pick up the finished comjob and update the UI
        offlineActionExecuter.addAction(OfflineActions.ComJobIsFinished);
    }

    @Override
    public void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
        getComJobExecutionModel().setDeviceCache(cache);
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        getComJobExecutionModel().addMeterReading(deviceIdentifier, meterReading);
    }

    @Override
    public void storeLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
        getComJobExecutionModel().addCollectedLoadProfile(loadProfileIdentifier, collectedLoadProfile);
        getComJobExecutionModel().updateLoadProfileReadDate(loadProfileIdentifier, Date.from(currentDate));
    }

    @Override
    public void storeLogBookData(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate) {
        getComJobExecutionModel().addCollectedLogBook(logBookIdentifier, collectedLogBook);
        getComJobExecutionModel().updateLogBookReadDate(logBookIdentifier, Date.from(currentDate));
    }

    @Override
    public void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date lastExecutionStartTimestamp) {
        getComJobExecutionModel().updateLogBookLastReading(logBookIdentifier, lastExecutionStartTimestamp);
    }

    public void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, final long comTaskExecutionId) {
        getComJobExecutionModel().updateLogBookLastReading(logBookIdentifier, new Date(getComJobExecutionModel().getComTaskExecutionStartTimes().get(comTaskExecutionId)));
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return findOfflineDevice(identifier, DeviceOffline.needsEverything);
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        return Optional.of(getComJobExecutionModel().findOfflineDevice(this, identifier));
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        return Optional.of(getComJobExecutionModel().findOfflineRegister(this, identifier));
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return Optional.of(getComJobExecutionModel().findOfflineLoadProfile(this, loadProfileIdentifier));
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        return Optional.of(getComJobExecutionModel().findOfflineLogBook(this, logBookIdentifier));
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        return Optional.of(getComJobExecutionModel().findOfflineDeviceMessage(this, identifier));
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateConnectionTaskProperties(ConnectionTask connectionTask, Map<String, Object> connectionPropertyNameAndValue) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        new UnsupportedOperationException();
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        getComJobExecutionModel().updateGatewayOfDevice(deviceIdentifier, gatewayDeviceIdentifier);
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
    }

    @Override
    public void signalEvent(String topic, Object source) {
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        getComJobExecutionModel().addCollectedDeviceMessageInformation(messageIdentifier, newDeviceMessageStatus, sentDate, protocolInformation);
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
        return getComJobExecutionModel().findOfflineDevice(this, deviceIdentifier);
    }

    @Override
    public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        return true;
    }

    @Override
    public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionLinkIds) {
        return false;
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        return true;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    @Override
    public void start() {
        this.status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown() {
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    @Override
    public void shutdownImmediate() {
        this.shutdown();
    }

    @Override
    public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile loadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
        return null;
    }

    @Override
    public User getComServerUser() {
        return comServerUser;
    }

    public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
        return null;    // Not used in mobile DAO, as only used for scheduling of high priority tasks
    }

    @Override
    public void updateUmiwanStructure(ComTaskExecution comTaskExecution, Map<String, Object> properties, String cas) {
        // TODO: 19.06.2021
    }

    public BlockingQueue<ComJob> getJobQueue() {
        return jobQueue;
    }

    public BlockingQueue<ComJobExecutionModel> getFinishedQueue() {
        return finishedQueue;
    }

    public void notifyEventMonitorIsStarted() {
        eventMonitorIsStarted = true;
    }

    /**
     * When the user chooses to execute a comjob (in the UI), this setter is called.
     * This populates all the relevant comjob information in this DAO so it can be used to executed the comjob offline.
     * <p/>
     * Also, the job is added to the jobQueue, it will immediately be taken by the running comserver.
     */
    public void setComJobExecutionModel(ComJobExecutionModel comJobExecutionModel) {
        this.comJobExecutionModel = comJobExecutionModel;
        try {
            comJobExecutionModel.forceResult(ComJobResult.Pending); //Reset to pending, this could be a retry
            getJobQueue().put(createComJob(comJobExecutionModel));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Create the proper ComJob from the given model.
     * Filter out the MMR tasks, these should not be executed (nor logged) by the comserver framework.
     */
    private ComJob createComJob(ComJobExecutionModel comJobExecutionModel) {
        ComJob original = comJobExecutionModel.getComJob();
        ComTaskExecutionGroup comTaskExecutionGroup = new ComTaskExecutionGroup(original.getConnectionTask());
        List<ComTaskExecution> filteredComTaskExecutions = new ArrayList<>();
        for (ComTaskExecution originalComTaskExecution : original.getComTaskExecutions()) {
            if (!comJobExecutionModel.isMMROnly(originalComTaskExecution)) {
                comTaskExecutionGroup.add(originalComTaskExecution);
            }
        }
        return comTaskExecutionGroup;
    }

    private Date convertToDateViaSqlTimestamp(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }

    /**
     * The model that holds all information relevant to the (offline) execution of one comjob
     */
    public ComJobExecutionModel getComJobExecutionModel() {
        return comJobExecutionModel;
    }

    @Override
    public List<LookupEntry> getCompletionCodeLookupEntries() {
        return null;
    }

    public interface ServiceProvider {

        Clock clock();

        EngineConfigurationService engineConfigurationService();

        ThreadPrincipalService threadPrincipalService();

    }
}