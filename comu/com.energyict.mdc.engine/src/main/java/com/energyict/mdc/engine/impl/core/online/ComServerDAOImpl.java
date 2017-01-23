package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.DLMSKeyStoreUserFile;
import com.energyict.mdc.engine.impl.PropertyValueType;
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
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.security.CertificateAlias;
import com.energyict.mdc.upl.security.CertificateWrapper;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.security.cert.CertificateException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.streams.Currying.use;


/**
 * Provides a default implementation for the {@link ComServerDAO} interface
 * that uses the EIServer persistence framework for all requests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-07 (08:58)
 */
public class ComServerDAOImpl implements ComServerDAO {

    private final ServiceProvider serviceProvider;
    private final User comServerUser;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;

    public ComServerDAOImpl(ServiceProvider serviceProvider, User comServerUser) {
        this.serviceProvider = serviceProvider;
        this.comServerUser = comServerUser;
    }

    private EngineConfigurationService getEngineModelService() {
        return this.serviceProvider.engineConfigurationService();
    }

    private DeviceService getDeviceService() {
        return this.serviceProvider.deviceService();
    }

    private KeyStoreService getKeyStoreService() {
        return this.serviceProvider.keyStoreService();
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

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        Optional<? extends ComPort> reloaded = getEngineModelService().findComPort(comPort.getId());
        if (!reloaded.isPresent() || reloaded.get().isObsolete()) {
            return null;
        } else if (reloaded.get().getModificationDate().isAfter(comPort.getModificationDate())) {
            return reloaded.get();
        } else {
            return comPort;
        }
    }

    private Optional<ConnectionTask> refreshConnectionTask(ConnectionTask connectionTask) {
        Optional<ConnectionTask> reloaded = getConnectionTaskService().findConnectionTask(connectionTask.getId());
        if (reloaded.isPresent()) {
            if (reloaded.get().getVersion() == connectionTask.getVersion()) {
                reloaded = Optional.of(connectionTask);
            }
            if (reloaded.get().isObsolete()) {
                reloaded = Optional.empty();
            }
        }
        return reloaded;
    }

    private Optional<ConnectionTask> lockConnectionTask(ConnectionTask connectionTask) {
        ConnectionTask reloaded = getConnectionTaskService().attemptLockConnectionTask(connectionTask.getId());
        if (reloaded != null) {
            if (reloaded.getVersion() == connectionTask.getVersion()) {
                return Optional.of(connectionTask);
            }
            return Optional.of(reloaded);
        }
        return Optional.empty();
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
        Device device = getDeviceService().findDeviceById(offlineDevice.getId()).get();
        return getCommunicationTaskService().getPlannedComTaskExecutionsFor(comPort, device);
    }

