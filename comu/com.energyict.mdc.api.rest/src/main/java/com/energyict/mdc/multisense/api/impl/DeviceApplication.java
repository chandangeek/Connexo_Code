package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.multisense.public.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/dda", "app=MDC", "name=" + DeviceApplication.COMPONENT_NAME, "version=v2.0"})
public class DeviceApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(DeviceApplication.class.getName());

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DDA";

    private volatile DeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile TopologyService topologyService;
    private volatile IssueService issueService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                DeviceResource.class,
                DeviceConfigurationResource.class,
                DeviceTypeResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return new ArrayList<>();
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(deviceService).to(DeviceService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(issueService).to(IssueService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(topologyService).to(TopologyService.class);
            bind(DeviceInfoFactory.class).to(DeviceInfoFactory.class);
            bind(DeviceTypeInfoFactory.class).to(DeviceTypeInfoFactory.class);
            bind(DeviceConfigurationInfoFactory.class).to(DeviceConfigurationInfoFactory.class);
            bind(finiteStateMachineService).to(FiniteStateMachineService.class);
            bind(deviceLifeCycleService).to(DeviceLifeCycleService.class);
            bind(transactionService).to(TransactionService.class);
        }
    }

}