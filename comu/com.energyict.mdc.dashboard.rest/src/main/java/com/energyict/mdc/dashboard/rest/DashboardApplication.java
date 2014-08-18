package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusResource;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionOverviewResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionResource;
import com.energyict.mdc.dashboard.rest.status.impl.DashboardFieldResource;
import com.energyict.mdc.dashboard.rest.status.impl.MessageSeeds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:32)
 */
@Component(name = "com.energyict.mdc.dashboard.rest", service = { Application.class, InstallService.class }, immediate = true, property = {"alias=/dsr", "name=" + DashboardApplication.COMPONENT_NAME})
public class DashboardApplication extends Application implements InstallService {

    public static final String COMPONENT_NAME = "DSR";

    private volatile StatusService statusService;
    private volatile EngineModelService engineModelService;
    private volatile NlsService nlsService;
    private volatile DashboardService dashboardService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceDataService deviceDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ExceptionLogger.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
                ComServerStatusResource.class,
                ComServerStatusSummaryResource.class,
                ConnectionOverviewResource.class,
                DashboardFieldResource.class,
                ConnectionResource.class,
                DeviceConfigurationService.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public void install() {
        Installer installer = new Installer();
        installer.createTranslations(COMPONENT_NAME, nlsService.getThesaurus(COMPONENT_NAME, Layer.REST), Layer.REST, MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(statusService).to(StatusService.class);
            bind(engineModelService).to(EngineModelService.class);
            bind(nlsService).to(NlsService.class);
            bind(dashboardService).to(DashboardService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceDataService).to(DeviceDataService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
        }
    }

}