    @Override
    public PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName) {
        return getDeviceService()
                    .findDeviceByIdentifier(deviceIdentifier)
                    .flatMap(Device::getDeviceProtocolPluggableClass)
                    .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                    .map(use(this::getPropertyValueType).with(propertyName))
                    .orElse(PropertyValueType.UNSUPPORTED);
    }

    private PropertyValueType getPropertyValueType(DeviceProtocol deviceProtocol, String propertyName) {
        List<PropertySpec> allPropertySpecs = new ArrayList<>();
        allPropertySpecs.addAll(deviceProtocol.getPropertySpecs());
        for (DeviceProtocolDialect deviceProtocolDialect : deviceProtocol.getDeviceProtocolDialects()) {
            allPropertySpecs.addAll(deviceProtocolDialect.getPropertySpecs());
        }
        for (PropertySpec propertySpec : allPropertySpecs) {
            if (propertySpec.getName().equals(propertyName)) {
                return PropertyValueType.from(propertySpec);
            }
        }
        return PropertyValueType.UNSUPPORTED;
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(final ConnectionTask connectionTask) {
        return this.serviceProvider.transactionService().execute(connectionTask::getProperties);
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, final ComServer comServer) {
        try {
            return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comServer);
        } catch (OptimisticLockException e) {
            Optional reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                return getConnectionTaskService().attemptLockConnectionTask((ScheduledConnectionTask) reloaded.get(), comServer);
            }
            return null;
        }
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return findOfflineDevice(identifier, DeviceOffline.needsEverything);
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        Device device = this.findDevice(identifier);
        if (device != null) {
            return Optional.of(new OfflineDeviceImpl(device, offlineDeviceContext, new OfflineDeviceServiceProvider()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        Register register = this.findRegister(identifier);
        return Optional.of(new OfflineRegisterImpl(this.getStorageRegister(register, when), this.serviceProvider.identificationService()));
    }

    private Register findRegister(RegisterIdentifier identifier) {
        return this.serviceProvider
                .registerService()
                .find(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Register with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        LoadProfile loadProfile = this.findLoadProfile(loadProfileIdentifier);
        return Optional.of(new OfflineLoadProfileImpl(loadProfile, this.serviceProvider.topologyService(), this.serviceProvider.identificationService()));
    }

    private LoadProfile findLoadProfile(LoadProfileIdentifier identifier) {
        return this.serviceProvider
                .loadProfileService()
                .findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LoadProfile with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        LogBook logBook = this.findLogBook(logBookIdentifier);
        return Optional.of(new OfflineLogBookImpl(logBook, this.serviceProvider.identificationService()));
    }

    private LogBook findLogBook(LogBookIdentifier identifier) {
        return this.serviceProvider
                .logBookService()
                .findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LogBook with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        DeviceMessage deviceMessage = this.findDeviceMessage(identifier);
        Device device = (Device) deviceMessage.getDevice(); //Downcast to the Connexo Device
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = device.getDeviceType().getDeviceProtocolPluggableClass();
        if (deviceProtocolPluggableClass.isPresent()) {
            return Optional.of(
                    new OfflineDeviceMessageImpl(
                            deviceMessage,
                            deviceProtocolPluggableClass.get().getDeviceProtocol(),
                            this.serviceProvider.identificationService(),
                            new OfflineDeviceImpl(device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider())
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    private DeviceMessage findDeviceMessage(MessageIdentifier identifier) {
        return this.serviceProvider
                .deviceMessageService()
                .findDeviceMessageByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("DeviceMessage with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public void updateIpAddress(String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        try {
            connectionTask.setProperty(connectionTaskPropertyName, ipAddress);
            connectionTask.save();
        } catch (OptimisticLockException e) {
            Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                reloaded.get().setProperty(connectionTaskPropertyName, ipAddress);
                reloaded.get().save();
            }
        }
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        this.executeTransaction(() -> {
            Device device = this.findDevice(deviceIdentifier);
            device.setProtocolProperty(propertyName, propertyValue);
            device.save();
            return null;
        });
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        this.executeTransaction(() -> {
            handleCertificatePropertyValue(propertyValue);

            //Now update the general property
            Device device = findDevice(deviceIdentifier);
            device.setProtocolProperty(propertyName, propertyValue);
            device.save();
            return null;
        });
    }

    /**
     * If the provided property value holds a certificate, make sure to store it in the proper place.
     */
    private void handleCertificatePropertyValue(Object propertyValue) {
        if (propertyValue instanceof CertificateAlias) {
            // If the property value is of type CertificateAlias, add the given certificate in the DLMS key store, under the given alias.
            this.doAddCACertificate((CertificateAlias) propertyValue);
        } else if (propertyValue instanceof CertificateWrapperId) {
            /* If the property value is of type CertificateWrapperId, add the given certificate in the trust store.
             * The id of the created {@link CertificateWrapper} will be set as property value on the device. */
            CertificateWrapperId certificateWrapperId = (CertificateWrapperId) propertyValue;
            int id = this.doAddEndDeviceCertificate(certificateWrapperId);
            certificateWrapperId.setId(id);
        }
    }

    @Override
    public void addCACertificate(final CertificateAlias certificateAlias) {
        this.executeTransaction(() -> {
            this.doAddCACertificate(certificateAlias);
            return null;
        });
    }

    private void doAddCACertificate(CertificateAlias certificateAlias) {
        try {
            new DLMSKeyStoreUserFile(this.getKeyStoreService())
                    .importCertificate(
                        certificateAlias.getCertificate(),
                        certificateAlias.getAlias());
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int addEndDeviceCertificate(final CertificateWrapperId certificateWrapperId) {
        return this.executeTransaction(() -> this.doAddEndDeviceCertificate(certificateWrapperId));
    }

    private int doAddEndDeviceCertificate(CertificateWrapperId certificateWrapperId) {
        CertificateWrapperShadow certificateWrapperShadow = certificateWrapperId.createCertificateWrapperShadow();
        if (certificateWrapperShadow != null) {
            CertificateWrapper certificateWrapper = MeteringWarehouse.getCurrent().getCertificateWrapperFactory().create(certificateWrapperShadow);
            return certificateWrapper.getId();
        }
        return 0;
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        this.executeTransaction(() -> {
            handleCertificatePropertyValue(propertyValue);

            //Now update the given security property.
            Device device = this.findDevice(deviceIdentifier);
            device.setSecurityProperty(propertyName, propertyValue);
            return null;
        });
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        Device device = this.findDevice(deviceIdentifier);
        Device gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = this.findDevice(gatewayDeviceIdentifier);
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
    public void storeConfigurationFile(DeviceIdentifier identifier, final DateTimeFormatter timeStampFormat, final String fileExtension, final byte[] contents) {
        Device device = this.findDevice(identifier);
        this.doStoreConfigurationFile(device, timeStampFormat, fileExtension, contents);
    }

    private void doStoreConfigurationFile(Device device, DateTimeFormatter timeStampFormat, String fileExtension, byte[] contents) {
        throw new RuntimeException("Storing of UserFiles is currently not supported ...");
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComServer comServer) {
        return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comServer) != null;
    }

    public void unlock(final OutboundConnectionTask connectionTask) {
        try {
            getConnectionTaskService().unlockConnectionTask(connectionTask);
        } catch (OptimisticLockException e) {
            Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                getConnectionTaskService().unlockConnectionTask(reloaded.get());
            }
        }
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
    public ConnectionTask<?, ?> executionStarted(final ConnectionTask connectionTask, final ComServer comServer) {
        return this.executeTransaction(() -> {
            Optional<ConnectionTask> lockedConnectionTask = lockConnectionTask(connectionTask);
            if (lockedConnectionTask.isPresent()) {
                ConnectionTask connectionTask1 = lockedConnectionTask.get();
                connectionTask1.executionStarted(comServer);
                return connectionTask1;
            }
            return null;
        });
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(final ConnectionTask connectionTask) {
        ConnectionTask updatedConnectionTask = null;
        try {
            connectionTask.executionCompleted();
            updatedConnectionTask = connectionTask;
        } catch (OptimisticLockException e) {
            final Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                updatedConnectionTask = reloaded.get();
                reloaded.get().executionCompleted();
            }
        }
        return updatedConnectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed(final ConnectionTask connectionTask) {
        ConnectionTask updatedConnectionTask = null;
        try {
            connectionTask.executionFailed();
            updatedConnectionTask = connectionTask;
        } catch (OptimisticLockException e) {
            final Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                updatedConnectionTask = reloaded.get();
                updatedConnectionTask.executionFailed();
            }
        }
        return updatedConnectionTask;
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
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        getCommunicationTaskService().executionRescheduled(comTaskExecution, rescheduleDate);
    }

    @Override
    public void executionCompleted(final List<? extends ComTaskExecution> comTaskExecutions) {
        boolean connectionTaskWasNotLocked = true;
        for (ComTaskExecution execution : comTaskExecutions) {
            if (connectionTaskWasNotLocked) {
                connectionTaskWasNotLocked = false;
                execution.getConnectionTask().ifPresent(this::lockConnectionTask);
            }
            getCommunicationTaskService().executionCompletedFor(execution);

        }
    }

    @Override
    public void executionFailed(final ComTaskExecution comTaskExecution) {
        getCommunicationTaskService().executionFailedFor(comTaskExecution);
    }

    @Override
    public void executionFailed(final List<? extends ComTaskExecution> comTaskExecutions) {
        comTaskExecutions.forEach(comTaskExecution -> getCommunicationTaskService().executionFailedFor(comTaskExecution));
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
    public void releaseTasksFor(final ComPort comPort) {
        this.executeTransaction(() -> {
            List<ComTaskExecution> comTaskExecutionsWhichAreExecuting = getCommunicationTaskService().findComTaskExecutionsWhichAreExecuting(comPort);
            Set<ConnectionTask> lockedConnectionTasks = new HashSet<>();

            if (comTaskExecutionsWhichAreExecuting.isEmpty()) {
                // There are no ComTaskExec locked for this com port,
                // so we might be in the case where only the connection task is locked

                lockedConnectionTasks.addAll(getLockedByComServer(comPort.getComServer()));
            } else {
                for (ComTaskExecution comTaskExecution : comTaskExecutionsWhichAreExecuting) {
                    if (comTaskExecution.getConnectionTask().isPresent()) {
                        lockedConnectionTasks.add(comTaskExecution.getConnectionTask().get());
                        getCommunicationTaskService().unlockComTaskExecution(comTaskExecution);
                    }
                }
            }

            // unlock the connection tasks (after all affected ComTaskExecs are unlocked)
            for (ConnectionTask lockedConnectionTask : lockedConnectionTasks) {
                if (lockedConnectionTask instanceof OutboundConnectionTask) {
                    unlock((OutboundConnectionTask) lockedConnectionTask);
                }
            }
            return null;
        });
    }

    /**
     * Find and release any ComTaskExec executed by a ComPort
     * <p>
     * Those tasks will not appear busy anymore, but the ComServer will still continue with these tasks until they are actually finished
     * Normally no other port will pick it up until the nextExecutionTimeStamp has passed,
     * but we update that nextExecutionTimestamp to the next according to his schedule in the beginning of the session (from Govanni)
     */
    private void unlockAllTasksByComPort(ComPort otherComPort) {
        for (ComTaskExecution otherComTaskExec : getCommunicationTaskService().findComTaskExecutionsWhichAreExecuting(otherComPort)) {
            getCommunicationTaskService().unlockComTaskExecution(otherComTaskExec);
        }
    }

    /**
     * Find connections locked by a comServer
     */
    private List<ConnectionTask> getLockedByComServer(ComServer comServer) {
        return getConnectionTaskService().findLockedByComServer(comServer);
    }

    @Override
    public ComSession createComSession(final ComSessionBuilder builder, Instant stopDate, final ComSession.SuccessIndicator successIndicator) {
        /* We should already be in a transaction so don't wrap it again */
        return builder.endSession(stopDate, successIndicator).create();
    }

    @Override
    public void storeMeterReadings(final DeviceIdentifier identifier, final MeterReading meterReading) {
        Device device = this.findDevice(identifier);
        device.store(meterReading);
    }

    @Override
    public void signalEvent(String topic, Object source) {
        this.serviceProvider.eventService().postEvent(topic, source);
    }

    @Override
    public void updateDeviceMessageInformation(final MessageIdentifier messageIdentifier, final DeviceMessageStatus newDeviceMessageStatus, final Instant sentDate, final String protocolInformation) {
        DeviceMessage deviceMessage = this.findDeviceMessage(messageIdentifier);
        try {
            updateDeviceMessage(newDeviceMessageStatus, sentDate, protocolInformation, deviceMessage);
        } catch (OptimisticLockException e) { // if someone tried to update the message while the ComServer was executing it ...
            DeviceMessage reloadedDeviceMessage = getDeviceService().findDeviceById(((Device) deviceMessage.getDevice()).getId())   //Downcast to Connexo Device
                    .get()
                    .getMessages()
                    .stream()
                    .filter(deviceDeviceMessage -> deviceDeviceMessage.getId() == deviceMessage.getId())
                    .findAny()
                    .get();
            updateDeviceMessage(newDeviceMessageStatus, sentDate, protocolInformation, reloadedDeviceMessage);
        }
    }

    private void updateDeviceMessage(DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation, DeviceMessage deviceMessage) {
        deviceMessage.setSentDate(sentDate);
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
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(final DeviceIdentifier deviceIdentifier, final int confirmationCount) {
        return convertToOfflineDeviceMessages(doConfirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount));
    }

    private List<OfflineDeviceMessage> convertToOfflineDeviceMessages(List<DeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> offlineDeviceMessages = new ArrayList<>(deviceMessages.size());
        deviceMessages.forEach(deviceMessage -> {
                    Device device = (Device) deviceMessage.getDevice();
                    if (device.getDeviceProtocolPluggableClass().isPresent()) {       //Downcast to Connexo Device
                        offlineDeviceMessages.add(new OfflineDeviceMessageImpl(
                                deviceMessage,
                                device.getDeviceProtocolPluggableClass().get().getDeviceProtocol(),
                                serviceProvider.identificationService(),
                                new OfflineDeviceImpl(device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider())
                        ));
                    }
                }
        );
        return offlineDeviceMessages;
    }

    private List<DeviceMessage> doConfirmSentMessagesAndGetPending(DeviceIdentifier identifier, int confirmationCount) {
        Device device = this.findDevice(identifier);
        return this.doConfirmSentMessagesAndGetPending(device, confirmationCount);
    }

    private List<DeviceMessage> doConfirmSentMessagesAndGetPending(Device device, int confirmationCount) {
        this.updateSentMessageStates(device, confirmationCount);
        return this.findPendingMessageAndMarkAsSent(device);
    }

    private void updateSentMessageStates(Device device, int confirmationCount) {
        List<DeviceMessage> sentMessages = device.getMessagesByState(DeviceMessageStatus.SENT);
        FutureMessageState newState = this.getFutureMessageState(sentMessages, confirmationCount);
        sentMessages.forEach(newState::applyTo);
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

    private List<DeviceMessage> findPendingMessageAndMarkAsSent(Device device) {
        List<DeviceMessage> pendingMessages = device.getMessagesByState(DeviceMessageStatus.PENDING);
        List<DeviceMessage> sentMessages = new ArrayList<>(pendingMessages.size());
        for (DeviceMessage pendingMessage : pendingMessages) {
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
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier identifier, InboundComPort comPort) {
        Device device = this.findDevice(identifier);
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
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier identifier, InboundComPort inboundComPort) {
        Device device = this.findDevice(identifier);
        ConnectionTask<?, ?> connectionTask = this.getInboundConnectionTask(inboundComPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            return connectionTask.getTypedProperties();
        }
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier identifier) {
        try {
            Device device = this.findDevice(identifier);
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
    @SuppressWarnings("unchecked")
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        LoadProfile loadProfile = this.findLoadProfile(loadProfileIdentifier);
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice((loadProfile).getDevice());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        LogBook logBook = this.findLogBook(logBookIdentifier);
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(logBook.getDevice());
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        LogBook logBook = this.findLogBook(logBookIdentifier);
        // Refresh device and LogBook to avoid OptimisticLockException
        Device device = this.serviceProvider.deviceService().findDeviceById(logBook.getDevice().getId()).get();
        LogBook refreshedLogBook = device.getLogBooks().stream().filter(each -> each.getId() == logBook.getId()).findAny().get();
        LogBook.LogBookUpdater logBookUpdater = device.getLogBookUpdaterFor(refreshedLogBook);
        logBookUpdater.setLastLogBookIfLater(lastLogBook);
        logBookUpdater.setLastReadingIfLater(lastLogBook); // We assume the event will be persisted with a time difference of only a few milliseconds
        logBookUpdater.update();
    }

    @Override
    public void storePathSegments(DeviceIdentifier sourceDeviceIdentifier, List<TopologyPathSegment> topologyPathSegments) {
        TopologyService.G3CommunicationPathSegmentBuilder g3CommunicationPathSegmentBuilder = serviceProvider.topologyService()
                .addCommunicationSegments(this.findDevice(sourceDeviceIdentifier));
        topologyPathSegments.forEach(topologyPathSegment -> {
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
        TopologyService.G3NeighborhoodBuilder g3NeighborhoodBuilder = serviceProvider.topologyService().buildG3Neighborhood(this.findDevice(sourceDeviceIdentifier));
        topologyNeighbours.forEach(topologyNeighbour -> {
            Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(topologyNeighbour.getNeighbour());
            if (optionalDevice.isPresent()) {
                TopologyService.G3NeighborBuilder g3NeighborBuilder = g3NeighborhoodBuilder.addNeighbor(optionalDevice.get(), ModulationScheme.fromId(topologyNeighbour.getModulationSchema()), Modulation
                        .fromOrdinal(topologyNeighbour.getModulation()), PhaseInfo.fromId(topologyNeighbour.getPhaseDifferential()));
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

    private Device findDevice(DeviceIdentifier identifier) {
        return this
                .getOptionalDeviceByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Device with identifier " + identifier.toString() + " does not exist"));
    }

    private Optional<Device> getOptionalDeviceByIdentifier(DeviceIdentifier deviceIdentifier) {
        return this.getDeviceService().findDeviceByIdentifier(deviceIdentifier);
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

    @Override
    public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(collectedBreakerStatus.getDeviceIdentifier());
        optionalDevice.ifPresent(device -> {
            BreakerStatusStorage breakerStatusStorage = new BreakerStatusStorage(getDeviceService(), serviceProvider.clock());
            breakerStatusStorage.updateBreakerStatus(collectedBreakerStatus.getBreakerStatus(), device);
        });
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        LoadProfile loadProfile = this.findLoadProfile(loadProfileIdentifier);
        // Refresh the device and the LoadProfile to avoid OptimisticLockException
        Device device = this.serviceProvider.deviceService().findDeviceById(loadProfile.getDevice().getId()).get();
        LoadProfile refreshedLoadProfile = device.getLoadProfiles().stream().filter(each -> each.getId() == loadProfile.getId()).findAny().get();
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(refreshedLoadProfile);
        loadProfileUpdater.setLastReadingIfLater(lastReading);
        loadProfileUpdater.update();
    }

    @Override
    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> loadProfileUpdate, Map<LogBookIdentifier, Instant> logBookUpdate) {
        loadProfileUpdate.entrySet().stream().forEach(entrySet -> this.updateLoadProfile(this.findLoadProfile(entrySet.getKey()), entrySet.getValue()));
        logBookUpdate.entrySet().stream().forEach(entrySet -> this.updateLogBook(this.findLogBook(entrySet.getKey()), entrySet.getValue()));
    }

    private void updateLoadProfile(LoadProfile loadProfile, Instant lastReading) {
        loadProfile
            .getDevice()
            .getLoadProfileUpdaterFor(loadProfile)
            .setLastReadingIfLater(lastReading)
            .update();
    }

    private void updateLogBook(LogBook logBook, Instant lastLogBook) {
        logBook
            .getDevice()
            .getLogBookUpdaterFor(logBook)
            .setLastReadingIfLater(lastLogBook)
            .update();
    }

    @Override
    public void updateCalendars(CollectedCalendar collectedCalendar) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(collectedCalendar.getDeviceIdentifier());
        optionalDevice.ifPresent(device -> this.updateCalendars(collectedCalendar, device));
    }

    private void updateCalendars(CollectedCalendar collectedCalendar, Device device) {
        device.calendars().updateCalendars(collectedCalendar);
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {
        this.executeTransaction(() -> {
            getDeviceService().deleteOutdatedComTaskExecutionTriggers();
            return null;
        });
    }

    @Override
    public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile offlineLoadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
        final Device dataLogger = this.findDevice(offlineLoadProfile.getDeviceIdentifier());
        if (dataLogger != null) {
            if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
                Optional<Channel> dataLoggerChannel = dataLogger.getChannels().stream().filter((c) -> c.getReadingType().getMRID().equals(readingTypeMRID)).findFirst();
                if (dataLoggerChannel.isPresent()) {
                    List<DataLoggerChannelUsage> dataLoggerChannelUsages = this.serviceProvider.topologyService().findDataLoggerChannelUsagesForChannels(dataLoggerChannel.get(), dataPeriod);
                    List<Pair<OfflineLoadProfile, Range<Instant>>> linkedOffLineLoadProfiles = new ArrayList<>();
                    // 'linked' periods
                    if (!dataLoggerChannelUsages.isEmpty()) {
                        dataLoggerChannelUsages.forEach(usage -> {
                            Device slave = usage.getDataLoggerReference().getOrigin();
                            List<? extends ReadingType> slaveChannelReadingTypes = usage.getSlaveChannel().getReadingTypes();
                            Optional<Channel> slaveChannel = slave.getChannels().stream().filter((c) -> slaveChannelReadingTypes.contains(c.getReadingType())).findFirst();
                            if (slaveChannel.isPresent()) {
                                OfflineLoadProfile dataLoggerSlaveOfflineLoadProfile = new OfflineLoadProfileImpl(slaveChannel.get()
                                        .getLoadProfile(), this.serviceProvider.topologyService(), this.serviceProvider.identificationService()) {
                                    protected void goOffline() {
                                        super.goOffline();
                                        // To avoid to have to retrieve the involved slave channels again
                                        setAllLoadProfileChannels(convertToOfflineChannels(Collections.singletonList(slaveChannel.get())));
                                    }

                                    @Override
                                    public boolean isDataLoggerSlaveLoadProfile() {
                                        return true;
                                    }
                                };

                                linkedOffLineLoadProfiles.add(Pair.of(dataLoggerSlaveOfflineLoadProfile, usage.getRange().intersection(dataPeriod)));
                            }
                        });
                    }
                    //'unlinked periods'
                    if (linkedOffLineLoadProfiles.isEmpty()) {
                        linkedOffLineLoadProfiles.add(Pair.of(offlineLoadProfile, dataPeriod));   //All data is stored on the data logger channel
                    } else {
                        RangeSet<Instant> unlinkedPeriods = TreeRangeSet.create();
                        unlinkedPeriods.add(dataPeriod);
                        linkedOffLineLoadProfiles
                                .stream()
                                .map(Pair::getLast)
                                .forEach(unlinkedPeriods::remove);
                        unlinkedPeriods
                                .asRanges()
                                .stream()
                                .map(unlinkedRange -> Pair.of(offlineLoadProfile, unlinkedRange))
                                .forEach(linkedOffLineLoadProfiles::add);
                    }
                    return linkedOffLineLoadProfiles;
                }
            }
        }
        return Collections.singletonList(Pair.of(offlineLoadProfile, dataPeriod));
    }

    @Override
    public User getComServerUser() {
        return comServerUser;
    }

    private Register getStorageRegister(Register register, Instant readingDate) {
        final Device dataLogger = register.getDevice();
        if (dataLogger != null) {
            if (dataLogger.getDeviceConfiguration().isDataloggerEnabled()) {
                Optional<Register> slaveRegister = this.serviceProvider.topologyService().getSlaveRegister(register, readingDate);
                if (slaveRegister.isPresent()) {
                    return slaveRegister.get();
                }
            }
        }
        return register;
    }

    private Instant now() {
        return this.serviceProvider.clock().instant();
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

    public interface ServiceProvider {

        Clock clock();

        Thesaurus thesaurus();

        EngineConfigurationService engineConfigurationService();

        ConnectionTaskService connectionTaskService();

        CommunicationTaskService communicationTaskService();

        DeviceService deviceService();

        RegisterService registerService();

        LoadProfileService loadProfileService();

        LogBookService logBookService();

        DeviceMessageService deviceMessageService();

        TopologyService topologyService();

        EngineService engineService();

        TransactionService transactionService();

        EventService eventService();

        IdentificationService identificationService();

        KeyStoreService keyStoreService();

        FirmwareService firmwareService();

        DeviceConfigurationService deviceConfigurationService();
    }

    private class OfflineDeviceServiceProvider implements OfflineDeviceImpl.ServiceProvider {

        @Override
        public Thesaurus thesaurus() {
            return serviceProvider.thesaurus();
        }

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

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return serviceProvider.deviceConfigurationService();
        }

        @Override
        public FirmwareService firmwareService() {
            return serviceProvider.firmwareService();
        }

    }

}