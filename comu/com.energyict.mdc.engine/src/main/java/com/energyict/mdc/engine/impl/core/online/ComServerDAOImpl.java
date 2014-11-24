package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceMessageImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLogBookImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComJobFactory;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedComJobFactory;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.SingleThreadedComJobFactory;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.sql.Fetcher;

import java.text.DateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

        public Clock clock();

        public EngineModelService engineModelService();

        public ConnectionTaskService connectionTaskService();

        public CommunicationTaskService communicationTaskService();

        public DeviceService deviceService();

        public EngineService engineService();

        public TransactionService transactionService();

        public EventService eventService();

    }

    private final ServiceProvider serviceProvider;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;

    public ComServerDAOImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    private EngineModelService getEngineModelService() {
        return this.serviceProvider.engineModelService();
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
        public Optional<DeviceCache> findProtocolCacheByDevice(Device device) {
            return serviceProvider.engineService().findDeviceCacheByDevice(device);
        }

    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        Optional<ComServer> reloaded = getEngineModelService().findComServer(comServer.getId());
        if (reloaded.isPresent()) {
            if (reloaded.get().isObsolete()) {
                return null;
            } else if (reloaded.get().getModificationDate().isAfter(comServer.getModificationDate())) {
                return reloaded.get();
            } else {
                return comServer;
            }
        } else {
            return null;
        }
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        ComPort reloaded = getEngineModelService().findComPort(comPort.getId());
        if (reloaded == null || reloaded.isObsolete()) {
            return null;
        } else if (reloaded.getModificationDate().isAfter(comPort.getModificationDate())) {
            return reloaded;
        } else {
            return comPort;
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
        Device device = getDeviceDataService().findDeviceById(offlineDevice.getId());
        return getCommunicationTaskService().getPlannedComTaskExecutionsFor(comPort, device);
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(final ConnectionTask connectionTask) {
        return this.serviceProvider.transactionService().execute(() -> connectionTask.getProperties());
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
    public OfflineDevice findDevice(DeviceIdentifier<?> identifier) {
        BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> device = identifier.findDevice();
        if (device != null) {
            return new OfflineDeviceImpl((Device) device, DeviceOffline.needsEverything, new OfflineDeviceServiceProvider());
        } else {
            return null;
        }
    }

    @Override
    public OfflineRegister findOfflineRegister(RegisterIdentifier identifier) {
        return new OfflineRegisterImpl((Register) identifier.findRegister());
    }

    @Override
    public OfflineLoadProfile findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return new OfflineLoadProfileImpl((LoadProfile) loadProfileIdentifier.findLoadProfile());
    }

    @Override
    public OfflineLogBook findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        return new OfflineLogBookImpl((LogBook) logBookIdentifier.getLogBook());
    }

    //    @Override
//    public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
//        EndDeviceMessage deviceMessage = (EndDeviceMessage) identifier.getDeviceMessage();
//        return deviceMessage.goOffline();
//    }

    @Override
    public void updateIpAddress(String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        TypedProperties properties = connectionTask.getTypedProperties();
        properties.setProperty(connectionTaskPropertyName, ipAddress);
        // TODO: JP-1123
        // serverConnectionTask.updateProperties(properties);
        // add/remove/update properties
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        BaseDevice device = deviceIdentifier.findDevice();
        BaseDevice gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = gatewayDeviceIdentifier.findDevice();
        } else {
            gatewayDevice = null;
        }
        device.setPhysicalGateway(gatewayDevice);
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, final DateFormat timeStampFormat, final String fileExtension, final byte[] contents) {
        this.doStoreConfigurationFile(deviceIdentifier.findDevice(), timeStampFormat, fileExtension, contents);
    }

    private void doStoreConfigurationFile(BaseDevice device, DateFormat timeStampFormat, String fileExtension, byte[] contents) {
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

    private String getUniqueUserFileName(DateFormat timeStampFormat) {
        String fileName = "Config_" + timeStampFormat.format(Date.from(getClock().instant()));
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
            toServerConnectionTask(connectionTask).executionStarted(comServer);
            return null;
        });
    }

    @Override
    public void executionCompleted(final ConnectionTask connectionTask) {
        this.toServerConnectionTask(connectionTask).executionCompleted();
    }

    @Override
    public void executionFailed(final ConnectionTask connectionTask) {
        this.toServerConnectionTask(connectionTask).executionFailed();
    }

    @Override
    public void executionStarted(final ComTaskExecution comTaskExecution, final ComPort comPort) {
        this.executeTransaction(() -> {
            getCommunicationTaskService().executionStartedFor(comTaskExecution, comPort);
            return null;
        });
    }

    @Override
    public void executionCompleted(final ComTaskExecution comTaskExecution) {
//        this.toServerComTaskExecution(comTaskExecution).executionCompleted();
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
//        this.toServerComTaskExecution(comTaskExecution).executionFailed();
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
        return builder.endSession(serviceProvider.clock().instant(), successIndicator).create();
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
        return deviceMessages.stream().map(deviceMessage -> new OfflineDeviceMessageImpl(deviceMessage, deviceMessage.getDevice().getDeviceProtocolPluggableClass().getDeviceProtocol())).collect(Collectors.toList());
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
            for (ComTaskEnablement comTaskEnablement : enabledComTasks(device.getDeviceConfiguration().getCommunicationConfiguration())) {
                if (comTaskEnablement.getComTask().equals(first.getComTasks().get(0))) {
                    securityPropertySet = comTaskEnablement.getSecurityPropertySet();
                }
            }
            return securityPropertySet;
        }
    }

    private List<ComTaskEnablement> enabledComTasks(DeviceCommunicationConfiguration communicationConfiguration) {
        return communicationConfiguration.getComTaskEnablements();
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
        } catch (NotFoundException e) {
            return null;
        }
    }

    private ScheduledConnectionTask toServerConnectionTask(ConnectionTask connectionTask) {
        return (ScheduledConnectionTask) connectionTask;
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return new DeviceIdentifierForAlreadyKnownDevice((Device) loadProfileIdentifier.findLoadProfile().getDevice());
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return new DeviceIdentifierForAlreadyKnownDevice(((LogBook) logBookIdentifier.getLogBook()).getDevice());
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Date lastLogBook) {
        LogBook logBook = (LogBook) logBookIdentifier.getLogBook();
        LogBook.LogBookUpdater logBookUpdater = logBook.getDevice().getLogBookUpdaterFor(logBook);
        logBookUpdater.setLastLogBookIfLater(lastLogBook);
        logBookUpdater.setLastReadingIfLater(Date.from(getClock().instant())); // We assume the event will be persisted with a time difference of only a few milliseconds
        logBookUpdater.update();
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Date lastReading) {
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