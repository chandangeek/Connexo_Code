package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.offline.*;
import com.energyict.mdc.engine.impl.core.*;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.protocol.api.device.data.TopologyNeighbour;
import com.energyict.mdc.protocol.api.device.data.TopologyPathSegment;
import com.energyict.mdc.protocol.api.device.data.identifiers.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.*;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides a default implementation for the {@link ComServerDAO} interface
 * that uses the EIServer persistence framework for all requests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-07 (08:58)
 */
public class ComServerDAOImpl implements ComServerDAO {

    public interface ServiceProvider {

        Clock clock();

        EngineConfigurationService engineConfigurationService();

        ConnectionTaskService connectionTaskService();

        CommunicationTaskService communicationTaskService();

        DeviceService deviceService();

        TopologyService topologyService();

        EngineService engineService();

        TransactionService transactionService();

        EventService eventService();

        IdentificationService identificationService();

        FirmwareService firmwareService();
    }

    private final ServiceProvider serviceProvider;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;

    public ComServerDAOImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    private EngineConfigurationService getEngineModelService() {
        return this.serviceProvider.engineConfigurationService();
    }

    private DeviceService getDeviceDataService() {
        return this.serviceProvider.deviceService();
    }

    private CommunicationTaskService getCommunicationTaskService() {
        return this.serviceProvider.communicationTaskService();
    }

    private ConnectionTaskService getConnectionTaskService() {
        return this.serviceProvider.connectionTaskService();
    }

    private Clock getClock() {
        return this.serviceProvider.clock();
    }

    private TransactionService getTransactionService() {
        return this.serviceProvider.transactionService();
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
    public ComServer getThisComServer() {
        return getEngineModelService().findComServerBySystemName().orElse(null);
    }

    @Override
    public ComServer getComServer(String systemName) {
        return this.getEngineModelService().findComServer(systemName).orElse(null);
    }

    private class OfflineDeviceServiceProvider implements OfflineDeviceImpl.ServiceProvider {

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public Optional<DeviceCache> findProtocolCacheByDevice(Device device) {
            return serviceProvider.engineService().findDeviceCacheByDevice(device);
        }

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        Optional<ComServer> reloaded = getEngineModelService().findComServer(comServer.getId());
        if (reloaded.isPresent()) {
            if (reloaded.get().isObsolete()) {
                return null;
            } else if (reloaded.get().getVersion() > comServer.getVersion()) {
                return reloaded.get();
            } else {
                return comServer;
            }
        } else {
            return null;
        }
    }

    private ComJobFactory getComJobFactoryFor(OutboundComPort comPort) {
        // Zero is not allowed, i.e. rejected by the OutboundComPortImpl validation methods
        if (comPort.getNumberOfSimultaneousConnections() == 1) {
            return new SingleThreadedComJobFactory();
        } else {
            return new MultiThreadedComJobFactory(comPort.getNumberOfSimultaneousConnections());
        }
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        try (Fetcher<ComTaskExecution> comTaskExecutions = getCommunicationTaskService().getPlannedComTaskExecutionsFor(comPort)) {
            ComJobFactory comJobFactoryFor = getComJobFactoryFor(comPort);
            return comJobFactoryFor.consume(comTaskExecutions.iterator());
        }
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice offlineDevice, InboundComPort comPort) {
        Device device = getDeviceDataService().findDeviceById(offlineDevice.getId()).get();
        return getCommunicationTaskService().getPlannedComTaskExecutionsFor(comPort, device);
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(final ConnectionTask connectionTask) {
        return this.serviceProvider.transactionService().execute(connectionTask::getProperties);
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comServer);
    }

    @Override
    public void unlock(ScheduledConnectionTask connectionTask) {
        getConnectionTaskService().unlockConnectionTask(connectionTask);
    }

    @Override
    public OfflineDevice findOfflineDevice(DeviceIdentifier<?> identifier) {
        return findOfflineDevice(identifier, DeviceOffline.needsEverything);
    }

    @Override
    public OfflineDevice findOfflineDevice(DeviceIdentifier<?> identifier, OfflineDeviceContext offlineDeviceContext) {
        BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> device = identifier.findDevice();
        if (device != null) {
            return new OfflineDeviceImpl((Device) device, offlineDeviceContext, new OfflineDeviceServiceProvider());
        } else {
            return null;
        }
    }

    @Override
    public OfflineRegister findOfflineRegister(RegisterIdentifier identifier) {
        return new OfflineRegisterImpl((Register) identifier.findRegister(), this.serviceProvider.identificationService());
    }

    @Override
    public OfflineLoadProfile findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return new OfflineLoadProfileImpl((LoadProfile) loadProfileIdentifier.findLoadProfile(), this.serviceProvider.topologyService(), this.serviceProvider.identificationService());
    }

