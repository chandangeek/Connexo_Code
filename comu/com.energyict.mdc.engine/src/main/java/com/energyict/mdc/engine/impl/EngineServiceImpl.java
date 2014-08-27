package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiServiceFactory;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

/**
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 13:17
 */
@Component(name="com.energyict.mdc.engine", service = {EngineService.class, InstallService.class}, property = "name=" + EngineService.COMPONENTNAME, immediate = true)
public class EngineServiceImpl implements EngineService, InstallService {
    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    private volatile HexService hexService;
    private volatile EngineModelService engineModelService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile IssueService issueService;
    private volatile DeviceDataService deviceDataService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile StatusService statusService;
    private volatile ManagementBeanFactory managementBeanFactory;
    private volatile WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory;
    private volatile WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    private volatile EmbeddedWebServerFactory embeddedWebServerFactory;
    private volatile UserService userService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialComponentService;
    private volatile NlsService nlsService;

    public EngineServiceImpl() {
    }

    @Inject
    public EngineServiceImpl(
            OrmService ormService, EventService eventService, NlsService nlsService, TransactionService transactionService, Clock clock, ThreadPrincipalService threadPrincipalService,
            HexService hexService, EngineModelService engineModelService, IssueService issueService,
            DeviceDataService deviceDataService, MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, DeviceConfigurationService deviceConfigurationService,
            ProtocolPluggableService protocolPluggableService, StatusService statusService,
            ManagementBeanFactory managementBeanFactory, EmbeddedWebServerFactory embeddedWebServerFactory,
            WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory, WebSocketEventPublisherFactory webSocketEventPublisherFactory,
            SocketService socketService, SerialComponentService serialComponentService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        setTransactionService(transactionService);
        setClock(clock);
        setHexService(hexService);
        setEngineModelService(engineModelService);
        setThreadPrincipalService(threadPrincipalService);
        setIssueService(issueService);
        setDeviceDataService(deviceDataService);
        setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        setUserService(userService);
        setDeviceConfigurationService(deviceConfigurationService);
        setProtocolPluggableService(protocolPluggableService);
        setSocketService(socketService);
        setSerialComponentService(serialComponentService);
        this.setStatusService(statusService);
        this.setManagementBeanFactory(managementBeanFactory);
        this.setEmbeddedWebServerFactory(embeddedWebServerFactory);
        this.setWebSocketQueryApiServiceFactory(webSocketQueryApiServiceFactory);
        this.setWebSocketEventPublisherFactory(webSocketEventPublisherFactory);
        this.install();
        activate();
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
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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
    public void setWebSocketQueryApiServiceFactory(WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory) {
        this.webSocketQueryApiServiceFactory = webSocketQueryApiServiceFactory;
    }

    @Reference
    public void setWebSocketEventPublisherFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Reference
    public void setEmbeddedWebServerFactory(EmbeddedWebServerFactory embeddedWebServerFactory) {
        this.embeddedWebServerFactory = embeddedWebServerFactory;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
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
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference
    public void setSerialComponentService(SerialComponentService serialComponentService) {
        this.serialComponentService = serialComponentService;
    }

    DataModel getDataModel() {
        return dataModel;
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
                bind(WebSocketQueryApiServiceFactory.class).toInstance(webSocketQueryApiServiceFactory);
                bind(WebSocketEventPublisherFactory.class).toInstance(webSocketEventPublisherFactory);
                bind(EmbeddedWebServerFactory.class).toInstance(embeddedWebServerFactory);
                bind(ManagementBeanFactory.class).toInstance(managementBeanFactory);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        ServiceProvider.instance.set(serviceProvider);
    }

    @Deactivate
    public void deactivate() {
        ServiceProvider.instance.set(null);
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus, this.eventService).install(true);
    }

    private class ServiceProviderImpl implements ServiceProvider {

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public DeviceDataService deviceDataService() {
            return deviceDataService;
        }

        @Override
        public EngineModelService engineModelService() {
            return engineModelService;
        }

        @Override
        public EngineService engineService() {
            return EngineServiceImpl.this;
        }

        @Override
        public NlsService nlsService() {
            return nlsService;
        }

        @Override
        public EventService eventService() {
            return eventService;
        }

        @Override
        public HexService hexService() {
            return hexService;
        }

        @Override
        public IssueService issueService() {
            return issueService;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return threadPrincipalService;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }

        @Override
        public UserService userService() {
            return userService;
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
        public SerialComponentService serialComponentService() {
            return serialComponentService;
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return managementBeanFactory;
        }

        @Override
        public WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory() {
            return webSocketQueryApiServiceFactory;
        }

        @Override
        public WebSocketEventPublisherFactory webSocketEventPublisherFactory() {
            return webSocketEventPublisherFactory;
        }

        @Override
        public EmbeddedWebServerFactory embeddedWebServerFactory() {
            return embeddedWebServerFactory;
        }
    }

}
