package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.ServerDeviceCommunicationConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComJobFactory;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.MultiThreadedComJobFactory;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
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
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//import com.energyict.mdc.engine.model.ComServer;

/**
 * Provides a default implementation for the {@link ComServerDAO} interface
 * that uses the EIServer persistence framework for all requests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-07 (08:58)
 */
public class ComServerDAOImpl implements ComServerDAO {

    private final ServiceProvider serviceProvider;

    private ServerProcessStatus status = ServerProcessStatus.STARTING;

    public ComServerDAOImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    private EngineModelService getEngineModelService() {
        return this.serviceProvider.engineModelService();
    }

    private DeviceDataService getDeviceDataService() {
        return this.serviceProvider.deviceDataService();
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
        return getEngineModelService().findComServerBySystemName();
    }

    @Override
    public ComServer getComServer(String systemName) {
        return getEngineModelService().findComServer(systemName);
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        ComServer reloaded = getEngineModelService().findComServer(comServer.getId());
        if (reloaded == null || reloaded.isObsolete()) {
            return null;
        } else if (reloaded.getModificationDate().after(comServer.getModificationDate())) {
            return reloaded;
        } else {
            return comServer;
        }
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        ComPort reloaded = getEngineModelService().findComPort(comPort.getId());
        if (reloaded == null || reloaded.isObsolete()) {
            return null;
        } else if (reloaded.getModificationDate().after(comPort.getModificationDate())) {
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
        List<ComTaskExecution> comTaskExecutions = getDeviceDataService().getPlannedComTaskExecutionsFor(comPort);
        ComJobFactory comJobFactoryFor = getComJobFactoryFor(comPort);
        return comJobFactoryFor.consume(comTaskExecutions);
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice offlineDevice, InboundComPort comPort) {
        Device device = getDeviceDataService().findDeviceById(offlineDevice.getId());
        return getDeviceDataService().getPlannedComTaskExecutionsFor(comPort, device);
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        return getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);
    }

    @Override
    public void unlock(ScheduledConnectionTask connectionTask) {
        getDeviceDataService().unlockConnectionTask(connectionTask);
    }

    @Override
    public OfflineDevice findDevice(DeviceIdentifier<?> identifier) {
        BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> device = identifier.findDevice();
//            BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device = device1;
        if (device != null) {
            return new OfflineDeviceImpl((Device) device, DeviceOffline.needsEverything);
        } else {
            return null;
        }
    }

    @Override
    public OfflineRegister findRegister(RegisterIdentifier identifier) {
        return new OfflineRegisterImpl((com.energyict.mdc.device.data.Register) identifier.findRegister());
    }

//    @Override
//    public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
//        EndDeviceMessage deviceMessage = (EndDeviceMessage) identifier.getDeviceMessage();
//        return deviceMessage.goOffline();
//    }

    @Override
    public void updateIpAddress(String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        final TypedProperties properties = connectionTask.getTypedProperties();
        properties.setProperty(connectionTaskPropertyName, ipAddress);
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
//                serverConnectionTask.updateProperties(properties);
                // add/remove/update properties
                return null; // TODO JP-1123

            }
        });
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        final BaseDevice device = deviceIdentifier.findDevice();
        final BaseDevice gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = gatewayDeviceIdentifier.findDevice();
        } else {
            gatewayDevice = null;
        }
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                device.setPhysicalGateway(gatewayDevice);
                return null;
            }
        });
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, final DateFormat timeStampFormat, final String fileExtension, final byte[] contents) {
        final BaseDevice device = deviceIdentifier.findDevice();
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                doStoreConfigurationFile(device, timeStampFormat, fileExtension, contents);
                return null;
            }
        });
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
        String fileName = "Config_" + timeStampFormat.format(getClock().now());
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
        return getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer) != null;
    }

    public void unlock(final OutboundConnectionTask connectionTask) {
        getDeviceDataService().unlockConnectionTask(connectionTask);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        return getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, comPort) != null;
    }

    @Override
    public void unlock(final ComTaskExecution comTaskExecution) {
        getDeviceDataService().unlockComTaskExecution(comTaskExecution);
    }

    @Override
    public void executionStarted(final ConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerConnectionTask(connectionTask).executionStarted(comServer);
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final ConnectionTask connectionTask) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerConnectionTask(connectionTask).executionCompleted();
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final ConnectionTask connectionTask) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerConnectionTask(connectionTask).executionFailed();
                return null;
            }
        });
    }

    @Override
    public void executionStarted(final ComTaskExecution comTaskExecution, final ComPort comPort) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerComTaskExecution(comTaskExecution).executionStarted(comPort);
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerComTaskExecution(comTaskExecution).executionCompleted();
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final List<? extends ComTaskExecution> comTaskExecutions) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    toServerComTaskExecution(comTaskExecution).executionCompleted();
                }
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                toServerComTaskExecution(comTaskExecution).executionFailed();
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final List<? extends ComTaskExecution> comTaskExecutions) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    toServerComTaskExecution(comTaskExecution).executionFailed();
                }
                return null;
            }
        });
    }

    @Override
    public void releaseInterruptedTasks(final ComServer comServer) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                getDeviceDataService().releaseInterruptedConnectionTasks(comServer);
                getDeviceDataService().releaseInterruptedComTasks(comServer);
                return null;
            }
        });
    }

    @Override
    public TimeDuration releaseTimedOutTasks(final ComServer comServer) {
        return this.executeTransaction(new Transaction<TimeDuration>() {
            @Override
            public TimeDuration perform() {
                getDeviceDataService().releaseTimedOutConnectionTasks(comServer);
                return getDeviceDataService().releaseTimedOutComTasks(comServer);
            }
        });
    }

    @Override
    public ComSession createComSession(final ComSessionBuilder builder, final ComSession.SuccessIndicator successIndicator) {
        return this.executeTransaction(new Transaction<ComSession>() {
            @Override
            public ComSession perform() {
                return builder.endSession(serviceProvider.clock().now(), successIndicator).create();
            }
        });
    }

    // TODO still required?