    @Override
    public OfflineLogBook findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        return new OfflineLogBookImpl((LogBook) logBookIdentifier.getLogBook(), this.serviceProvider.identificationService());
    }

    @Override
    public void updateIpAddress(String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        connectionTask.setProperty(connectionTaskPropertyName, ipAddress);
        connectionTask.save();
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        final BaseDevice device = deviceIdentifier.findDevice();
        Device deviceById = getDeviceDataService().findDeviceById(device.getId()).get();
        deviceById.setProtocolProperty(propertyName, propertyValue);
        deviceById.save();
    }


    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        Device device = (Device) deviceIdentifier.findDevice();
        Device gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = (Device) gatewayDeviceIdentifier.findDevice();
        } else {
            gatewayDevice = null;
        }
        if (gatewayDevice != null) {
            this.serviceProvider.topologyService().setPhysicalGateway(device, gatewayDevice);
        } else {
            this.serviceProvider.topologyService().clearPhysicalGateway(device);
        }
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, final DateTimeFormatter timeStampFormat, final String fileExtension, final byte[] contents) {
        this.doStoreConfigurationFile(deviceIdentifier.findDevice(), timeStampFormat, fileExtension, contents);
    }

    private void doStoreConfigurationFile(BaseDevice device, DateTimeFormatter timeStampFormat, String fileExtension, byte[] contents) {
        throw new RuntimeException("Storing of UserFiles is currently not supported ...");
//        try {
//            String fileName = this.getUniqueUserFileName(timeStampFormat);
//            UserFileShadow userFileShadow = new UserFileShadow();
//            userFileShadow.setExtension(fileExtension);
//            userFileShadow.setName(fileName);
//            ByteArrayInputStream byteStream = new ByteArrayInputStream(contents);
//            new UserFileFactoryImpl()
//            UserFile userFile = MeteringWarehouse.getCurrent().getUserFileFactory().create(userFileShadow);
//            userFile.updateContents(byteStream);
//        } finally {
//            this.closeConnection();
//        }
    }

    private String getUniqueUserFileName(DateTimeFormatter timeStampFormat) {
        String fileName = "Config_" + timeStampFormat.format(getClock().instant());
        int version = this.getVersion(fileName);
        if (version > 1) {
            fileName += "_(" + version + ")";
        }
        return fileName;
    }

    /**
     * Returns the first available number that can be used to create
     * a {@link UserFile} with a unique name.
     *
     * @param fileName The base name for the UserFile
     * @return A number that will make the file name unique
     */
    private int getVersion(String fileName) {
        return 1;
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComServer comServer) {
        return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comServer) != null;
    }

    public void unlock(final OutboundConnectionTask connectionTask) {
        getConnectionTaskService().unlockConnectionTask(connectionTask);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        return getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, comPort) != null;
    }

    @Override
    public void unlock(final ComTaskExecution comTaskExecution) {
        getCommunicationTaskService().unlockComTaskExecution(comTaskExecution);
    }

    @Override
    public void executionStarted(final ConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(() -> {
            connectionTask.executionStarted(comServer);
            return null;
        });
    }

    @Override
    public void executionCompleted(final ConnectionTask connectionTask) {
        connectionTask.executionCompleted();
    }

    @Override
    public void executionFailed(final ConnectionTask connectionTask) {
        connectionTask.executionFailed();
    }

    @Override
    public void executionStarted(final ComTaskExecution comTaskExecution, final ComPort comPort, boolean executeInTransaction) {
        if (executeInTransaction) {
            this.executeTransaction(() -> {
                markComTaskForExecutionStarted(comTaskExecution, comPort);
                return null;
            });
        } else {
            markComTaskForExecutionStarted(comTaskExecution, comPort);
        }
    }

    private void markComTaskForExecutionStarted(ComTaskExecution comTaskExecution, ComPort comPort) {
        getCommunicationTaskService().executionStartedFor(comTaskExecution, comPort);
    }

    @Override
    public void executionCompleted(final ComTaskExecution comTaskExecution) {
        getCommunicationTaskService().executionCompletedFor(comTaskExecution);
    }

    @Override
    public void executionCompleted(final List<? extends ComTaskExecution> comTaskExecutions) {
        comTaskExecutions
                .stream()
                .forEach(comTaskExecution -> getCommunicationTaskService().executionCompletedFor(comTaskExecution));
    }

    @Override
    public void executionFailed(final ComTaskExecution comTaskExecution) {
        getCommunicationTaskService().executionFailedFor(comTaskExecution);
    }

    @Override
    public void executionFailed(final List<? extends ComTaskExecution> comTaskExecutions) {
        comTaskExecutions
                .stream()
                .forEach(comTaskExecution -> getCommunicationTaskService().executionFailedFor(comTaskExecution));
    }

    @Override
    public void releaseInterruptedTasks(final ComServer comServer) {
        this.executeTransaction(() -> {
            getConnectionTaskService().releaseInterruptedConnectionTasks(comServer);
            getCommunicationTaskService().releaseInterruptedComTasks(comServer);
            return null;
        });
    }

    @Override
    public TimeDuration releaseTimedOutTasks(final ComServer comServer) {
        return this.executeTransaction(() -> {
            getConnectionTaskService().releaseTimedOutConnectionTasks(comServer);
            return getCommunicationTaskService().releaseTimedOutComTasks(comServer);
        });
    }

    @Override
    public ComSession createComSession(final ComSessionBuilder builder, final ComSession.SuccessIndicator successIndicator) {
        /* We should already be in a transaction so don't wrap it again */
        return builder.endSession(now(), successIndicator).create();
    }

    @Override
    public void storeMeterReadings(final DeviceIdentifier<Device> deviceIdentifier, final MeterReading meterReading) {
        Device device = deviceIdentifier.findDevice();
        device.store(meterReading);
    }

    @Override
    public void signalEvent(String topic, Object source) {
        this.serviceProvider.eventService().postEvent(topic, source);
    }

    @Override
    public void updateDeviceMessageInformation(final MessageIdentifier messageIdentifier, final DeviceMessageStatus newDeviceMessageStatus, final String protocolInformation) {
        DeviceMessage deviceMessage = messageIdentifier.getDeviceMessage();
        deviceMessage.setProtocolInformation(protocolInformation);
        deviceMessage.updateDeviceMessageStatus(newDeviceMessageStatus);
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        return getCommunicationTaskService().isComTaskStillPending(comTaskExecutionId);
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        return getCommunicationTaskService().areComTasksStillPending(comTaskExecutionIds);
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(final DeviceIdentifier<Device> deviceIdentifier, final int confirmationCount) {
        return convertToOfflineDeviceMessages(doConfirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount));
    }

    private List<OfflineDeviceMessage> convertToOfflineDeviceMessages(List<DeviceMessage<Device>> deviceMessages) {
        return deviceMessages.stream().map(deviceMessage -> new OfflineDeviceMessageImpl(deviceMessage, deviceMessage.getDevice().getDeviceProtocolPluggableClass().getDeviceProtocol(), this.serviceProvider.identificationService())).collect(Collectors.toList());
    }

    private List<DeviceMessage<Device>> doConfirmSentMessagesAndGetPending(DeviceIdentifier<Device> deviceIdentifier, int confirmationCount) {
        return this.doConfirmSentMessagesAndGetPending(deviceIdentifier.findDevice(), confirmationCount);
    }

    private List<DeviceMessage<Device>> doConfirmSentMessagesAndGetPending(Device device, int confirmationCount) {
        this.updateSentMessageStates(device, confirmationCount);
        return this.findPendingMessageAndMarkAsSent(device);
    }

    private void updateSentMessageStates(Device device, int confirmationCount) {
        List<DeviceMessage<Device>> sentMessages = device.getMessagesByState(DeviceMessageStatus.SENT);
        FutureMessageState newState = this.getFutureMessageState(sentMessages, confirmationCount);
        sentMessages.forEach(newState::applyTo);
    }

    private FutureMessageState getFutureMessageState(List<DeviceMessage<Device>> sentMessages, int confirmationCount) {
        if (confirmationCount == 0) {
            return FutureMessageState.FAILED;
        } else if (confirmationCount == sentMessages.size()) {
            return FutureMessageState.CONFIRMED;
        } else {
            return FutureMessageState.INDOUBT;
        }
    }

    private List<DeviceMessage<Device>> findPendingMessageAndMarkAsSent(Device device) {
        List<DeviceMessage<Device>> pendingMessages = device.getMessagesByState(DeviceMessageStatus.PENDING);
        List<DeviceMessage<Device>> sentMessages = new ArrayList<>(pendingMessages.size());
        for (DeviceMessage<Device> pendingMessage : pendingMessages) {
            pendingMessage.updateDeviceMessageStatus(DeviceMessageStatus.SENT);
            sentMessages.add(pendingMessage);
        }
        return sentMessages;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return getTransactionService().execute(transaction);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        Device device = (Device) deviceIdentifier.findDevice();
        InboundConnectionTask connectionTask = this.getInboundConnectionTask(comPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            SecurityPropertySet securityPropertySet = this.getSecurityPropertySet(device, connectionTask);
            if (securityPropertySet == null) {
                return null;
            } else {
                return device.getSecurityProperties(securityPropertySet);
            }
        }
    }

    /**
     * Gets the {@link SecurityProperty} that needs to be used when
     * the Device is communicating to the ComServer
     * via the specified {@link InboundConnectionTask}.
     *
     * @param device         The Device
     * @param connectionTask The ConnectionTask
     * @return The SecurityPropertySet or <code>null</code> if the Device is not ready for inbound communication
     */
    private SecurityPropertySet getSecurityPropertySet(Device device, InboundConnectionTask connectionTask) {
        SecurityPropertySet securityPropertySet = null;
        ComTaskExecution first = this.getFirstComTaskExecution(connectionTask);
        if (first == null) {
            return null;
        } else {
            for (ComTaskEnablement comTaskEnablement : enabledComTasks(device.getDeviceConfiguration())) {
                if (comTaskEnablement.getComTask().equals(first.getComTasks().get(0))) {
                    securityPropertySet = comTaskEnablement.getSecurityPropertySet();
                }
            }
            return securityPropertySet;
        }
    }

    private List<ComTaskEnablement> enabledComTasks(DeviceConfiguration deviceConfiguration) {
        return deviceConfiguration.getComTaskEnablements();
    }

    /**
     * Gets the first {@link ComTaskExecution} that uses the specified
     * {@link InboundConnectionTask}. It is assumed that this method is used
     * to find the {@link SecurityPropertySet} and because the Device
     * will always use the same authentication/encryption levels when
     * initiating inbound communication, all ComTaskExecution will need
     * to have the same SecurityPropertySet. Therefore, it is sufficient
     * to take the first one.
     *
     * @param connectionTask The ConnectionTask
     * @return The ComTaskExecution or <code>null</code> if none are defined,
     * indicating that the Device is not ready for inbound communication
     */
    private ComTaskExecution getFirstComTaskExecution(InboundConnectionTask connectionTask) {
        List<ComTaskExecution> comTaskExecutions = getCommunicationTaskService().findComTaskExecutionsByConnectionTask(connectionTask).find();
        if (comTaskExecutions.isEmpty()) {
            return null;
        } else {
            return comTaskExecutions.get(0);
        }
    }

    private InboundConnectionTask getInboundConnectionTask(InboundComPort comPort, Device device) {
        InboundComPortPool comPortPool = comPort.getComPortPool();
        for (InboundConnectionTask inboundConnectionTask : device.getInboundConnectionTasks()) {
            if (comPortPool.equals(inboundConnectionTask.getComPortPool())) {
                return inboundConnectionTask;
            }
        }
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Device device = (Device) deviceIdentifier.findDevice(); //TODO ugly casting ...
        ConnectionTask<?, ?> connectionTask = this.getInboundConnectionTask(inboundComPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            return connectionTask.getTypedProperties();
        }
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        try {
            Device device = (Device) deviceIdentifier.findDevice();
            if (device != null) {
                return device.getDeviceProtocolProperties();
            } else {
                return null;
            }
        } catch (CanNotFindForIdentifier e) {
            return null;
        }
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(loadProfileIdentifier.findLoadProfile().getDevice());
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(((LogBook) logBookIdentifier.getLogBook()).getDevice());
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        LogBook logBook = (LogBook) logBookIdentifier.getLogBook();
        LogBook.LogBookUpdater logBookUpdater = logBook.getDevice().getLogBookUpdaterFor(logBook);
        logBookUpdater.setLastLogBookIfLater(lastLogBook);
        logBookUpdater.setLastReadingIfLater(getClock().instant()); // We assume the event will be persisted with a time difference of only a few milliseconds
        logBookUpdater.update();
    }

    @Override
    public void storePathSegments(DeviceIdentifier sourceDeviceIdentifier, List<TopologyPathSegment> topologyPathSegments) {
        TopologyService.G3CommunicationPathSegmentBuilder g3CommunicationPathSegmentBuilder = serviceProvider.topologyService().addCommunicationSegments(((Device) sourceDeviceIdentifier.findDevice()));
        topologyPathSegments.stream().forEach(topologyPathSegment -> {
            Optional<Device> target = getOptionalDeviceByIdentifier(topologyPathSegment.getTarget());
            Optional<Device> intermediateHop = getOptionalDeviceByIdentifier(topologyPathSegment.getIntermediateHop());
            if (target.isPresent() && intermediateHop.isPresent()) {
                g3CommunicationPathSegmentBuilder.add(
                        target.get(),
                        intermediateHop.get(),
                        topologyPathSegment.getTimeToLive(),
                        topologyPathSegment.getCost()
                );
            }
        });
        g3CommunicationPathSegmentBuilder.complete();
    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
        TopologyService.G3NeighborhoodBuilder g3NeighborhoodBuilder = serviceProvider.topologyService().buildG3Neighborhood((Device) sourceDeviceIdentifier.findDevice());
        topologyNeighbours.stream().forEach(topologyNeighbour -> {
            Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(topologyNeighbour.getNeighbour());
            if (optionalDevice.isPresent()) {
                TopologyService.G3NeighborBuilder g3NeighborBuilder = g3NeighborhoodBuilder.addNeighbor(optionalDevice.get(), ModulationScheme.fromId(topologyNeighbour.getModulationSchema()), Modulation.fromOrdinal(topologyNeighbour.getModulation()), PhaseInfo.fromId(topologyNeighbour.getPhaseDifferential()));
                g3NeighborBuilder.linkQualityIndicator(topologyNeighbour.getLqi());
                g3NeighborBuilder.timeToLiveSeconds(topologyNeighbour.getNeighbourValidTime());
                g3NeighborBuilder.toneMap(topologyNeighbour.getToneMap());
                g3NeighborBuilder.toneMapTimeToLiveSeconds(topologyNeighbour.getTmrValidTime());
                g3NeighborBuilder.txCoefficient(topologyNeighbour.getTxCoeff());
                g3NeighborBuilder.txGain(topologyNeighbour.getTxGain());
                g3NeighborBuilder.txResolution(topologyNeighbour.getTxRes());
            }
        });
        g3NeighborhoodBuilder.complete();
    }

    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(topologyDeviceAddressInformation.getDeviceIdentifier());
        if (optionalDevice.isPresent()) {
            serviceProvider.topologyService().setG3DeviceAddressInformation(
                    optionalDevice.get(),
                    topologyDeviceAddressInformation.getFullIPv6Address(),
                    topologyDeviceAddressInformation.getIpv6ShortAddress(),
                    topologyDeviceAddressInformation.getLogicalDeviceId());
        }
    }

    private Optional<Device> getOptionalDeviceByIdentifier(DeviceIdentifier deviceIdentifier) {
        Device device = null;
        try {
            device = (Device) deviceIdentifier.findDevice();
        } catch (CanNotFindForIdentifier e) {
            // ignore
        }
        return Optional.ofNullable(device);
    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(collectedFirmwareVersions.getDeviceIdentifier());
        optionalDevice.ifPresent(device -> {
            FirmwareStorage firmwareStorage = new FirmwareStorage(serviceProvider.firmwareService(), serviceProvider.clock());
            firmwareStorage.updateMeterFirmwareVersion(collectedFirmwareVersions.getActiveMeterFirmwareVersion(), device);
            firmwareStorage.updateCommunicationFirmwareVersion(collectedFirmwareVersions.getActiveCommunicationFirmwareVersion(), device);
        });
    }

    private Instant now() {
        return this.serviceProvider.clock().instant();
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        LoadProfile loadProfile = (LoadProfile) loadProfileIdentifier.findLoadProfile();
        LoadProfile.LoadProfileUpdater loadProfileUpdater = loadProfile.getDevice().getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReadingIfLater(lastReading);
        loadProfileUpdater.update();
    }

    private enum FutureMessageState {
        INDOUBT {
            @Override
            public void applyTo(DeviceMessage message) {
                message.updateDeviceMessageStatus(DeviceMessageStatus.INDOUBT);
            }
        },

        FAILED {
            @Override
            public void applyTo(DeviceMessage message) {
                message.updateDeviceMessageStatus(DeviceMessageStatus.FAILED);
            }
        },

        CONFIRMED {
            @Override
            public void applyTo(DeviceMessage message) {
                message.updateDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            }
        };

        public abstract void applyTo(DeviceMessage message);

    }

}