package com.energyict.mdc.engine.monitor.impl.rest;

import com.elster.jupiter.nls.*;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.MdcMonitorAppService;
import com.energyict.mdc.engine.monitor.impl.rest.resource.MonitorResource;
import com.energyict.mdc.engine.status.StatusService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.*;


@Component(
        name = "com.energyict.mdc.engine.monitor.rest",
        service = {Application.class},
        property = {"alias=/CSMonitor", "app=CSM", "name=CSM"},
        immediate = true)
public class MdcMonitorRestApplication extends Application implements TranslationKeyProvider{

    private volatile StatusService statusService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                MonitorResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }


    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(MdcMonitorAppService.APPLICATION_KEY, Layer.REST);
    }

    @Override
    public String getComponentName() {
        return MdcMonitorAppService.APPLICATION_KEY;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return null;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(statusService).to(StatusService.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}


