/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor.app.impl.rest;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.app.MdcMonitorAppService;
import com.energyict.mdc.engine.monitor.app.impl.rest.resource.MonitorResource;
import com.energyict.mdc.engine.status.StatusService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component(
        name = "com.energyict.mdc.engine.monitor.rest",
        service = {Application.class},
        property = {"alias=/CSMonitor", "app=MDC", "name=" + MdcMonitorAppService.COMPONENT_NAME},
        immediate = true)
public class MdcMonitorRestApplication extends Application {

    private volatile StatusService statusService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
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
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
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
        this.thesaurus = nlsService.getThesaurus(MdcMonitorAppService.COMPONENT_NAME, Layer.REST);
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(statusService).to(StatusService.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
            bind(userService).to(UserService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}


