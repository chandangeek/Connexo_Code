package com.energyict.mdc.engine.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.io.Serializable;

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
    private volatile TaskHistoryService taskHistoryService;
    private volatile IssueService issueService;
    private volatile DeviceDataService deviceDataService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile UserService userService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;

    public EngineServiceImpl() {
    }

    @Inject
    public EngineServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService) {
        super();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
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
    public DeviceCache findDeviceCacheByDeviceId(Device device) {
        return dataModel.mapper(DeviceCache.class).getUnique("device", device).orNull();
    }

    @Override
    public void install() {
        this.install(false);
    }

    @Override
    public DeviceCache newDeviceCache(Device device, Serializable simpleCacheObject) {
        return dataModel.getInstance(DeviceCacheImpl.class).initialize(device, simpleCacheObject);
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
    public void setNlsService(NlsService nlsService) {
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
    public void setTaskHistoryService(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
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
            }
        };
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.thesaurus, this.eventService).install(exeuteDdl);
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
        public TaskHistoryService taskHistoryService() {
            return taskHistoryService;
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
    }
}