//    private EndDeviceCache createOrUpdateDeviceCache(final int deviceId, final DeviceCacheShadow shadow) {
//        return this.executeTransaction(new Transaction<EndDeviceCache>() {
//            @Override
//            public EndDeviceCache perform() throws SQLException, BusinessException {
//                DeviceCacheFactory deviceCacheFactory = getManager().getMdwInterface().getDeviceCacheFactory();
//                EndDeviceCache deviceCache = deviceCacheFactory.findByDeviceId(deviceId);
//                if (deviceCache == null) {    // create a new one
//                    deviceCache = deviceCacheFactory.create(shadow);
//                } else {    // update the existing one
//                    deviceCache.update(shadow);
//                }
//                return deviceCache;
//            }
//        });
//    }

    @Override
    public void storeMeterReadings(final DeviceIdentifier<Device> deviceIdentifier, final MeterReading meterReading) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                Device device = deviceIdentifier.findDevice();
                device.store(meterReading);
                return null;
            }
        });
    }

    @Override
    public void signalEvent(String topic, Object source) {
        this.serviceProvider.eventService().postEvent(topic, source);
    }

    @Override
    public void updateDeviceMessageInformation(final MessageIdentifier messageIdentifier, final DeviceMessageStatus newDeviceMessageStatus, final String protocolInformation) {
        this.executeTransaction(new Transaction<Void>() {
            @Override
            public Void perform() {
                // TODO complete once Messages are ported
//                EndDeviceMessage deviceMessage = (EndDeviceMessage) messageIdentifier.getDeviceMessage();
//                if (newDeviceMessageStatus.ordinal() != deviceMessage.getStatus().ordinal()) {  // When the status doesn't change, no update is needed
//                    deviceMessage.moveTo(newDeviceMessageStatus);
//                }
//                deviceMessage.updateProtocolInfo(protocolInformation);
                return null;
            }
        });
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        return getDeviceDataService().areComTasksStillPending(Arrays.asList(comTaskExecutionId));
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        return getDeviceDataService().areComTasksStillPending(comTaskExecutionIds);
    }

    @Override
    public void setMaxNumberOfTries(final ScheduledConnectionTask connectionTask, final int maxNumberOfTries) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                connectionTask.setMaxNumberOfTries(maxNumberOfTries);
                connectionTask.save();
            }
        });
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(final DeviceIdentifier deviceIdentifier, final int confirmationCount) {
        return this.executeTransaction(new Transaction<List<OfflineDeviceMessage>>() {
            @Override
            public List<OfflineDeviceMessage> perform() {
                // TODO complete once Messages are ported
//                return convertToOfflineDeviceMessages(doConfirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount));
                return null;
            }
        });
    }

    /*

        // TODO complete once Messages are ported


        private List<OfflineDeviceMessage> convertToOfflineDeviceMessages(List<EndDeviceMessage> deviceMessages) {
            List<OfflineDeviceMessage> offlineDeviceMessages = new ArrayList<>();
            for (EndDeviceMessage deviceMessage : deviceMessages) {
                offlineDeviceMessages.add(deviceMessage.goOffline());
            }
            return offlineDeviceMessages;
        }

        private List<EndDeviceMessage> doConfirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) throws BusinessException, SQLException {
            return this.doConfirmSentMessagesAndGetPending(((Device) deviceIdentifier.findDevice()), confirmationCount);
        }

        private List<EndDeviceMessage> doConfirmSentMessagesAndGetPending(Device device, int confirmationCount) throws BusinessException, SQLException {
            this.updateSentMessageStates(device, confirmationCount);
            return this.findPendingMessageAndMarkAsSent(device);
        }

        private void updateSentMessageStates(Device device, int confirmationCount) throws BusinessException, SQLException {
            List<DeviceMessage> sentMessages = device.getMessagesByState(DeviceMessageStatus.SENT);
            FutureMessageState newState = this.getFutureMessageState(sentMessages, confirmationCount);
            for (DeviceMessage sentMessage : sentMessages) {
                newState.applyTo(sentMessage);
            }
        }

        private FutureMessageState getFutureMessageState(List<DeviceMessage> sentMessages, int confirmationCount) {
            if (confirmationCount == 0) {
                return FutureMessageState.FAILED;
            } else if (confirmationCount == sentMessages.size()) {
                return FutureMessageState.CONFIRMED;
            } else {
                return FutureMessageState.INDOUBT;
            }
        }

        private List<EndDeviceMessage> findPendingMessageAndMarkAsSent(Device device) {
            List<DeviceMessage> pendingMessages = device.getMessagesByState(DeviceMessageStatus.PENDING);
            List<EndDeviceMessage> sentMessages = new ArrayList<>(pendingMessages.size());
            for (DeviceMessage pendingMessage : pendingMessages) {
                EndDeviceMessage readyToSend = (EndDeviceMessage) pendingMessage;
                readyToSend.moveTo(DeviceMessageStatus.SENT);
                sentMessages.add(readyToSend);
            }
            return sentMessages;
        }

    */
    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return getTransactionService().execute(transaction);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        //TODO complete once SecurityProperties are properly ported !!
