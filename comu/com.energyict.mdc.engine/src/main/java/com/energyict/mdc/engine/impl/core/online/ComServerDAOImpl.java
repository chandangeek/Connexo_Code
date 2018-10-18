/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.TypedPropertiesValueAdapter;
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
import com.energyict.mdc.device.topology.G3NodeState;
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
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceMessageImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLogBookImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComJobFactory;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.mdc.engine.impl.core.MultiThreadedComJobFactory;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.SingleThreadedComJobFactory;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
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
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.use;
import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;


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

    private SecurityManagementService getSecurityManagementService() {
        return this.serviceProvider.securityManagementService();
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
        List<ConnectionTaskProperty> connectionTaskProperties = this.serviceProvider.transactionService().execute(connectionTask::getProperties);
        return adaptToUPLValues(connectionTaskProperties);
    }

    private List<ConnectionTaskProperty> adaptToUPLValues(List<ConnectionTaskProperty> connectionTaskProperties) {
        return connectionTaskProperties.stream().map(this::newConnectionTaskProperty).collect(Collectors.toList());
    }

    private ConnectionTaskProperty newConnectionTaskProperty(ConnectionTaskProperty property) {
        return new ConnectionTaskProperty() {
            @Override
            public PluggableClass getPluggableClass() {
                return property.getConnectionTask().getPluggableClass();
            }

            @Override
            public String getName() {
                return property.getName();
            }

            @Override
            public Object getValue() {
                return TypedPropertiesValueAdapter.adaptToUPLValue(property.getConnectionTask().getDevice(), property.getValue());
            }

            @Override
            public boolean isInherited() {
                return property.isInherited();
            }

            @Override
            public Range<Instant> getActivePeriod() {
                return property.getActivePeriod();
            }

            @Override
            public ConnectionTask getConnectionTask() {
                return property.getConnectionTask();
            }
        };
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
        Optional<Device> device = getDeviceFor(identifier);
        return device.map(device1 -> new OfflineDeviceImpl(device1, offlineDeviceContext, new OfflineDeviceServiceProvider()));
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        Optional<Register> register = this.findRegister(identifier);
        return register.map(register1 -> new OfflineRegisterImpl(this.getStorageRegister(register1, when), this.serviceProvider.identificationService()));
    }

    private Optional<Register> findRegister(RegisterIdentifier identifier) {
        return this.serviceProvider
                .registerService()
                .findByIdentifier(identifier);
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        Optional<LoadProfile> loadProfile = this.findLoadProfile(loadProfileIdentifier);
        return loadProfile.map(loadProfile1 -> new OfflineLoadProfileImpl(loadProfile1, this.serviceProvider.topologyService(), this.serviceProvider.identificationService()));
    }

    private Optional<LoadProfile> findLoadProfile(LoadProfileIdentifier identifier) {
        return this.serviceProvider
                .loadProfileService()
                .findByIdentifier(identifier);
    }

    private LoadProfile findLoadProfileOrThrowException(LoadProfileIdentifier identifier) {
        return this.findLoadProfile(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LoadProfile with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        Optional<LogBook> logBook = this.findLogBook(logBookIdentifier);
        return logBook.map(logBook1 -> new OfflineLogBookImpl(logBook1, this.serviceProvider.identificationService()));
    }

    private Optional<LogBook> findLogBook(LogBookIdentifier identifier) {
        return this.serviceProvider
                .logBookService()
                .findByIdentifier(identifier);
    }

    private LogBook findLogBookOrThrowException(LogBookIdentifier identifier) {
        return findLogBook(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LogBook with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        Optional<DeviceMessage> optionalDeviceMessage = this.findDeviceMessage(identifier);
        if (optionalDeviceMessage.isPresent()) {
            DeviceMessage deviceMessage = optionalDeviceMessage.get();
            Device device = (Device) deviceMessage.getDevice(); //Downcast to the Connexo Device
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = device.getDeviceType().getDeviceProtocolPluggableClass();
            if (deviceProtocolPluggableClass.isPresent()) {
                return Optional.of(
                        new OfflineDeviceMessageImpl(
                                deviceMessage,
                                deviceProtocolPluggableClass.get().getDeviceProtocol(),
                                this.serviceProvider.identificationService(),
                                this.serviceProvider.protocolPluggableService(),
                                new OfflineDeviceImpl(device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider())
                        )
                );
            }
        }
        return Optional.empty();
    }

    private Optional<DeviceMessage> findDeviceMessage(MessageIdentifier identifier) {
        return this.serviceProvider
                .deviceMessageService()
                .findDeviceMessageByIdentifier(identifier);
    }

    private DeviceMessage findDeviceMessageOrThrowException(MessageIdentifier identifier) {
        return findDeviceMessage(identifier)
                .orElseThrow(() -> new IllegalArgumentException("DeviceMessage with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        try {
            connectionTask.setProperty(connectionTaskPropertyName, propertyValue);
            connectionTask.saveAllProperties();
        } catch (OptimisticLockException e) {
            Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                reloaded.get().setProperty(connectionTaskPropertyName, propertyValue);
                reloaded.get().saveAllProperties();
            }
        }
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        handleCertificatePropertyValue(propertyValue); //TODO: is this step still required?

        Device device = this.findDevice(deviceIdentifier);
        device.setProtocolProperty(propertyName, propertyValue);
        device.save();
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        Device device = findDevice(deviceIdentifier);
        //TODO implement
    }

    /**
     * If the provided property value holds a certificate, make sure to store it in the proper place.
     */
    private void handleCertificatePropertyValue(Object propertyValue) {
        //TODO insert the new certificate in the CertificateWrapper table or in a trust store (do we want this?)
 /*       if (propertyValue instanceof CertificateWrapper) {
            // If the property value is of type CertificateAlias, add the given certificate in the DLMS key store, under the given alias.
            this.doAddCACertificate((CertificateWrapper) propertyValue);
        } else if (propertyValue instanceof CollectedCertificateWrapper) {
            // If the property value is a CollectedCertificateWrapper then add the certificate in the trust store.
            this.doAddEndDeviceCertificate((CollectedCertificateWrapper) propertyValue);
        }*/
    }

    @Override
    public void addCACertificate(final CertificateWrapper certificateWrapper) {
        this.executeTransaction(() -> {
            this.doAddCACertificate(certificateWrapper);
            return null;
        });
    }

    private void doAddCACertificate(CertificateWrapper certificateWrapper) {
        //TODO create CertificateWrapper entry
    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return this.executeTransaction(() -> this.doAddEndDeviceCertificate(collectedCertificateWrapper));
    }

    private long doAddEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        // Todo: wait for PKI feature (CXO-3603) to be implemented and merged into this branch
        throw new UnsupportedOperationException("Waiting for implementation of the PKI feature (CXO-3603)");
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        //TODO: re-add usefull implementation
//        handleCertificatePropertyValue(propertyValue);
//
//        //Now update the given security property.
//        Device device = this.findDevice(deviceIdentifier);
//        device.setSecurityProperty(propertyName, propertyValue);
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        Device device = this.findDevice(deviceIdentifier);
        Optional<Device> gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = this.getDeviceFor(gatewayDeviceIdentifier);
        } else {
            gatewayDevice = Optional.empty();
        }
        if (gatewayDevice.isPresent()) {
            this.serviceProvider.topologyService().setPhysicalGateway(device, gatewayDevice.get());
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
        unlock((com.energyict.mdc.device.data.tasks.ConnectionTask) connectionTask);
    }

    public void unlock(final ConnectionTask connectionTask) {
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
            //first of all, lock the comserver object so you don't run into a deadlock
            ComServer lockedComServer = getEngineModelService().lockComServer(comPort.getComServer());

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
                unlock(lockedConnectionTask);
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
        DeviceMessage deviceMessage = this.findDeviceMessageOrThrowException(messageIdentifier);
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
                                serviceProvider.protocolPluggableService(),
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
        this.executeTransaction(() -> {
            sentMessages.forEach(newState::applyTo);
            return null;
        });
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
        this.executeTransaction(() -> {
            for (DeviceMessage pendingMessage : pendingMessages) {
                pendingMessage.updateDeviceMessageStatus(DeviceMessageStatus.SENT);
                sentMessages.add(pendingMessage);
            }
            return null;
        });
        return sentMessages;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return getTransactionService().execute(transaction);
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        Device device = this.findDevice(deviceIdentifier);
        InboundConnectionTask connectionTask = this.getInboundConnectionTask(comPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            SecurityPropertySet securityPropertySet = this.getSecurityPropertySet(device, connectionTask);
            if (securityPropertySet == null) {
                return null;
            } else {
                return new DeviceProtocolSecurityPropertySetImpl(
                        securityPropertySet.getName(),
                        securityPropertySet.getClient(),
                        securityPropertySet.getAuthenticationDeviceAccessLevel().getId(),
                        securityPropertySet.getEncryptionDeviceAccessLevel().getId(),
                        securityPropertySet.getSecuritySuite() != null ? securityPropertySet.getSecuritySuite().getId() : -1,
                        securityPropertySet.getRequestSecurityLevel() != null ? securityPropertySet.getRequestSecurityLevel().getId() : -1,
                        securityPropertySet.getResponseSecurityLevel() != null ? securityPropertySet.getResponseSecurityLevel().getId() : -1,
                        device.getSecurityProperties(securityPropertySet)
                );
            }
        }
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Device device = this.findDevice(deviceIdentifier);
        InboundConnectionTask connectionTask = this.getInboundConnectionTask(inboundComPort, device);
        if (connectionTask != null) {
            if (connectionTask.getProtocolDialectConfigurationProperties() != null) {
                ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = connectionTask.getProtocolDialectConfigurationProperties();
                String dialectName = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
                Device masterDevice = connectionTask.getDevice();   //Use the master device, this one holds the actual dialect properties
                Optional<ProtocolDialectProperties> protocolDialectProperties = masterDevice.getProtocolDialectProperties(dialectName);
                if (protocolDialectProperties.isPresent()) {
                    final TypedProperties result = protocolDialectProperties.get().getTypedProperties();
                    result.setProperty(DEVICE_PROTOCOL_DIALECT.getName(), protocolDialectProperties.get().getDeviceProtocolDialectName());

                    DeviceProtocolDialect deviceProtocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialect();
                    addDefaultValuesIfNecessary(deviceProtocolDialect, result);

                    return TypedPropertiesValueAdapter.adaptToUPLValues(device, result);
                }
            }
        }
        return null;
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     */
    private void addDefaultValuesIfNecessary(DeviceProtocolDialect theActualDialect, com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        for (com.energyict.mdc.upl.properties.PropertySpec propertySpec : theActualDialect.getUPLPropertySpecs()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
    }

    /**
     * Gets the {@link SecurityPropertySet} that needs to be used when
     * the Device is communicating to the ComServer
     * via the specified {@link InboundConnectionTask}.
     *
     * @param device The Device
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
                if (comTaskEnablement.getComTask().equals(first.getComTask())) {
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
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier identifier, InboundComPort inboundComPort) {
        Device device = this.findDevice(identifier);
        ConnectionTask<?, ?> connectionTask = this.getInboundConnectionTask(inboundComPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            TypedProperties typedProperties = connectionTask.getTypedProperties();
            return TypedPropertiesValueAdapter.adaptToUPLValues(device, typedProperties);
        }
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        Device device = findDevice(deviceIdentifier);

        List<OutboundConnectionTask> outboundConnectionTasks = device.getConnectionTasks().stream()
                .filter(connectionTask -> connectionTask instanceof OutboundConnectionTask)
                .map(connectionTask1 -> ((OutboundConnectionTask) connectionTask1))
                .collect(Collectors.toList());
        Optional<OutboundConnectionTask> defaultConnectionTask = outboundConnectionTasks.stream().filter(OutboundConnectionTask::isDefault).findAny();

        TypedProperties result = TypedProperties.empty();
        if (defaultConnectionTask.isPresent()) {
            result = defaultConnectionTask.get().getTypedProperties();
        } else if (!outboundConnectionTasks.isEmpty()) {
            result = outboundConnectionTasks.get(0).getTypedProperties();
        }
        return TypedPropertiesValueAdapter.adaptToUPLValues(device, result);
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolProperties(DeviceIdentifier identifier) {
        try {
            Optional<Device> device = this.getDeviceFor(identifier);
            TypedProperties typedProperties = device.map(Device::getDeviceProtocolProperties).orElse(null);
            if (typedProperties != null) {
                return TypedPropertiesValueAdapter.adaptToUPLValues(device.get(), typedProperties);
            } else {
                return null;
            }
        } catch (CanNotFindForIdentifier e) {
            return null;
        }
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
        Optional<Device> device = getDeviceFor(deviceIdentifier);
        if (device.isPresent()) {
            TypedProperties localProperties = TypedProperties.empty();

            TypedProperties deviceProtocolProperties = device.get().getDeviceProtocolProperties();
            deviceProtocolProperties
                    .localPropertyNames()
                    .stream()
                    .forEach(localName -> localProperties.setProperty(localName, deviceProtocolProperties.getProperty(localName)));

            return TypedPropertiesValueAdapter.adaptToUPLValues(device.get(), localProperties);
        }
        return null;
    }

    @Override
    public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
        return new OfflineDeviceImpl(findDevice(deviceIdentifier), context, new OfflineDeviceServiceProvider());
    }

    @Override
    public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = findDevice(identifier).getDeviceProtocolPluggableClass();
        if (deviceProtocolPluggableClass.isPresent()) {
            return deviceProtocolPluggableClass.get().getJavaClassName();
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        LoadProfile loadProfile = this.findLoadProfileOrThrowException(loadProfileIdentifier);
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice((loadProfile).getDevice());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        LogBook logBook = this.findLogBookOrThrowException(logBookIdentifier);
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(logBook.getDevice());
    }

    @Override
    public Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier) {
        return serviceProvider.deviceService().findDeviceByIdentifier(deviceIdentifier);
    }

    @Override
    public List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier) {
        return serviceProvider.deviceService().findAllDevicesByIdentifier(deviceIdentifier);
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        LogBook logBook = this.findLogBookOrThrowException(logBookIdentifier);
        // Refresh device and LogBook to avoid OptimisticLockException
        Device device = this.serviceProvider.deviceService().findDeviceById(logBook.getDevice().getId()).get();
        LogBook refreshedLogBook = device.getLogBooks().stream().filter(each -> each.getId() == logBook.getId()).findAny().get();
        LogBook.LogBookUpdater logBookUpdater = device.getLogBookUpdaterFor(refreshedLogBook);
        logBookUpdater.setLastLogBookIfLater(lastLogBook);
        logBookUpdater.setLastReadingIfLater(lastLogBook); // We assume the event will be persisted with a time difference of only a few milliseconds
        logBookUpdater.update();
    }

    @Override
    public void storePathSegments(List<TopologyPathSegment> topologyPathSegments) {
        TopologyService.G3CommunicationPathSegmentBuilder g3CommunicationPathSegmentBuilder = serviceProvider.topologyService()
                .addCommunicationSegments();
        topologyPathSegments.forEach(topologyPathSegment -> {
            Optional<Device> source = getOptionalDeviceByIdentifier(topologyPathSegment.getSource());
            Optional<Device> target = getOptionalDeviceByIdentifier(topologyPathSegment.getTarget());
            Optional<Device> intermediateHop = getOptionalDeviceByIdentifier(topologyPathSegment.getIntermediateHop());
            if (target.isPresent() && intermediateHop.isPresent()) {
                g3CommunicationPathSegmentBuilder.add(
                        source.get(),
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
                TopologyService.G3NeighborBuilder g3NeighborBuilder = g3NeighborhoodBuilder.addNeighbor(
                        optionalDevice.get(), ModulationScheme.fromId(topologyNeighbour.getModulationSchema()),
                        Modulation.fromId(topologyNeighbour.getModulation()),
                        PhaseInfo.fromId(topologyNeighbour.getPhaseDifferential()),
                        G3NodeState.fromId(topologyNeighbour.getState())
                );
                g3NeighborBuilder.linkQualityIndicator(topologyNeighbour.getLqi());
                g3NeighborBuilder.timeToLiveSeconds(topologyNeighbour.getNeighbourValidTime());
                g3NeighborBuilder.toneMap(topologyNeighbour.getToneMap());
                g3NeighborBuilder.toneMapTimeToLiveSeconds(topologyNeighbour.getTmrValidTime());
                g3NeighborBuilder.txCoefficient(topologyNeighbour.getTxCoeff());
                g3NeighborBuilder.txGain(topologyNeighbour.getTxGain());
                g3NeighborBuilder.txResolution(topologyNeighbour.getTxRes());
                g3NeighborBuilder.macPANId(topologyNeighbour.getMacPANId());
                g3NeighborBuilder.nodeAddress(topologyNeighbour.getNodeAddress());
                g3NeighborBuilder.shortAddress(topologyNeighbour.getShortAddress());
                g3NeighborBuilder.lastUpdate(topologyNeighbour.getLastUpdate());
                g3NeighborBuilder.lastPathRequest(topologyNeighbour.getLastPathRequest());
                g3NeighborBuilder.roundTrip(topologyNeighbour.getRoundTrip());
                g3NeighborBuilder.linkCost(topologyNeighbour.getLinkCost());
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
                .orElseThrow(() -> CanNotFindForIdentifier.device(identifier, MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER));
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
            firmwareStorage.updateCaConfigImageVersion(collectedFirmwareVersions.getActiveCaConfigImageVersion(), device);
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
    public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(deviceIdentifier);
        optionalDevice.ifPresent(device -> {
            DeviceCertificateStorage certificateStorage = new DeviceCertificateStorage(getSecurityManagementService());
            certificateStorage.updateDeviceCSR(device, certificateType, csr);
        });
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        LoadProfile loadProfile = this.findLoadProfileOrThrowException(loadProfileIdentifier);
        // Refresh the device and the LoadProfile to avoid OptimisticLockException
        Device device = this.serviceProvider.deviceService().findDeviceById(loadProfile.getDevice().getId()).get();
        LoadProfile refreshedLoadProfile = device.getLoadProfiles().stream().filter(each -> each.getId() == loadProfile.getId()).findAny().get();
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(refreshedLoadProfile);
        loadProfileUpdater.setLastReadingIfLater(lastReading);
        loadProfileUpdater.update();
    }

    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> loadProfileUpdate, Map<LogBookIdentifier, Instant> logBookUpdate) {
        Map<DeviceIdentifier, List<Function<Device, Void>>> updateMap = new HashMap<>();

        // first do the loadprofiles
        loadProfileUpdate.entrySet().stream().forEach(entrySet -> {
            DeviceIdentifier deviceIdentifier = entrySet.getKey().getDeviceIdentifier();
            List<Function<Device, Void>> functionList = updateMap.get(deviceIdentifier);
            if (functionList == null) {
                functionList = new ArrayList<>();
                updateMap.put(deviceIdentifier, functionList);
            }

            functionList.add(updateLoadProfile(findLoadProfileOrThrowException(entrySet.getKey()), entrySet.getValue()));
        });
        // then do the logbooks
        logBookUpdate.entrySet().stream().forEach(entrySet -> {
            DeviceIdentifier deviceIdentifier = entrySet.getKey().getDeviceIdentifier();
            List<Function<Device, Void>> functionList = updateMap.get(deviceIdentifier);
            if (functionList == null) {
                functionList = new ArrayList<>();
                updateMap.put(deviceIdentifier, functionList);
            }
            functionList.add(updateLogBook(findLogBookOrThrowException(entrySet.getKey()), entrySet.getValue()));
        });

        // then do your thing
        updateMap.entrySet().stream().forEach(entrySet -> {
            Device oldDevice = findDevice(entrySet.getKey());
            Device device = this.serviceProvider.deviceService().findDeviceById(oldDevice.getId()).get();
            entrySet.getValue().stream().forEach(deviceVoidFunction -> deviceVoidFunction.apply(device));
        });
    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Optional<Device> device = getDeviceFor(deviceIdentifier);
        if (device.isPresent()) {
            InboundConnectionTask connectionTask = this.getInboundConnectionTask(inboundComPort, device.get());
            if (connectionTask != null) {
                ComTaskExecution first = this.getFirstComTaskExecution(connectionTask);
                if (first != null) {
                    return first.isOnHold();
                }
            }
        }
        return null;
    }

    private Function<Device, Void> updateLoadProfile(LoadProfile loadProfile, Instant lastReading) {
        return device -> {
            LoadProfile refreshedLoadProfile = device.getLoadProfiles().stream().filter(each -> each.getId() == loadProfile.getId()).findAny().get();
            LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(refreshedLoadProfile);
            loadProfileUpdater.setLastReadingIfLater(lastReading);
            loadProfileUpdater.update();
            return null;
        };
    }

    private Function<Device, Void> updateLogBook(LogBook logBook, Instant lastLogBook) {
        return device -> {
            LogBook refreshedLogBook = device.getLogBooks().stream().filter(each -> each.getId() == logBook.getId()).findAny().get();
            LogBook.LogBookUpdater logBookUpdater = device.getLogBookUpdaterFor(refreshedLogBook);
            logBookUpdater.setLastReadingIfLater(lastLogBook);
            logBookUpdater.setLastLogBookIfLater(lastLogBook);
            logBookUpdater.update();
            return null;
        };
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
        final Device masterDevice = this.getDeviceFor(offlineLoadProfile.getDeviceIdentifier()).get();
        if (masterDevice != null && storageOnSlaveDevice(masterDevice)) {
            Optional<Channel> masterDeviceChannel = masterDevice.getChannels().stream().filter((c) -> c.getReadingType().getMRID().equals(readingTypeMRID)).findFirst();
            if (masterDeviceChannel.isPresent()) {
                List<DataLoggerChannelUsage> dataLoggerChannelUsages = this.serviceProvider.topologyService().findDataLoggerChannelUsagesForChannels(masterDeviceChannel.get(), dataPeriod);
                List<Pair<OfflineLoadProfile, Range<Instant>>> linkedOffLineLoadProfiles = new ArrayList<>();
                // 'linked' periods
                if (!dataLoggerChannelUsages.isEmpty()) {
                    dataLoggerChannelUsages.forEach(usage -> {
                        Device slave = usage.getPhysicalGatewayReference().getOrigin();
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
        return Collections.singletonList(Pair.of(offlineLoadProfile, dataPeriod));
    }

    @Override
    public User getComServerUser() {
        return comServerUser;
    }

    private Register getStorageRegister(Register register, Instant readingDate) {
        final Device masterDevice = register.getDevice();
        if (masterDevice != null && storageOnSlaveDevice(masterDevice)) {
            Optional<Register> slaveRegister = this.serviceProvider.topologyService().getSlaveRegister(register, readingDate);
            if (slaveRegister.isPresent()) {
                return slaveRegister.get();
            }
        }
        return register;
    }

    private boolean storageOnSlaveDevice(Device masterDevice) {
        return (masterDevice.getDeviceConfiguration().isDataloggerEnabled() || masterDevice.getDeviceConfiguration().isMultiElementEnabled());
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

        ProtocolPluggableService protocolPluggableService();

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

        FirmwareService firmwareService();

        DeviceConfigurationService deviceConfigurationService();

        SecurityManagementService securityManagementService();
    }

    private class OfflineDeviceServiceProvider implements OfflineDeviceImpl.ServiceProvider {

        @Override
        public Thesaurus thesaurus() {
            return serviceProvider.thesaurus();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
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

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

    }

}