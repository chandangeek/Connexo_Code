/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.TypedPropertiesValueAdapter;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.G3NodeState;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.offline.DeviceOffline;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceMessageImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineLogBookImpl;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.commands.store.PreStoreLoadProfile;
import com.energyict.mdc.engine.impl.commands.store.PreStoreLogBook;
import com.energyict.mdc.engine.impl.core.AdaptiveQueryTuner;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComJobFactory;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.mdc.engine.impl.core.FixedQueryTuner;
import com.energyict.mdc.engine.impl.core.MultiThreadedComJobFactory;
import com.energyict.mdc.engine.impl.core.QueryTuner;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.SingleThreadedComJobFactory;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.security.Privileges;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
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
import sun.security.x509.X509CertImpl;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(ComServerDAOImpl.class.getName());
    private final ServiceProvider serviceProvider;
    private final User comServerUser;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;
    private Map<Long, QueryTuner> pendingQueryLimitCalculators = new HashMap<>();


    public ComServerDAOImpl(ServiceProvider serviceProvider, User comServerUser) {
        this.serviceProvider = serviceProvider;
        this.comServerUser = comServerUser;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private EngineConfigurationService getEngineModelService() {
        return serviceProvider.engineConfigurationService();
    }

    private DeviceService getDeviceService() {
        return serviceProvider.deviceService();
    }

    private SecurityManagementService getSecurityManagementService() {
        return serviceProvider.securityManagementService();
    }

    private CommunicationTaskService getCommunicationTaskService() {
        return serviceProvider.communicationTaskService();
    }

    private PriorityComTaskService getPriorityComTaskService() {
        return serviceProvider.priorityComTaskService();
    }

    private ConnectionTaskService getConnectionTaskService() {
        return serviceProvider.connectionTaskService();
    }

    private Clock getClock() {
        return serviceProvider.clock();
    }

    private TransactionService getTransactionService() {
        return serviceProvider.transactionService();
    }

    @Override
    public ServerProcessStatus getStatus() {
        return status;
    }

    @Override
    public void start() {
        status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown() {
        status = ServerProcessStatus.SHUTDOWN;
    }

    @Override
    public void shutdownImmediate() {
        shutdown();
    }

    @Override
    public ComServer getThisComServer() {
        return getEngineModelService().findComServerBySystemName().orElse(null);
    }

    @Override
    public ComServer getComServer(String systemName) {
        return getEngineModelService().findComServer(systemName).orElse(null);
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

    public List<OfflineUserInfo> getUsersCredentialInformation() {
        List<OfflineUserInfo> userInfos = new ArrayList<OfflineUserInfo>();
        String defaultDomain = serviceProvider.userService().findDefaultUserDirectory().getDomain();
        for (User user : serviceProvider.userService().getAllUsers()) {
            boolean canUseComServerMobile = user.getPrivileges().stream().anyMatch(p -> p.getName().equals(Privileges.OPERATE_MOBILE_COMSERVER.getKey()));
            userInfos.add(new OfflineUserInfo(user, canUseComServerMobile, defaultDomain.equals(user.getDomain())));
        }
        return userInfos;
    }

    public Optional<OfflineUserInfo> checkAuthentication(String loginPassword) {
        return serviceProvider.userService().authenticateBase64(loginPassword)
                .filter(user -> user.getPrivileges().stream().anyMatch(p -> p.getName().equals(Privileges.OPERATE_MOBILE_COMSERVER.getKey())))
                .map(user -> new OfflineUserInfo(user, true,
                        serviceProvider.userService().findDefaultUserDirectory().getDomain().equals(user.getDomain())));
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        Optional<? extends ComPort> reloaded = getEngineModelService().findComPort(comPort.getId());
        if (!reloaded.isPresent() || reloaded.get().isObsolete()) {
            return null;
        } else if (reloaded.get().getModTime().isAfter(comPort.getModTime())) {
            return reloaded.get();
        } else {
            return comPort;
        }
    }

    private Optional<ConnectionTask> refreshConnectionTask(ConnectionTask connectionTask) {
        Optional<ConnectionTask> reloaded = getConnectionTaskService().findConnectionTask(connectionTask.getId());
        return reloaded.filter(Predicates.not(ConnectionTask::isObsolete));
    }

    private Optional<ConnectionTask> lockConnectionTask(ConnectionTask connectionTask) {
        ConnectionTask reloaded = getConnectionTaskService().attemptLockConnectionTask(connectionTask.getId());
        return Optional.ofNullable(reloaded);
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
    public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
        long start = System.currentTimeMillis();
        List<ComTaskExecution> comTaskExecutions = getCommunicationTaskService().getPendingComTaskExecutionsListFor(comPort, 0);
        long fetchComTaskDuration = System.currentTimeMillis() - start;
        ComJobFactory comJobFactoryFor = getComJobFactoryFor(comPort);
        start = System.currentTimeMillis();
        List<ComJob> comJobs = comJobFactoryFor.collect(comTaskExecutions.iterator());
        long addToGroupDuration = System.currentTimeMillis() - start;
        LOGGER.warning("perf - fetchComTaskDuration=" + fetchComTaskDuration + ", addToGroupDuration=" + addToGroupDuration);
        return comJobs;
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        QueryTuner adaptiveLimitCalculator = getAdaptiveLimitCalculator(comPort);
        long start = System.currentTimeMillis();
        List<ComTaskExecution> comTaskExecutions = getCommunicationTaskService().getPendingComTaskExecutionsListFor(comPort, adaptiveLimitCalculator.getTuningFactor());
        long fetchComTaskDuration = System.currentTimeMillis() - start;
        ComJobFactory comJobFactoryFor = getComJobFactoryFor(comPort);
        start = System.currentTimeMillis();
        List<ComJob> comJobs = comJobFactoryFor.consume(comTaskExecutions.iterator());
        long addToGroupDuration = System.currentTimeMillis() - start;
        LOGGER.warning("perf - fetchComTaskDuration=" + fetchComTaskDuration + ", addToGroupDuration=" + addToGroupDuration);

        adaptiveLimitCalculator.calculateFactor(fetchComTaskDuration, comJobs.size(), comPort.getNumberOfSimultaneousConnections());

        return comJobs;
    }

    private QueryTuner getAdaptiveLimitCalculator(OutboundComPort comPort) {
        QueryTuner pendingQueryLimit = pendingQueryLimitCalculators.get(comPort.getId());

        if (pendingQueryLimit == null) {
            if (serviceProvider.engineService().isAdaptiveQuery()) {
                pendingQueryLimit = new AdaptiveQueryTuner();
            } else {
                pendingQueryLimit = new FixedQueryTuner();
            }
            pendingQueryLimitCalculators.put(comPort.getId(), pendingQueryLimit);
        }

        return pendingQueryLimit;
    }

    @Override
    public List<ComTaskExecution> findExecutableOutboundComTasks(ComServer comServer, Duration delta, long limit, long skip) {
        List<OutboundComPortPool> outboundComPortPools =
                getEngineModelService().findContainingComPortPoolsForComServer(comServer)
                        .stream().filter(ComPortPool::isActive)
                        .filter(comPortPool -> !comPortPool.isInbound())
                        .map(OutboundComPortPool.class::cast)
                        .collect(Collectors.toList());
        return getCommunicationTaskService().getPendingComTaskExecutionsListFor(comServer, outboundComPortPools, delta, limit, skip);
    }


    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
        return findExecutableHighPriorityOutboundComTasks(comServer, currentHighPriorityLoadPerComPortPool, Instant.now());
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
        return getPriorityComTaskService().findExecutable(comServer, currentHighPriorityLoadPerComPortPool, date);
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
        List<ConnectionTaskProperty> connectionTaskProperties = serviceProvider.transactionService().execute(connectionTask::getProperties);
        return adaptToUPLValues(connectionTaskProperties);
    }

    @Override
    public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId) {
        Device device = this.findDevice(deviceIdentifier);
        for (ComTaskEnablement comTaskEnablement : enabledComTasks(device.getDeviceConfiguration())) {
            if (comTaskEnablement.getComTask().getId() == comTaskId) {
                return comTaskEnablement;
            }
        }
        return null;
    }

    @Override
    public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
        Device device = this.findDevice(deviceIdentifier);
        List<SecurityPropertySet> allSecurityPropertySet = new ArrayList<SecurityPropertySet>();
        for (ComTaskEnablement comTaskEnablement : enabledComTasks(device.getDeviceConfiguration())) {
            allSecurityPropertySet.add(comTaskEnablement.getSecurityPropertySet());
        }
        return allSecurityPropertySet;
    }

    @Override
    public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
        Optional<ComTaskExecution> comTaskExecution = getCommunicationTaskService().findComTaskExecution(comTaskExecutionId);
        if (comTaskExecution.isPresent()) {
            Optional<ConnectionTask<?, ?>> connectionTask = comTaskExecution.get().getConnectionTask();
            if (connectionTask.isPresent()) {
                Device masterDevice = connectionTask.get().getDevice();
                ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = connectionTask.get().getProtocolDialectConfigurationProperties();
                if (protocolDialectConfigurationProperties != null) {
                    String dialectName = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
                    //Use the master device, this one holds the actual dialect properties
                    Optional<ProtocolDialectProperties> protocolDialectProperties = masterDevice.getProtocolDialectProperties(dialectName);
                    if (protocolDialectProperties.isPresent()) {
                        return protocolDialectProperties.get().getTypedProperties();
                    }
                }
            }
        }
        return null;
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
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, final ComPort comPort) {
        try {
            return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comPort);
        } catch (OptimisticLockException e) {
            Optional reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                return getConnectionTaskService().attemptLockConnectionTask((ScheduledConnectionTask) reloaded.get(), comPort);
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
        Optional<Register> register = findRegister(identifier);
        return register.map(register1 -> new OfflineRegisterImpl(getStorageRegister(register1, when), serviceProvider.identificationService()));
    }

    private Optional<Register> findRegister(RegisterIdentifier identifier) {
        return serviceProvider
                .registerService()
                .findByIdentifier(identifier);
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        Optional<LoadProfile> loadProfile = findLoadProfile(loadProfileIdentifier);
        return loadProfile.map(loadProfile1 -> new OfflineLoadProfileImpl(loadProfile1, serviceProvider.topologyService(), serviceProvider.identificationService()));
    }

    private Optional<LoadProfile> findLoadProfile(LoadProfileIdentifier identifier) {
        return serviceProvider
                .loadProfileService()
                .findByIdentifier(identifier);
    }

    private LoadProfile findLoadProfileOrThrowException(LoadProfileIdentifier identifier) {
        return findLoadProfile(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LoadProfile with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        Optional<LogBook> logBook = findLogBook(logBookIdentifier);
        return logBook.map(logBook1 -> new OfflineLogBookImpl(logBook1, serviceProvider.identificationService()));
    }

    private Optional<LogBook> findLogBook(LogBookIdentifier identifier) {
        return serviceProvider
                .logBookService()
                .findByIdentifier(identifier);
    }

    private LogBook findLogBookOrThrowException(LogBookIdentifier identifier) {
        return findLogBook(identifier)
                .orElseThrow(() -> new IllegalArgumentException("LogBook with identifier " + identifier.toString() + " does not exist"));
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        Optional<DeviceMessage> optionalDeviceMessage = findDeviceMessage(identifier);
        if (optionalDeviceMessage.isPresent()) {
            DeviceMessage deviceMessage = optionalDeviceMessage.get();
            Device device = (Device) deviceMessage.getDevice(); //Downcast to the Connexo Device
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = device.getDeviceType().getDeviceProtocolPluggableClass();
            if (deviceProtocolPluggableClass.isPresent()) {
                return Optional.of(
                        new OfflineDeviceMessageImpl(
                                deviceMessage,
                                deviceProtocolPluggableClass.get().getDeviceProtocol(),
                                serviceProvider.identificationService(),
                                serviceProvider.protocolPluggableService(),
                                serviceProvider.deviceMessageSpecificationService(),
                                new OfflineDeviceImpl(device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider())
                        )
                );
            }
        }
        return Optional.empty();
    }

    private Optional<DeviceMessage> findDeviceMessage(MessageIdentifier identifier) {
        return serviceProvider
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

        Device device = findDevice(deviceIdentifier);
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
        if (propertyValue instanceof CollectedCertificateWrapper) {
            // If the property value is a CollectedCertificateWrapper then add the certificate in the trust store.
            this.addTrustedCertificates(Collections.singletonList((CollectedCertificateWrapper) propertyValue));
            throw new UnsupportedOperationException("Not supported to automatically update the security accessor value models a certificate, but the certificate was saved in following trust store " + ((CollectedCertificateWrapper) propertyValue)
                    .getTrustStoreName());
        }
    }

    @Override
    public void addTrustedCertificates(final List<CollectedCertificateWrapper> collectedCertificates) {
        if (!collectedCertificates.isEmpty()) {
            //create and map a new trust store for each distinct truststore name associated with provided CollectedCertificateWrapper list
            Map<String, TrustStore> trustStoreMapByName = collectedCertificates
                    .stream()
                    .map(CollectedCertificateWrapper::getTrustStoreName)
                    .distinct()
                    .collect(Collectors.toMap(Object::toString, this::getTrustStoreByNameOrElseThrow));

            //store certificates to the appropriated truststore
            collectedCertificates
                    .forEach(collectedCertificateWrapper -> trustStoreMapByName
                            .get(collectedCertificateWrapper.getTrustStoreName())
                            .addCertificate(collectedCertificateWrapper.getAlias(),
                                    getX509CertificateFromCollectedCertificateWrapper(collectedCertificateWrapper)));
        }
    }

    private TrustStore getTrustStoreByNameOrElseThrow(String trustStoreName) {
        return getSecurityManagementService().findTrustStore(trustStoreName)
                .orElseThrow(() -> new NotFoundException("Unable to find trust store with name: " + trustStoreName));
    }

    private X509Certificate getX509CertificateFromCollectedCertificateWrapper(CollectedCertificateWrapper collectedCertificateWrapper) {
        try {
            return new X509CertImpl(DatatypeConverter.parseBase64Binary(collectedCertificateWrapper.getBase64Certificate()));
        } catch (CertificateException e) {
            throw new UnsupportedOperationException("Failed to create a X509Certificate from the received base64certificate");
        }
    }

    @Override
    @Deprecated
    //TODO: remove this when doing next major increase
    public void addCACertificate(final CertificateWrapper certificateWrapper) {
        executeTransaction(() -> {
            doAddCACertificate(certificateWrapper);
            return null;
        });
    }

    private void doAddCACertificate(CertificateWrapper certificateWrapper) {
        //TODO create CertificateWrapper entry
    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return executeTransaction(() -> doAddEndDeviceCertificate(collectedCertificateWrapper));
    }

    private long doAddEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        // Todo: wait for PKI feature (CXO-3603) to be implemented and merged into this branch
        throw new UnsupportedOperationException("Waiting for implementation of the PKI feature (CXO-3603)");
    }

    /**
     * This method should be used when trying to set the actual security accessor value and we don't have a passive key generated. This could be the case during key agreement
     *
     * @param deviceIdentifier
     * @param propertyName the name of the security accessor
     * @param propertyValue the new label and key in hex format separated by a colon e.g. 574B2D44422D30312D544553542D5048415345322D32303137:800202300D020012301A0D021220041420586F8048EB8683B5EA8A51BD8317CEF38C50E7AA28A4260D0224580420FA9829FF5E6D2AC488DE3249714E7CE9CC18DA04FB4099910E0C7CB7AC76ECC1
     */
    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        String newKey = (String) propertyValue;
        Device device = findDevice(deviceIdentifier);

        device.getDeviceType().getSecurityAccessors().stream()
                .filter(securityAccessorTypeOnDeviceType -> securityAccessorTypeOnDeviceType.getSecurityAccessorType().getName().equals(propertyName))
                .findFirst()
                .map(securityAccessorTypeOnDeviceType -> securityAccessorTypeOnDeviceType.getSecurityAccessorType())
                .map(securityAccessorType -> device.getSecurityAccessor(securityAccessorType).orElseGet(() -> device.newSecurityAccessor(securityAccessorType)))
                .ifPresent(securityAccessor -> {
                    HsmKey hsmKey = (HsmKey) getServiceProvider().securityManagementService().newSymmetricKeyWrapper(securityAccessor.getKeyAccessorTypeReference());
                    byte[] key = DatatypeConverter.parseHexBinary(newKey.split(":")[1]);
                    String label = new String(DatatypeConverter.parseHexBinary(newKey.split(":")[0]));
                    hsmKey.setKey(key, label);
                    securityAccessor.setActualPassphraseWrapperReference(hsmKey);
                    securityAccessor.save();
                });
    }

    /**
     * This method should be used when trying to renew the actual security accessor value and we don't have a passive key generated. This could be the case during key agreement
     *
     * @param deviceIdentifier
     * @param propertyName
     * @param propertyValue
     * @param comTaskExecution
     */
    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution) {
        handleCertificatePropertyValue(propertyValue);
        SecurityPropertySet securityPropertySet = getSecurityPropertySet(deviceIdentifier, comTaskExecution);
        SecurityAccessor securityAccessor = getSecurityAccessor(deviceIdentifier, propertyName, securityPropertySet);
        updateDeviceSecurityAccessor(securityAccessor, propertyValue);
    }

    /**
     * This method should be used when we try to renew the actual security accessor value and we already have a passive key generated
     *
     * @param deviceIdentifier
     * @param propertyName
     * @param comTaskExecution
     */
    @Override
    public void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution) {
        SecurityPropertySet securityPropertySet = getSecurityPropertySet(deviceIdentifier, comTaskExecution);
        SecurityAccessor securityAccessor = getSecurityAccessor(deviceIdentifier, propertyName, securityPropertySet);
        swapTempAndActualKeys(securityAccessor);
    }

    private void updateDeviceSecurityAccessor(SecurityAccessor securityAccessor, Object propertyValue) {

        this.executeTransaction(() -> {
            if (securityAccessor.getActualPassphraseWrapperReference().isPresent()) {
                Object actualValue = securityAccessor.getActualPassphraseWrapperReference().get();
                if (actualValue instanceof PlaintextSymmetricKey) {
                    if (((PlaintextSymmetricKey) actualValue).getKey().isPresent()) { //we need an actual key to be present in order to know what algorithm to choose for the the new key
                        SymmetricKeyWrapper symmetricKeyWrapper = getSecurityManagementService().newSymmetricKeyWrapper(securityAccessor.getKeyAccessorTypeReference());
                        ((PlaintextSymmetricKey) symmetricKeyWrapper).setKey(new SecretKeySpec(DatatypeConverter.parseHexBinary(String.valueOf(propertyValue)), ((PlaintextSymmetricKey) actualValue).getKey()
                                .get()
                                .getAlgorithm()));
                        updateSecurityAccessorActualKeyValue(securityAccessor, symmetricKeyWrapper);

                    }
                } else if (actualValue instanceof PlaintextPassphrase) {
                    PlaintextPassphrase plaintextPassphrase = (PlaintextPassphrase) actualValue;
                    plaintextPassphrase.setEncryptedPassphrase((String) propertyValue);
                    securityAccessor.clearTempValue();
                    securityAccessor.save();
                } else if (actualValue instanceof com.elster.jupiter.pki.CertificateWrapper) {
                    //TODO: see what should we do in case of certificates...
                    throw new UnsupportedOperationException("Not supported to automatically update the security accessor that models a certificate ");
                } else if (actualValue instanceof HsmKey) {
                    HsmKey hsmKey = (HsmKey) getSecurityManagementService().newSymmetricKeyWrapper(securityAccessor.getKeyAccessorTypeReference());
                    hsmKey.setKey(DatatypeConverter.parseHexBinary(String.valueOf(propertyValue)), ((HsmKey) actualValue).getLabel());
                    updateSecurityAccessorActualKeyValue(securityAccessor, hsmKey);
                }
            } else {
                throw new UnsupportedOperationException("Not allowed to update the key without having a passive key available. Automatic key renewal can be done only using the 'generate value' approach");
            }
            return null;
        });
    }

    private void updateSecurityAccessorActualKeyValue(SecurityAccessor securityAccessor, SymmetricKeyWrapper symmetricKeyWrapper) {
        securityAccessor.setActualPassphraseWrapperReference(symmetricKeyWrapper);
        securityAccessor.clearTempValue();
        securityAccessor.save();
    }

    private void swapTempAndActualKeys(SecurityAccessor securityAccessor) {
        if (securityAccessor.getActualPassphraseWrapperReference().isPresent() && securityAccessor.getTempValue().isPresent()) {
            securityAccessor.swapValues();
            securityAccessor.clearTempValue();
            securityAccessor.save();
        }
    }

    private SecurityAccessor getSecurityAccessor(DeviceIdentifier deviceIdentifier, String propertyName, SecurityPropertySet securityPropertySet) {
        Device device = this.findDevice(deviceIdentifier);
        ConfigurationSecurityProperty configurationSecurityProperty = securityPropertySet.getConfigurationSecurityProperties()
                .stream()
                .filter(securityProperty -> securityProperty.getName().equals(propertyName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unable to find " + propertyName + " property"));
        SecurityAccessorType securityAccessorType = configurationSecurityProperty.getSecurityAccessorType();
        return device.getSecurityAccessorByName(securityAccessorType.getName())
                .orElseThrow(() -> new NotFoundException("Unable to find security accessor with name: " + securityAccessorType.getName()));
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        Device device = findDevice(deviceIdentifier);
        Optional<Device> gatewayDevice;
        if (gatewayDeviceIdentifier != null) {
            gatewayDevice = getDeviceFor(gatewayDeviceIdentifier);
        } else {
            gatewayDevice = Optional.empty();
        }
        if (gatewayDevice.isPresent()) {
            serviceProvider.topologyService().setPhysicalGateway(device, gatewayDevice.get());
        } else {
            serviceProvider.topologyService().clearPhysicalGateway(device);
        }
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier identifier, final DateTimeFormatter timeStampFormat, final String fileName, final String fileExtension, final byte[] contents) {
        Device device = findDevice(identifier);
        doStoreConfigurationFile(device, timeStampFormat, fileName, fileExtension, contents);
    }

    private void doStoreConfigurationFile(Device device, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
        LocalDateTime localDateTime = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();
        device.getDeviceConfiguration().getDeviceType().addDeviceMessageFile(new ByteArrayInputStream(contents), fileName + "_" + localDateTime.format(timeStampFormat) + "." + fileExtension);
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
        return getConnectionTaskService().attemptLockConnectionTask(connectionTask, comPort) != null;
    }

    public void unlock(final OutboundConnectionTask connectionTask) {
        unlock((ConnectionTask) connectionTask);
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
    public boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort) {
        return getPriorityComTaskService().attemptLockComTaskExecution(comTaskExecution, comPort) != null;
    }

    @Override
    public void unlock(final ComTaskExecution comTaskExecution) {
        getCommunicationTaskService().unlockComTaskExecution(comTaskExecution);
    }

    @Override
    public ConnectionTask<?, ?> executionStarted(final ConnectionTask connectionTask, final ComPort comPort) {
        return executeTransaction(() -> {
            Optional<ConnectionTask> lockedConnectionTask = lockConnectionTask(connectionTask);
            if (lockedConnectionTask.isPresent()) {
                ConnectionTask connectionTask1 = lockedConnectionTask.get();
                connectionTask1.executionStarted(comPort);
                return connectionTask1;
            }
            return null;
        });
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(final ConnectionTask connectionTask) {
        ConnectionTask updatedConnectionTask = null;
        try {
            if (connectionTask instanceof InboundConnectionTask) {
                Optional<ConnectionTask> lockedConnectionTask = lockConnectionTask(connectionTask);
                if (lockedConnectionTask.isPresent()) {
                    ConnectionTask task = lockedConnectionTask.get();
                    task.executionCompleted();
                    updatedConnectionTask = task;
                } else {
                    throw new IllegalStateException("Can't find inbound connection task");
                }
            } else {
                connectionTask.executionCompleted();
                updatedConnectionTask = connectionTask;
            }
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
            if (connectionTask instanceof InboundConnectionTask) {
                Optional<ConnectionTask> lockedConnectionTask = lockConnectionTask(connectionTask);
                if (lockedConnectionTask.isPresent()) {
                    ConnectionTask task = lockedConnectionTask.get();
                    task.executionFailed();
                    updatedConnectionTask = task;
                } else {
                    throw new IllegalStateException("Can't find inbound connection task");
                }
            } else {
                connectionTask.executionFailed();
                updatedConnectionTask = connectionTask;
            }
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
    public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
        ConnectionTask updatedConnectionTask = null;
        try {
            connectionTask.executionRescheduled();
            updatedConnectionTask = connectionTask;
        } catch (OptimisticLockException e) {
            final Optional<ConnectionTask> reloaded = refreshConnectionTask(connectionTask);
            if (reloaded.isPresent()) {
                updatedConnectionTask = reloaded.get();
                updatedConnectionTask.executionRescheduled();
            }
        }
        return updatedConnectionTask;
    }

    @Override
    public void executionStarted(final ComTaskExecution comTaskExecution, final ComPort comPort, boolean executeInTransaction) {
        if (executeInTransaction) {
            executeTransaction(() -> {
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

    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        getCommunicationTaskService().executionRescheduledToComWindow(comTaskExecution, comWindowStartDate);
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
    public void releaseInterruptedTasks(final ComPort comPort) {
        LOGGER.warning("Start unlocking BUSY comTasks on comPort " + comPort);
        executeTransaction(() -> {
            getCommunicationTaskService().releaseInterruptedComTasks(comPort);
            LOGGER.info("Unlocked BUSY comTasks on comPort " + comPort);
            getConnectionTaskService().releaseInterruptedConnectionTasks(comPort);
            LOGGER.info("Unlocked BUSY connectionTasks on comPort " + comPort);
            return null;
        });
    }

    @Override
    public TimeDuration releaseTimedOutTasks(final ComPort comPort) {
        LOGGER.warning("Start unlocking timed out comTasks on comPort '" + comPort + "'");
        List<ComTaskExecution> timedOutComTasks = getCommunicationTaskService().findTimedOutComTasksByComPort(comPort);
        LOGGER.warning("Found " + timedOutComTasks.size() + " comtasks to be unblocked");
        long unlockedCount = unlockComTasks(timedOutComTasks);
        LOGGER.warning("Unlocked " + unlockedCount + " out of " + timedOutComTasks.size() + " timed out comTasks on comPort '" + comPort + "'");
        List<ConnectionTask> timedOutConnectionTasks = getConnectionTaskService().findTimedOutConnectionTasksByComPort(comPort);
        LOGGER.warning("Found " + timedOutConnectionTasks.size() + " connections to be unblocked");
        unlockedCount = unlockConnectionTasks(timedOutConnectionTasks);
        LOGGER.warning("Unlocked " + unlockedCount + " out of " + timedOutConnectionTasks.size() + " timed out connectionTasks on comPort '" + comPort + "'");

        return getMinimumWaitTimeForPort((OutboundComPort) comPort);
    }

    private TimeDuration getMinimumWaitTimeForPort(OutboundComPort comPort) {
        int waitTime = -1;
        List<OutboundComPortPool> containingComPortPoolsForComServer = getEngineModelService().findContainingComPortPoolsForComPort(comPort);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            waitTime = minimumWaitTime(waitTime, ((OutboundComPortPool) comPortPool).getTaskExecutionTimeout().getSeconds());
        }
        if (waitTime <= 0) {
            return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        } else {
            return new TimeDuration(waitTime, TimeDuration.TimeUnit.SECONDS);
        }
    }

    private int minimumWaitTime(int currentWaitTime, int comPortPoolTaskExecutionTimeout) {
        if (currentWaitTime < 0) {
            return comPortPoolTaskExecutionTimeout;
        } else {
            return Math.min(currentWaitTime, comPortPoolTaskExecutionTimeout);
        }
    }

    @Override
    public void releaseTasksFor(final ComPort comPort) {
        unlockComTasks(comPort);
        unlockConnectionTasks(comPort);
    }

    private void unlockComTasks(ComPort comPort) {
        List<ComTaskExecution> lockedComTasks = getCommunicationTaskService().findLockedByComPort(comPort);
        LOGGER.warning("Start unlocking " + lockedComTasks.size() + " 'BUSY' comTasks on comPort '" + comPort + "'");
        long unlockedCount = unlockComTasks(lockedComTasks);
        LOGGER.warning("Unlocked " + unlockedCount + " out of " + lockedComTasks.size() + " 'BUSY' comTasks on comPort '" + comPort + "'");
    }

    private long unlockComTasks(List<ComTaskExecution> lockedComTasks) {
        long unlockedCount = 0;
        long triedCount = 0;
        for (ComTaskExecution lockedComTask : lockedComTasks) {
            ++triedCount;
            if (unlocked(lockedComTask)) {
                ++unlockedCount;
            }
            if (triedCount % 2000 == 0) {
                LOGGER.warning("Tried " + triedCount + ", unlocked " + unlockedCount);
            }
        }
        return unlockedCount;
    }

    private long unlockConnectionTasks(List<ConnectionTask> lockedConnectionTasks) {
        long unlockedCount = 0;
        long triedCount = 0;
        for (ConnectionTask lockedComTask : lockedConnectionTasks) {
            ++triedCount;
            if (unlocked(lockedComTask)) {
                ++unlockedCount;
            }
            if (triedCount % 1000 == 0) {
                LOGGER.warning("Tried " + triedCount + ", unlocked " + unlockedCount);
            }
        }
        return unlockedCount;
    }

    private boolean unlocked(ComTaskExecution lockedComTask) {
        // execute each unlock in its own transaction, to minimize the number of zombie sessions
        // locking on database rows if the process is killed here
        return executeTransaction(() -> {
            if (attemptUnlock(lockedComTask)) {
                return true;
            }
            return false;
        });
    }

    private boolean attemptUnlock(final ComTaskExecution comTask) {
        return getCommunicationTaskService().attemptUnlockComTaskExecution(comTask);
    }

    private void unlockConnectionTasks(ComPort comPort) {
        List<ConnectionTask> lockedConnectionTasks = getConnectionTaskService().findLockedByComPort(comPort);
        LOGGER.warning("Start unlocking " + lockedConnectionTasks.size() + " BUSY connections on comPort '" + comPort + "'");
        long unlockedCount = 0;
        for (ConnectionTask lockedConnectionTask : lockedConnectionTasks) {
            if (unlocked(lockedConnectionTask)) {
                ++unlockedCount;
            }
        }
        LOGGER.warning("Unlocked " + unlockedCount + " out of " + lockedConnectionTasks.size() + " connections on comPort '" + comPort + "'");
    }

    private boolean unlocked(ConnectionTask lockedConnectionTask) {
        // execute each unlock in its own transaction, to minimize the number of zombie sessions
        // locking on database rows if the process is killed here
        return executeTransaction(() -> {
            if (attemptUnlock(lockedConnectionTask)) {
                return true;
            }
            return false;
        });
    }

    private boolean attemptUnlock(final ConnectionTask connectionTask) {
        return getConnectionTaskService().attemptUnlockConnectionTask(connectionTask);
    }

    @Override
    public ComSession createComSession(final ComSessionBuilder builder, Instant stopDate, final ComSession.SuccessIndicator successIndicator) {
        builder.injectServices(serviceProvider.ormService().getDataModel(DeviceDataServices.COMPONENT_NAME).get(), serviceProvider.connectionTaskService(), serviceProvider.thesaurus());
        //update connection task to one with correctly injected services
        serviceProvider.connectionTaskService().findConnectionTask(builder.getConnectionTask().getId()).ifPresent(builder::setConnectionTask);
        return builder.endSession(stopDate, successIndicator).create();
    }

    @Override
    public void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
        Optional<DeviceCache> deviceCache = serviceProvider.engineService().findDeviceCacheByDeviceIdentifier(deviceIdentifier);
        if (deviceCache.isPresent()) {
            DeviceCache actualDeviceCache = deviceCache.get();
            actualDeviceCache.setCacheObject(cache.getDeviceProtocolCache());
            actualDeviceCache.update();
        } else {
            serviceProvider.engineService().newDeviceCache(deviceIdentifier, cache.getDeviceProtocolCache());
        }
    }

    @Override
    public void storeMeterReadings(final DeviceIdentifier identifier, final MeterReading meterReading) {
        Device device = this.findDevice(identifier);
        device.store(meterReading);
    }

    @Override
    public void storeLoadProfile(Optional<OfflineLoadProfile> offlineLoadProfile, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
        storeLoadProfile(offlineLoadProfile, collectedLoadProfile.getLoadProfileIdentifier(), collectedLoadProfile, currentDate);
    }

    @Override
    public void storeLoadProfile(final LoadProfileIdentifier loadProfileIdentifier, final CollectedLoadProfile collectedLoadProfile, final Instant currentDate) {
        storeLoadProfile(null, collectedLoadProfile.getLoadProfileIdentifier(), collectedLoadProfile, currentDate);
    }

    private PreStoreLoadProfile.PreStoredLoadProfile getPrestoredLoadProfile(PreStoreLoadProfile loadProfilePreStorer, Optional<OfflineLoadProfile> offlineLoadProfile,
                                                                             final CollectedLoadProfile collectedLoadProfile, final Instant currentDate) {
        if (offlineLoadProfile != null) {
            return loadProfilePreStorer.preStore(offlineLoadProfile, collectedLoadProfile, currentDate);
        }
        return loadProfilePreStorer.preStore(collectedLoadProfile, currentDate);
    }

    public void storeLoadProfile(Optional<OfflineLoadProfile> offlineLoadProfile, final LoadProfileIdentifier loadProfileIdentifier, final CollectedLoadProfile collectedLoadProfile, final Instant currentDate) {
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(this.serviceProvider.mdcReadingTypeUtilService(), this);
        if (collectedLoadProfile.getChannelInfo().stream().anyMatch(channelInfo -> channelInfo.getReadingTypeMRID() != null && !channelInfo.getReadingTypeMRID().isEmpty())) {
            PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = getPrestoredLoadProfile(loadProfilePreStorer, offlineLoadProfile, collectedLoadProfile, currentDate);
            if (preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK)) {
                Map<DeviceIdentifier, MeterReadingImpl> meterReadings = new HashMap<>();
                Map<LoadProfileIdentifier, Instant> lastReadings = new HashMap<>();
                ((PreStoreLoadProfile.CompositePreStoredLoadProfile) preStoredLoadProfile).getPreStoredLoadProfiles().forEach(eachPreStoredLoadProfile -> {
                    if (!eachPreStoredLoadProfile.getIntervalBlocks().isEmpty()) {
                        Pair<DeviceIdentifier, LoadProfileIdentifier> identifiers = getIdentifiers(eachPreStoredLoadProfile, loadProfileIdentifier, collectedLoadProfile);
                        // Add interval readings
                        MeterReadingImpl meterReading = meterReadings.get(identifiers.getFirst());
                        if (meterReading != null) {
                            meterReading.addAllIntervalBlocks(eachPreStoredLoadProfile.getIntervalBlocks());
                        } else {
                            meterReading = MeterReadingImpl.newInstance();
                            meterReading.addAllIntervalBlocks(eachPreStoredLoadProfile.getIntervalBlocks());
                            meterReadings.put(identifiers.getFirst(), meterReading);
                        }
                        // Add last reading updater
                        Instant existingLastReading = lastReadings.get(identifiers.getLast());
                        if ((existingLastReading == null) || (eachPreStoredLoadProfile.getLastReading() != null && eachPreStoredLoadProfile.getLastReading().isAfter(existingLastReading))) {
                            lastReadings.put(identifiers.getLast(), eachPreStoredLoadProfile.getLastReading());
                        }
                    }

                });
                meterReadings.forEach((deviceIdentifier, meterReading) -> {
                    storeMeterReadings(deviceIdentifier, meterReading);
                });
                Map<DeviceIdentifier, List<Function<Device, Void>>> updateMap = new HashMap<>();
                // do update the loadprofile
                lastReadings.forEach((theLoadProfileIdentifier, timestamp) -> {
                    LoadProfile loadProfile = findLoadProfileOrThrowException(theLoadProfileIdentifier);
                    List<Function<Device, Void>> functionList = updateMap.computeIfAbsent(theLoadProfileIdentifier.getDeviceIdentifier(), k -> new ArrayList<>());
                    functionList.add(updateLoadProfile(loadProfile, timestamp));
                });
                // then do your thing
                updateMap.forEach((deviceId, functions) -> {
                    Device oldDevice = findDevice(deviceId);
                    Device device = serviceProvider.deviceService().findDeviceById(oldDevice.getId()).get();
                    functions.forEach(deviceVoidFunction -> deviceVoidFunction.apply(device));
                });
            }
        }
    }

    private Pair<DeviceIdentifier, LoadProfileIdentifier> getIdentifiers(PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile, LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile) {
        DeviceIdentifier deviceIdentifier;
        LoadProfileIdentifier localLoadProfileIdentifier;
        if (preStoredLoadProfile.getOfflineLoadProfile() != null && preStoredLoadProfile.getOfflineLoadProfile().isDataLoggerSlaveLoadProfile()) {
            localLoadProfileIdentifier = preStoredLoadProfile.getLoadProfileIdentifier();
            deviceIdentifier = getDeviceIdentifierFor(localLoadProfileIdentifier);
            return Pair.of(deviceIdentifier, localLoadProfileIdentifier);
        } else {
            deviceIdentifier = getDeviceIdentifierFor(collectedLoadProfile.getLoadProfileIdentifier());
            return Pair.of(deviceIdentifier, loadProfileIdentifier);
        }
    }

    @Override
    public void storeLogBookData(final LogBookIdentifier logBookIdentifier, final CollectedLogBook collectedLogBook, final Instant currentDate) {
        PreStoreLogBook logBookPreStorer = new PreStoreLogBook(this);
        Optional<Pair<DeviceIdentifier, PreStoreLogBook.LocalLogBook>> localLogBook = logBookPreStorer.preStore(collectedLogBook, currentDate);
        if (localLogBook.isPresent() && !localLogBook.get().getLast().getEndDeviceEvents().isEmpty()) {
            findLogBook(logBookIdentifier).ifPresent(lb -> {
                if (!localLogBook.get().getLast().getEndDeviceEvents().isEmpty()) {
                    Map<DeviceIdentifier, Pair<DeviceIdentifier, MeterReadingImpl>> meterReadings = new HashMap<>();
                    Map<LogBookIdentifier, Instant> lastLogBooks = new HashMap<>();
                    // Add events readings
                    Pair<DeviceIdentifier, MeterReadingImpl> meterReadingsEntry = meterReadings.get(localLogBook.get().getFirst());
                    if (meterReadingsEntry != null) {
                        meterReadingsEntry.getLast().addAllEndDeviceEvents(localLogBook.get().getLast().getEndDeviceEvents());
                    } else {
                        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                        meterReading.addAllEndDeviceEvents(localLogBook.get().getLast().getEndDeviceEvents());
                        meterReadings.put(localLogBook.get().getFirst(), Pair.of(localLogBook.get().getFirst(), meterReading));
                    }
                    // Add last logbook updater
                    Instant existingLastLogBook = lastLogBooks.get(logBookIdentifier);
                    if ((existingLastLogBook == null) || (localLogBook.get().getLast().getLastLogbook() != null && localLogBook.get().getLast().getLastLogbook().isAfter(existingLastLogBook))) {
                        lastLogBooks.put(logBookIdentifier, localLogBook.get().getLast().getLastLogbook());
                    }
                    for (Map.Entry<DeviceIdentifier, Pair<DeviceIdentifier, MeterReadingImpl>> deviceMeterReadingEntry : meterReadings.entrySet()) {
                        storeMeterReadings(deviceMeterReadingEntry.getValue().getFirst(), deviceMeterReadingEntry.getValue().getLast());
                    }
                    Map<DeviceIdentifier, List<Function<Device, Void>>> updateMap = new HashMap<>();
                    // then do the logbooks
                    lastLogBooks.entrySet().stream().forEach(entrySet -> {
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
                        Device device = serviceProvider.deviceService().findDeviceById(oldDevice.getId()).get();
                        entrySet.getValue().stream().forEach(deviceVoidFunction -> deviceVoidFunction.apply(device));
                    });
                }
            });
        }
    }

    @Override
    public void updateLogBookLastReading(final LogBookIdentifier logBookIdentifier, final Date lastExecutionStartTimestamp) {
        this.executeTransaction(() -> {
            findLogBook(logBookIdentifier).ifPresent(lb -> {
                updateLogBook(lb, lastExecutionStartTimestamp.toInstant()).apply(lb.getDevice());
            });
            return null;
        });
    }

    @Override
    public void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, final long comTaskExecutionId) {
        getCommunicationTaskService().findComTaskExecution(comTaskExecutionId).ifPresent(cte -> {
            findLogBook(logBookIdentifier).ifPresent(lb -> {
                updateLogBook(lb, cte.getLastExecutionStartTimestamp()).apply(lb.getDevice());
            });
        });
    }

    @Override
    public void signalEvent(String topic, Object source) {
        serviceProvider.eventService().postEvent(topic, source);
    }

    @Override
    public void updateDeviceMessageInformation(final MessageIdentifier messageIdentifier, final DeviceMessageStatus newDeviceMessageStatus, final Instant sentDate, final String protocolInformation) {
        DeviceMessage deviceMessage = findDeviceMessageOrThrowException(messageIdentifier);
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
    public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionIds) {
        return getPriorityComTaskService().arePriorityComTasksStillPending(priorityComTaskExecutionIds);
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
                                serviceProvider.deviceMessageSpecificationService(),
                                new OfflineDeviceImpl(device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider())
                        ));
                    }
                }
        );
        return offlineDeviceMessages;
    }

    private List<DeviceMessage> doConfirmSentMessagesAndGetPending(DeviceIdentifier identifier, int confirmationCount) {
        Device device = findDevice(identifier);
        return doConfirmSentMessagesAndGetPending(device, confirmationCount);
    }

    private List<DeviceMessage> doConfirmSentMessagesAndGetPending(Device device, int confirmationCount) {
        updateSentMessageStates(device, confirmationCount);
        return findPendingMessageAndMarkAsSent(device);
    }

    private void updateSentMessageStates(Device device, int confirmationCount) {
        List<DeviceMessage> sentMessages = device.getMessagesByState(DeviceMessageStatus.SENT);
        FutureMessageState newState = getFutureMessageState(sentMessages, confirmationCount);
        executeTransaction(() -> {
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
        executeTransaction(() -> {
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
    public List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId) {
        Device device = this.findDevice(deviceIdentifier);
        Optional<SecurityPropertySet> securityPropertySet = this.serviceProvider.deviceConfigurationService().findSecurityPropertySet(securityPropertySetId);
        if (securityPropertySet.isPresent()) {
            com.energyict.mdc.upl.properties.TypedProperties securityProperties = device.getSecurityProperties(securityPropertySet.get());
            return securityProperties.propertyNames().stream()
                    .map(propertyName -> new DeviceMasterDataExtractor.SecurityProperty() {
                        @Override
                        public String name() {
                            return propertyName;
                        }

                        @Override
                        public Object value() {
                            return securityProperties.getTypedProperty(propertyName);
                        }
                    }).collect(Collectors.toList());
        }
        return Collections.<DeviceMasterDataExtractor.SecurityProperty>emptyList();
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        Device device = findDevice(deviceIdentifier);
        InboundConnectionTask connectionTask = getInboundConnectionTask(comPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            SecurityPropertySet securityPropertySet = getSecurityPropertySet(device, connectionTask);
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
    public TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        Device device = this.findDevice(deviceIdentifier);
        InboundConnectionTask connectionTask = this.getInboundConnectionTask(comPort, device);
        if (connectionTask == null) {
            return null;
        } else {
            SecurityPropertySet securityPropertySet = getSecurityPropertySet(device, connectionTask);
            if (securityPropertySet == null) {
                return null;
            } else {
                return device.getSecurityProperties(securityPropertySet);
            }
        }
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
     * @param id
     * @return The ComTaskExecution or <code>null</code> if none are defined,
     * indicating that the Device is not ready for inbound communication
     */
    private ComTaskExecution getFirstComTaskExecution(InboundConnectionTask connectionTask, long id) {
        List<ComTaskExecution> comTaskExecutions = getCommunicationTaskService().findComTaskExecutionsByConnectionTask(connectionTask).find();
        if (!comTaskExecutions.isEmpty()) {
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                if (comTaskExecution.getDevice().getId() == id) {
                    return comTaskExecution;
                }
            }
        }
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Device device = findDevice(deviceIdentifier);
        InboundConnectionTask connectionTask = getInboundConnectionTask(inboundComPort, device);
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
        ComTaskExecution first = getFirstComTaskExecution(connectionTask);
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

    private SecurityPropertySet getSecurityPropertySet(DeviceIdentifier deviceIdentifier, ComTaskExecution comTaskExecution) {
        SecurityPropertySet securityPropertySet = null;
        if (comTaskExecution == null) {
            return null;
        } else {
            Device device = this.findDevice(deviceIdentifier);
            for (ComTaskEnablement comTaskEnablement : enabledComTasks(device.getDeviceConfiguration())) {
                if (comTaskEnablement.getComTask().equals(comTaskExecution.getComTask())) {
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
        Device device = findDevice(identifier);
        ConnectionTask<?, ?> connectionTask = getInboundConnectionTask(inboundComPort, device);
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
            Optional<Device> device = getDeviceFor(identifier);
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
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(loadProfile.getDevice().getId(), loadProfile.getDevice().getmRID());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        LogBook logBook = this.findLogBookOrThrowException(logBookIdentifier);
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(logBook.getDevice().getId(), logBook.getDevice().getmRID());
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
        LogBook logBook = findLogBookOrThrowException(logBookIdentifier);
        // Refresh device and LogBook to avoid OptimisticLockException
        Device device = serviceProvider.deviceService().findDeviceById(logBook.getDevice().getId()).get();
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
            if (source.isPresent()) {
                serviceProvider.topologyService().clearOldCommunicationPathSegments(source.get(), Instant.now());
            }

            if (target.isPresent()) {
                serviceProvider.topologyService().clearOldCommunicationPathSegments(target.get(), Instant.now());
            }
        });
        g3CommunicationPathSegmentBuilder.complete();
    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
        TopologyService.G3NeighborhoodBuilder g3NeighborhoodBuilder = serviceProvider.topologyService().buildG3Neighborhood(findDevice(sourceDeviceIdentifier));
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
                g3NeighborBuilder.lastUpdate(topologyNeighbour.getLastUpdate().toInstant());
                java.util.Date last_path_request = topologyNeighbour.getLastPathRequest();
                if (last_path_request != null) {
                    g3NeighborBuilder.lastPathRequest(last_path_request.toInstant());
                }
                g3NeighborBuilder.roundTrip(topologyNeighbour.getRoundTrip());
                g3NeighborBuilder.linkCost(topologyNeighbour.getLinkCost());
            } else {
                // partial topology receive, reconstruct missing data
                // 1. Get current device to get its parent/master
                final Optional<Device> currentDevice = serviceProvider.deviceService().findDeviceByIdentifier(sourceDeviceIdentifier);

                if (currentDevice.isPresent()) {
                    final Optional<Device> physicalGateway = serviceProvider.topologyService().getPhysicalGateway(currentDevice.get());
                    if (physicalGateway.isPresent()) {
                        // 2. Get all the slaves of master
                        final List<Device> physicalConnectedDevices = serviceProvider.topologyService().findPhysicalConnectedDevices(physicalGateway.get());


                        // 3. Filter the correct one by short MAC address
                        final Optional<Device> meterNeighbour = physicalConnectedDevices.stream().filter(d ->
                                d.getDeviceProtocolProperties().hasValueFor("ShortAddressPAN") &&
                                        ((BigDecimal) d.getDeviceProtocolProperties().getTypedProperty("ShortAddressPAN"))
                                                .equals(BigDecimal.valueOf(topologyNeighbour.getShortAddress()))
                        ).findFirst();

                        // 4. Get device identifier and MAC address from neighbour
                        if (meterNeighbour.isPresent()) {
                            TopologyService.G3NeighborhoodBuilder invertedG3NeighborhoodBuilder = serviceProvider.topologyService().buildG3Neighborhood(meterNeighbour.get());

                            final String macAddress = (String) meterNeighbour.get().getDeviceProtocolProperties().getTypedProperty("callHomeId");

                            TopologyService.G3NeighborBuilder g3NeighborBuilder = invertedG3NeighborhoodBuilder.addNeighbor(
                                    currentDevice.get(), ModulationScheme.fromId(topologyNeighbour.getModulationSchema()),
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
                            g3NeighborBuilder.nodeAddress(macAddress);
                            g3NeighborBuilder.shortAddress(topologyNeighbour.getShortAddress());
                            g3NeighborBuilder.lastUpdate(topologyNeighbour.getLastUpdate().toInstant());
                            java.util.Date last_path_request = topologyNeighbour.getLastPathRequest();
                            if (last_path_request != null) {
                                g3NeighborBuilder.lastPathRequest(last_path_request.toInstant());
                            }
                            g3NeighborBuilder.roundTrip(topologyNeighbour.getRoundTrip());
                            g3NeighborBuilder.linkCost(topologyNeighbour.getLinkCost());
                            invertedG3NeighborhoodBuilder.complete();
                        }
                    }
                }
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
        return getDeviceService().findDeviceByIdentifier(deviceIdentifier);
    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {
        Optional<Device> optionalDevice = getOptionalDeviceByIdentifier(collectedFirmwareVersions.getDeviceIdentifier());
        optionalDevice.ifPresent(device -> {
            FirmwareStorage firmwareStorage = new FirmwareStorage(serviceProvider.firmwareService(), serviceProvider.clock(), serviceProvider.deviceConfigurationService());
            firmwareStorage.updateMeterFirmwareVersion(collectedFirmwareVersions.getActiveMeterFirmwareVersion(), device);
            firmwareStorage.updateCommunicationFirmwareVersion(collectedFirmwareVersions.getActiveCommunicationFirmwareVersion(), device);
            firmwareStorage.updateAuxiliaryFirmwareVersion(collectedFirmwareVersions.getActiveAuxiliaryFirmwareVersion(), device);
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
        LoadProfile loadProfile = findLoadProfileOrThrowException(loadProfileIdentifier);
        // Refresh the device and the LoadProfile to avoid OptimisticLockException
        Device device = serviceProvider.deviceService().findDeviceById(loadProfile.getDevice().getId()).get();
        LoadProfile refreshedLoadProfile = device.getLoadProfiles().stream().filter(each -> each.getId() == loadProfile.getId()).findAny().get();
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(refreshedLoadProfile);
        loadProfileUpdater.setLastReadingIfLater(lastReading);
        loadProfileUpdater.update();
    }

    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> loadProfileUpdate, Map<LogBookIdentifier, Instant> logBookUpdate) {
        Map<DeviceIdentifier, List<Function<Device, Void>>> updateMap = new HashMap<>();

        // first do the loadprofiles
        loadProfileUpdate.forEach((loadProfileId, timestamp) -> {
            DeviceIdentifier deviceIdentifier = loadProfileId.getDeviceIdentifier();
            List<Function<Device, Void>> functionList = updateMap.computeIfAbsent(deviceIdentifier, k -> new ArrayList<>());
            functionList.add(updateLoadProfile(findLoadProfileOrThrowException(loadProfileId), timestamp));
        });
        // then do the logbooks
        logBookUpdate.forEach((logBookId, timestamp) -> {
            DeviceIdentifier deviceIdentifier = logBookId.getDeviceIdentifier();
            List<Function<Device, Void>> functionList = updateMap.computeIfAbsent(deviceIdentifier, k -> new ArrayList<>());
            functionList.add(updateLogBook(findLogBookOrThrowException(logBookId), timestamp));
        });
        // then do your thing
        updateMap.forEach((deviceId, functions) -> {
            Device oldDevice = findDevice(deviceId);
            Device device = serviceProvider.deviceService().findDeviceById(oldDevice.getId()).get();
            functions.forEach(deviceVoidFunction -> deviceVoidFunction.apply(device));
        });
    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Optional<Device> device = getDeviceFor(deviceIdentifier);
        if (device.isPresent()) {
            InboundConnectionTask connectionTask = getInboundConnectionTask(inboundComPort, device.get());
            if (connectionTask != null) {
                ComTaskExecution first = getFirstComTaskExecution(connectionTask);
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
        optionalDevice.ifPresent(device -> updateCalendars(collectedCalendar, device));
    }

    private void updateCalendars(CollectedCalendar collectedCalendar, Device device) {
        device.calendars().updateCalendars(collectedCalendar);
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {
        executeTransaction(() -> {
            getDeviceService().deleteOutdatedComTaskExecutionTriggers();
            return null;
        });
    }

    @Override
    public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile offlineLoadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
        final Device masterDevice = getDeviceFor(offlineLoadProfile.getDeviceIdentifier()).get();
        if (masterDevice != null && storageOnSlaveDevice(masterDevice)) {
            Optional<Channel> masterDeviceChannel = masterDevice.getChannels().stream().filter((c) -> c.getReadingType().getMRID().equals(readingTypeMRID)).findFirst();
            if (masterDeviceChannel.isPresent()) {
                List<DataLoggerChannelUsage> dataLoggerChannelUsages = serviceProvider.topologyService().findDataLoggerChannelUsagesForChannels(masterDeviceChannel.get(), dataPeriod);
                List<Pair<OfflineLoadProfile, Range<Instant>>> linkedOffLineLoadProfiles = new ArrayList<>();
                // 'linked' periods
                if (!dataLoggerChannelUsages.isEmpty()) {
                    dataLoggerChannelUsages.forEach(usage -> {
                        Device slave = usage.getPhysicalGatewayReference().getOrigin();
                        List<? extends ReadingType> slaveChannelReadingTypes = usage.getSlaveChannel().getReadingTypes();
                        Optional<Channel> slaveChannel = slave.getChannels().stream().filter((c) -> slaveChannelReadingTypes.contains(c.getReadingType())).findFirst();
                        if (slaveChannel.isPresent()) {
                            OfflineLoadProfile dataLoggerSlaveOfflineLoadProfile = new OfflineLoadProfileImpl(slaveChannel.get()
                                    .getLoadProfile(), serviceProvider.topologyService(), serviceProvider.identificationService()) {
                                protected void goOffline() {
                                    super.goOffline();
                                    // To avoid to have to retrieve the involved slave channels again
                                    setAllOfflineChannels(convertToOfflineChannels(Collections.singletonList(slaveChannel.get())));
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

    @Override
    public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
        return serviceProvider.engineConfigurationService().findContainingActiveComPortPoolsForComPort(comPort);
    }

    private Register getStorageRegister(Register register, Instant readingDate) {
        final Device masterDevice = register.getDevice();
        if (masterDevice != null && storageOnSlaveDevice(masterDevice)) {
            Optional<Register> slaveRegister = serviceProvider.topologyService().getSlaveRegister(register, readingDate);
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
        return serviceProvider.clock().instant();
    }

    /**
     * Fetch the lookup table "comServerMobile_completionCodes"
     */
    public List<LookupEntry> getCompletionCodeLookupEntries() {
        return new ArrayList<LookupEntry>();
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

    public interface ServiceProvider extends AbstractComServerEventImpl.ServiceProvider {

        Thesaurus thesaurus();

        ProtocolPluggableService protocolPluggableService();

        DeviceMessageSpecificationService deviceMessageSpecificationService();

        EngineConfigurationService engineConfigurationService();

        ConnectionTaskService connectionTaskService();

        CommunicationTaskService communicationTaskService();

        OrmService ormService();

        PriorityComTaskService priorityComTaskService();

        DeviceService deviceService();

        RegisterService registerService();

        LoadProfileService loadProfileService();

        LogBookService logBookService();

        TopologyService topologyService();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        EngineService engineService();

        UserService userService();

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
        public DeviceMessageSpecificationService deviceMessageSpecificationService() {
            return serviceProvider.deviceMessageSpecificationService();
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