//        try {
//            CommunicationDevice device = (CommunicationDevice) deviceIdentifier.findDevice();
//            InboundConnectionTask connectionTask = this.getInboundConnectionTask(comPort, device);
//            if (connectionTask == null) {
//                return null;
//            } else {
//                SecurityPropertySet securityPropertySet = this.getSecurityPropertySet(device, connectionTask);
//                if (securityPropertySet == null) {
//                    return null;
//                } else {
//                    return device.getProtocolSecurityProperties(securityPropertySet);
//                }
//            }
//        } finally {
//            this.closeConnection();
//        }
        return Collections.emptyList();
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
            for (ComTaskEnablement comTaskEnablement : enabledComTasks((ServerDeviceCommunicationConfiguration) device.getDeviceConfiguration().getCommunicationConfiguration())) {
                if (comTaskEnablement.getComTask().equals(first.getComTask())) {
                    securityPropertySet = comTaskEnablement.getSecurityPropertySet();
                }
            }
            return securityPropertySet;
        }
    }

    private List<ComTaskEnablement> enabledComTasks(ServerDeviceCommunicationConfiguration communicationConfiguration) {
        return Collections.emptyList(); // TODO
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
        List<ComTaskExecution> comTaskExecutions = getDeviceDataService().findComTaskExecutionsByConnectionTask(connectionTask);
        if (comTaskExecutions.isEmpty()) {
            return null;
        } else {
            return comTaskExecutions.get(0);
        }
    }

    private ConnectionTask<?, ?> getInboundConnectionTask(InboundComPort comPort, Device device) {
        InboundComPortPool comPortPool = comPort.getComPortPool();
        for (ConnectionTask<?, ?> inboundConnectionTask : device.getConnectionTasks()) {
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

    private ServerComTaskExecution toServerComTaskExecution(ComTaskExecution scheduledComTask) {
        return (ServerComTaskExecution) scheduledComTask;
    }

    private enum FutureMessageState {
        //TODO enable once messages are properly ported
//        INDOUBT {
//            @Override
//            public void applyTo(DeviceMessage message) {
//                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.INDOUBT);
//            }
//        },
//
//        FAILED {
//            @Override
//            public void applyTo(DeviceMessage message) {
//                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.FAILED);
//            }
//        },
//
//        CONFIRMED {
//            @Override
//            public void applyTo(DeviceMessage message) {
//                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.CONFIRMED);
//            }
//        };
        ;

        //public abstract void applyTo(DeviceMessage message) throws BusinessException, SQLException;

    }

}