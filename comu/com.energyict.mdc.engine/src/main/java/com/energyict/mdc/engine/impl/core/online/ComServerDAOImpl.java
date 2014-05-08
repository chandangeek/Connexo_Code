package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessEvent;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.DeviceCacheFactory;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.ServerDeviceCommunicationConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileShadow;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
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

    private ServerProcessStatus status = ServerProcessStatus.STARTING;

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
        return this.getManager().getComServerFactory().findBySystemName();
    }

    @Override
    public ComServer getComServer(String systemName) {
        return this.getManager().getComServerFactory().findBySystemName(systemName);
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        try {
            ComServer reloaded = this.getManager().getComServerFactory().find((int) comServer.getId());
            if (reloaded == null || reloaded.isObsolete()) {
                return null;
            } else if (reloaded.getModificationDate().after(comServer.getModificationDate())) {
                return reloaded;
            } else {
                return comServer;
            }
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        try {
            ComPort reloaded = this.getManager().getComPortFactory().find((int) comPort.getId());
            if (reloaded == null || reloaded.isObsolete()) {
                return null;
            } else if (reloaded.getModificationDate().after(comPort.getModificationDate())) {
                return reloaded;
            } else {
                return comPort;
            }
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        try {
            return ManagerFactory.getCurrent().getComTaskExecutionFactory().findExecutableByComPort(comPort);
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice offlineDevice, InboundComPort comPort) {
        try {
            Device device = this.getManager().getMdwInterface().getDeviceFactory().find(offlineDevice.getId());
            return this.getManager().getComTaskExecutionFactory().findExecutableByComPort(device, comPort);
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        return MeteringWarehouse.getCurrent().getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);
    }

    @Override
    public void unlock(ScheduledConnectionTask connectionTask) {
        MeteringWarehouse.getCurrent().getDeviceDataService().unlockConnectionTask(connectionTask);
    }

    @Override
    public OfflineDevice findDevice(DeviceIdentifier<?> identifier) {
        try {
            BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> device = identifier.findDevice();
//            BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device = device1;
            if (device != null) {
                return new OfflineDeviceImpl((Device) device, DeviceOffline.needsEverything);
            } else {
                return null;
            }
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public OfflineRegister findRegister(RegisterIdentifier identifier) {
        try {
            return new OfflineRegisterImpl((com.energyict.mdc.device.data.Register) identifier.findRegister());
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
        EndDeviceMessage deviceMessage = (EndDeviceMessage) identifier.getDeviceMessage();
        return deviceMessage.goOffline();
    }

    @Override
    public void updateIpAddress(String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        final TypedProperties properties = connectionTask.getTypedProperties();
        properties.setProperty(connectionTaskPropertyName, ipAddress);
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
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
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                device.setPhysicalGateway(gatewayDevice);
                return null;
            }
        });
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, final DateFormat timeStampFormat, final String fileExtension, final byte[] contents) {
        final BaseDevice device = deviceIdentifier.findDevice();
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                doStoreConfigurationFile(device, timeStampFormat, fileExtension, contents);
                return null;
            }
        });
    }

    private void doStoreConfigurationFile(BaseDevice device, DateFormat timeStampFormat, String fileExtension, byte[] contents)
            throws
            BusinessException,
            SQLException {
        try {
            String fileName = this.getUniqueUserFileName(timeStampFormat);
            UserFileShadow userFileShadow = new UserFileShadow();
            userFileShadow.setExtension(fileExtension);
            userFileShadow.setName(fileName);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(contents);
            UserFile userFile = MeteringWarehouse.getCurrent().getUserFileFactory().create(userFileShadow);
            userFile.updateContents(byteStream);
        } finally {
            this.closeConnection();
        }
    }

    private String getUniqueUserFileName(DateFormat timeStampFormat) {
        String fileName = "Config_" + timeStampFormat.format(Clocks.getAppServerClock().now());
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
        return MeteringWarehouse.getCurrent().getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer) != null;
    }

    public void unlock(final OutboundConnectionTask connectionTask) {
        MeteringWarehouse.getCurrent().getDeviceDataService().unlockConnectionTask(connectionTask);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        return MeteringWarehouse.getCurrent().getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, comPort) != null;
    }

    @Override
    public void unlock(final ComTaskExecution comTaskExecution) {
        MeteringWarehouse.getCurrent().getDeviceDataService().unlockComTaskExecution(comTaskExecution);
    }

    @Override
    public void executionStarted(final ConnectionTask connectionTask, final ComServer comServer) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                toServerConnectionTask(connectionTask).executionStarted(comServer);
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final ConnectionTask connectionTask) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                toServerConnectionTask(connectionTask).executionCompleted();
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final ConnectionTask connectionTask) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                toServerConnectionTask(connectionTask).executionFailed();
                return null;
            }
        });
    }

    @Override
    public void executionStarted(final ComTaskExecution comTaskExecution, final ComPort comPort) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException {
                toServerComTaskExecution(comTaskExecution).executionStarted(comPort);
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final ComTaskExecution comTaskExecution) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                toServerComTaskExecution(comTaskExecution).executionCompleted();
                return null;
            }
        });
    }

    @Override
    public void executionCompleted(final List<? extends ComTaskExecution> comTaskExecutions) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    toServerComTaskExecution(comTaskExecution).executionCompleted();
                }
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final ComTaskExecution comTaskExecution) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                toServerComTaskExecution(comTaskExecution).executionFailed();
                return null;
            }
        });
    }

    @Override
    public void executionFailed(final List<? extends ComTaskExecution> comTaskExecutions) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    toServerComTaskExecution(comTaskExecution).executionFailed();
                }
                return null;
            }
        });
    }

    @Override
    public void releaseInterruptedTasks(final ComServer comServer) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException {
                getConnectionTaskFactory().releaseInterruptedConnectionTasks(comServer);
                getComTaskExecutionFactory().releaseInterruptedComTasks(comServer);
                return null;
            }
        });
    }

    @Override
    public TimeDuration releaseTimedOutTasks(final ComServer comServer) {
        return this.execute(new Transaction<TimeDuration>() {
            @Override
            public TimeDuration doExecute() throws SQLException {
                getConnectionTaskFactory().releaseTimedOutConnectionTasks(comServer);
                return getComTaskExecutionFactory().releaseTimedOutComTasks(comServer);
            }
        });
    }

    @Override
    public ComSession createOutboundComSession(final ScheduledConnectionTask owner, final ComSessionShadow shadow) {
        return this.execute(new Transaction<ComSession>() {
            @Override
            public ComSession doExecute() throws SQLException, BusinessException {
                return getComSessionFactory().createOutboundComSession(owner, shadow);
            }
        });
    }

    public ComSession createOutboundComSession(final OutboundConnectionTask owner, final ComSessionShadow shadow) {
        return this.execute(new Transaction<ComSession>() {
            @Override
            public ComSession doExecute() throws SQLException, BusinessException {
                return getComSessionFactory().createOutboundComSession(owner, shadow);
            }
        });
    }

    @Override
    public ComSession createInboundComSession(final InboundConnectionTask owner, final ComSessionShadow shadow) {
        return this.execute(new Transaction<ComSession>() {
            @Override
            public ComSession doExecute() throws SQLException, BusinessException {
                if (owner == null) {
                    return getComSessionFactory().createInboundComSession(shadow);
                } else {
                    return getComSessionFactory().createInboundComSession(owner, shadow);
                }
            }
        });
    }

    private EndDeviceCache createOrUpdateDeviceCache(final int deviceId, final DeviceCacheShadow shadow) {
        return this.execute(new Transaction<EndDeviceCache>() {
            @Override
            public EndDeviceCache doExecute() throws SQLException, BusinessException {
                DeviceCacheFactory deviceCacheFactory = getManager().getMdwInterface().getDeviceCacheFactory();
                EndDeviceCache deviceCache = deviceCacheFactory.findByDeviceId(deviceId);
                if (deviceCache == null) {    // create a new one
                    deviceCache = deviceCacheFactory.create(shadow);
                } else {    // update the existing one
                    deviceCache.update(shadow);
                }
                return deviceCache;
            }
        });
    }

    @Override
    public void storeMeterReadings(final DeviceIdentifier<Device> deviceIdentifier, final MeterReading meterReading) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                Device device = deviceIdentifier.findDevice();
                device.store(meterReading);
                return null;
            }
        });
    }

    @Override
    public void signalEvent(final BusinessEvent businessEvent) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws SQLException, BusinessException {
                ManagerFactory.getCurrent().getMdwInterface().signalEvent(businessEvent);
                return null;
            }
        });
    }

    @Override
    public void updateDeviceMessageInformation(final MessageIdentifier messageIdentifier, final DeviceMessageStatus newDeviceMessageStatus, final String protocolInformation) {
        this.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() {
                EndDeviceMessage deviceMessage = (EndDeviceMessage) messageIdentifier.getDeviceMessage();
                if (newDeviceMessageStatus.ordinal() != deviceMessage.getStatus().ordinal()) {  // When the status doesn't change, no update is needed
                    deviceMessage.moveTo(newDeviceMessageStatus);
                }
                deviceMessage.updateProtocolInfo(protocolInformation);
                return null;
            }
        });
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(final DeviceIdentifier deviceIdentifier, final int confirmationCount) {
        return this.execute(new Transaction<List<OfflineDeviceMessage>>() {
            @Override
            public List<OfflineDeviceMessage> doExecute() throws SQLException, BusinessException {
                return convertToOfflineDeviceMessages(doConfirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount));
            }
        });
    }

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

    @Override
    public boolean isStillPending(int comTaskExecutionId) {
        try {
            return this.getComTaskExecutionFactory().isStillPending(comTaskExecutionId);
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        try {
            return this.getComTaskExecutionFactory().areStillPending(comTaskExecutionIds);
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public void setMaxNumberOfTries(ScheduledConnectionTask connectionTask, int maxNumberOfTries) {
        connectionTask.setMaxNumberOfTries(maxNumberOfTries);
        connectionTask.save();
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        try {
            CommunicationDevice device = (CommunicationDevice) deviceIdentifier.findDevice();
            InboundConnectionTask connectionTask = this.getInboundConnectionTask(comPort, device);
            if (connectionTask == null) {
                return null;
            } else {
                SecurityPropertySet securityPropertySet = this.getSecurityPropertySet(device, connectionTask);
                if (securityPropertySet == null) {
                    return null;
                } else {
                    return device.getProtocolSecurityProperties(securityPropertySet);
                }
            }
        } finally {
            this.closeConnection();
        }
    }

    /**
     * Gets the {@link SecurityProperty} that needs to be used when
     * the {@link CommunicationDevice} is communicating to the ComServer
     * via the specified {@link InboundConnectionTask}.
     *
     * @param device         The Device
     * @param connectionTask The ConnectionTask
     * @return The SecurityPropertySet or <code>null</code> if the Device is not ready for inbound communication
     */
    private SecurityPropertySet getSecurityPropertySet(CommunicationDevice device, InboundConnectionTask connectionTask) {
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
        List<ComTaskExecution> comTaskExecutions = ManagerFactory.getCurrent().getComTaskExecutionFactory().findByConnectionTask(connectionTask);
        if (comTaskExecutions.isEmpty()) {
            return null;
        } else {
            return comTaskExecutions.get(0);
        }
    }

    private InboundConnectionTask getInboundConnectionTask(InboundComPort comPort, CommunicationDevice device) {
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
        try {
            CommunicationDevice device = (CommunicationDevice) deviceIdentifier.findDevice(); //TODO ugly casting ...
            InboundConnectionTask connectionTask = this.getInboundConnectionTask(inboundComPort, device);
            if (connectionTask == null) {
                return null;
            } else {
                return connectionTask.getTypedProperties();
            }
        } finally {
            this.closeConnection();
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
        } finally {
            this.closeConnection();
        }
    }

    private ServerManager getManager() {
        return ManagerFactory.getCurrent();
    }

    private ScheduledConnectionTask toServerConnectionTask(ConnectionTask connectionTask) {
        return (ScheduledConnectionTask) connectionTask;
    }

    private ServerComTaskExecution toServerComTaskExecution(ComTaskExecution scheduledComTask) {
        return (ServerComTaskExecution) scheduledComTask;
    }

    private ServerConnectionTaskFactory getConnectionTaskFactory() {
        return ManagerFactory.getCurrent().getConnectionTaskFactory();
    }

    private ServerComTaskExecutionFactory getComTaskExecutionFactory() {
        return ManagerFactory.getCurrent().getComTaskExecutionFactory();
    }

    private ComSessionFactory getComSessionFactory() {
        return ManagerFactory.getCurrent().getComSessionFactory();
    }

    private <T> T execute(Transaction<T> transaction) {
        try {
            return this.getManager().getMdwInterface().execute(transaction);
        } catch (NotFoundException e) {
            throw new DataAccessException(e);
        }
        // Not collapseable because of the constructors of the DataAccessException class
        catch (SQLException e) {
            throw new DataAccessException(e);
        } catch (BusinessException e) {
            throw new ApplicationException(e);
        } finally {
            this.closeConnection();
        }
    }

    private void closeConnection() {
        // Postpone closing until we have left the transaction context.
        Environment.DEFAULT.get().closeConnection();
    }

    private enum FutureMessageState {
        INDOUBT {
            @Override
            public void applyTo(DeviceMessage message) {
                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.INDOUBT);
            }
        },

        FAILED {
            @Override
            public void applyTo(DeviceMessage message) {
                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.FAILED);
            }
        },

        CONFIRMED {
            @Override
            public void applyTo(DeviceMessage message) {
                ((EndDeviceMessage) message).moveTo(DeviceMessageStatus.CONFIRMED);
            }
        };

        public abstract void applyTo(DeviceMessage message) throws BusinessException, SQLException;

    }

}