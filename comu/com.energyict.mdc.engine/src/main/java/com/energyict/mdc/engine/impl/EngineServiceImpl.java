package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.PrettyPrintTimeDurationTranslationKeys;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.elster.jupiter.appserver.AppService.SERVER_NAME_PROPERTY_NAME;

/**
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 13:17
 */
@Component(name = "com.energyict.mdc.engine",
        service = {EngineService.class, InstallService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + EngineService.COMPONENTNAME,
                "osgi.command.scope=mdc",
                "osgi.command.function=become",
                "osgi.command.function=launchComServer",
                "osgi.command.function=stopComServer",
                "osgi.command.function=lcs",
                "osgi.command.function=scs"},
        immediate = true)
public class EngineServiceImpl implements EngineService, InstallService, TranslationKeyProvider, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile Clock clock;
    private volatile NlsService nlsService;
    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private volatile HexService hexService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile IssueService issueService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile LogBookService logBookService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile StatusService statusService;
    private volatile ManagementBeanFactory managementBeanFactory;
    private volatile UserService userService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialATComponentService;
    private volatile FirmwareService firmwareService;
    private volatile List<DeactivationNotificationListener> deactivationNotificationListeners = new CopyOnWriteArrayList<>();

    private OptionalIdentificationService identificationService = new OptionalIdentificationService();
    private ComServerLauncher launcher;
    private ProtocolDeploymentListenerRegistration protocolDeploymentListenerRegistration;

    public EngineServiceImpl() {
    }

    @Inject
    public EngineServiceImpl(
            BundleContext bundleContext,
            OrmService ormService, EventService eventService, NlsService nlsService, TransactionService transactionService, Clock clock, ThreadPrincipalService threadPrincipalService,
            HexService hexService, EngineConfigurationService engineConfigurationService, IssueService issueService,
            MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, DeviceConfigurationService deviceConfigurationService,
            ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, LogBookService logBookService, DeviceService deviceService, TopologyService topologyService,
            ProtocolPluggableService protocolPluggableService, StatusService statusService,
            ManagementBeanFactory managementBeanFactory,
            SocketService socketService,
            SerialComponentService serialATComponentService,
            IdentificationService identificationService,
            FirmwareService firmwareService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setTransactionService(transactionService);
        this.setClock(clock);
        this.setHexService(hexService);
        this.setEngineConfigurationService(engineConfigurationService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setIssueService(issueService);
        this.setDeviceService(deviceService);
        this.setTopologyService(topologyService);
        this.setConnectionTaskService(connectionTaskService);
        this.setCommunicationTaskService(communicationTaskService);
        this.setLogBookService(logBookService);
        this.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        this.setUserService(userService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setSocketService(socketService);
        this.setSerialATComponentService(serialATComponentService);
        this.setStatusService(statusService);
        this.setManagementBeanFactory(managementBeanFactory);
        this.addIdentificationService(identificationService);
        this.setFirmwareService(firmwareService);
        this.install();
        this.activate(bundleContext);
    }

    @Override
    public Optional<DeviceCache> findDeviceCacheByDevice(Device device) {
        return dataModel.mapper(DeviceCache.class).getUnique("device", device);
    }

    @Override
    public DeviceCache newDeviceCache(Device device, DeviceProtocolCache deviceProtocolCache) {
        return dataModel.getInstance(DeviceCacheImpl.class).initialize(device, deviceProtocolCache);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setHexService(HexService hexService) {
        this.hexService = hexService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public NlsService nlsService() {
        return this.nlsService;
    }

    @Override
    public String getComponentName() {
        return EngineService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PrettyPrintTimeDurationTranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        List<MessageSeed> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(MessageSeeds.values()));
        keys.addAll(Arrays.asList(com.energyict.mdc.engine.impl.commands.MessageSeeds.values()));
        return keys;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Meter Data Collection Engine");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.AT + "))")
    public void setSerialATComponentService(SerialComponentService serialATComponentService) {
        this.serialATComponentService = serialATComponentService;
    }

    @Override
    public IdentificationService identificationService() {
        return this.identificationService;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addIdentificationService(IdentificationService identificationService) {
        this.identificationService.set(identificationService);
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @SuppressWarnings("unused")
    public void removeIdentificationService(IdentificationService identificationService) {
        this.identificationService.clear();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(StatusService.class).toInstance(statusService);
                bind(ManagementBeanFactory.class).toInstance(managementBeanFactory);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.dataModel.register(this.getModule());
        this.setHostNameIfOverruled(bundleContext);
        if (this.dataModel.isInstalled()) {
            this.tryStartComServer();
        }
    }

    private void setHostNameIfOverruled(BundleContext context) {
        Optional
                .ofNullable(context.getProperty(SERVER_NAME_PROPERTY_NAME))
                .ifPresent(HostName::setCurrent);
    }

    private void tryStartComServer() {
        this.launcher = new ComServerLauncher(new RunningComServerServiceProvider());
        this.protocolDeploymentListenerRegistration = this.protocolPluggableService.register(this.launcher);
        this.launcher.startComServer();
        if (this.launcher.isStarted()) {
            System.out.println("ComServer " + HostName.getCurrent() + " started!");
        } else {
            System.out.println("ComServer with name " + HostName.getCurrent() + " is not configured, not active or start is delayed because not all required services are active yet (see OSGi log service)");
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    public void deactivate() {
        this.stopComServer();
        this.deactivationNotificationListeners.forEach(EngineService.DeactivationNotificationListener::engineServiceDeactivationStarted);
    }

    @Override
    public void register(DeactivationNotificationListener deactivationNotificationListener) {
        this.deactivationNotificationListeners.add(deactivationNotificationListener);
    }

    @Override
    public void unregister(DeactivationNotificationListener deactivationNotificationListener) {
        this.deactivationNotificationListeners.remove(deactivationNotificationListener);
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService).install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "NLS", "DDC", "MIO");
    }

    @SuppressWarnings("unused")
    public void become(String serverName) {
        this.stopComServer();
        HostName.setCurrent(serverName);
        this.launchComServer();
    }

    @SuppressWarnings("unused")
    public void launchComServer() {
        if (this.launcher == null || !this.launcher.isStarted()) {
            this.tryStartComServer();
        } else {
            System.out.println("ComServer " + HostName.getCurrent() + " is already running");
        }
    }

    @SuppressWarnings("unused")
    public void lcs() {
        this.launchComServer();
    }

    @SuppressWarnings("unused")
    public void scs() {
        this.stopComServer();
    }

    @SuppressWarnings("unused")
    public void stopComServer() {
        if (this.launcher != null) {
            System.out.println("Stopping ComServer " + HostName.getCurrent());
            this.protocolDeploymentListenerRegistration.unregister();
            this.launcher.stopComServer();
            this.launcher = null;
        }
    }

    private static class OptionalIdentificationService implements IdentificationService {
        private AtomicReference<Optional<IdentificationService>> identificationService = new AtomicReference<>(Optional.empty());

        private void set(IdentificationService identificationService) {
            this.identificationService.set(Optional.of(identificationService));
        }

        private void clear() {
            this.identificationService.set(Optional.<IdentificationService>empty());
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByDatabaseId(long id) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByDatabaseId(id))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByMRID(String mRID) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByMRID(mRID))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierBySerialNumber(serialNumber))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByCallHomeId(String serialNumber) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByCallHomeId(serialNumber))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(BaseDevice device) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierForAlreadyKnownDevice(device))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByProperty(propertyName, propertyValue))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByConnectionTaskProperty(connectionTypeClass, propertyName, propertyValue))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierByDatabaseId(id))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(BaseLoadProfile loadProfile) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(loadProfileObisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierForFirstLoadProfileOnDevice(deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierByDatabaseId(long id) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierByDatabaseId(id))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierByObisCodeAndDeviceIdentifier(logbookObisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(BaseLogBook logBook) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierForAlreadyKnownLogbook(logBook))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public MessageIdentifier createMessageIdentifierByDatabaseId(long id) {
            return this.identificationService
                    .get()
                    .map(s -> s.createMessageIdentifierByDatabaseId(id))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage) {
            return this.identificationService
                    .get()
                    .map(s -> s.createMessageIdentifierForAlreadyKnownMessage(deviceMessage))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public MessageIdentifier createMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts) {
            return this.identificationService
                    .get()
                    .map(s -> s.createMessageIdentifierByDeviceAndProtocolInfoParts(deviceIdentifier, messageProtocolInfoParts))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }
    }

    private static class IdentificationServiceMissingException extends RuntimeException {
        private IdentificationServiceMissingException() {
            super("IdentificationService missing in EngineService");
        }
    }

    private class RunningComServerServiceProvider implements RunningComServerImpl.ServiceProvider {
        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public IssueService issueService() {
            return issueService;
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return managementBeanFactory;
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return threadPrincipalService;
        }

        @Override
        public UserService userService() {
            return userService;
        }

        @Override
        public NlsService nlsService() {
            return nlsService;
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return protocolPluggableService;
        }

        @Override
        public SocketService socketService() {
            return socketService;
        }

        @Override
        public HexService hexService() {
            return hexService;
        }

        @Override
        public SerialComponentService serialAtComponentService() {
            return serialATComponentService;
        }

        @Override
        public MeteringService meteringService() {
            return meteringService;
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return engineConfigurationService;
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }

        @Override
        public DeviceService deviceService() {
            return deviceService;
        }

        @Override
        public TopologyService topologyService() {
            return topologyService;
        }

        @Override
        public EngineService engineService() {
            return EngineServiceImpl.this;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }

        @Override
        public EventService eventService() {
            return eventService;
        }

        @Override
        public IdentificationService identificationService() {
            return identificationService;
        }

        @Override
        public FirmwareService firmwareService() {
            return firmwareService;
        }
    }

}
