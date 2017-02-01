/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link StatusService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (11:24)
 */
@Component(name = "com.energyict.mdc.engine.status", service = StatusService.class, property = "name=STS")
public class StatusServiceImpl implements StatusService {

    private volatile Clock clock;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ManagementBeanFactory managementBeanFactory;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Override
    public ComServerStatus getStatus() {
        Optional<ComServer> comServer = this.engineConfigurationService.findComServerBySystemName();
        return comServer.map(this::getStatus).orElseGet(UnknownComServerStatusImpl::new);
    }

    private ComServerStatus getStatus(ComServer comServer) {
        Optional<ComServerMonitorImplMBean> monitor = this.managementBeanFactory.findFor(comServer);
        if (monitor.isPresent()) {
            List<ScheduledComPortMonitor> scheduledComPortMonitors = this.getScheduledComPortMonitors(comServer);
            List<InboundComPortMonitor> inboundComPortMonitors = this.getInboundComPortMonitors(comServer);
            return new RunningComServerStatusImpl(clock, comServer, (ComServerMonitor) monitor.get(), scheduledComPortMonitors, inboundComPortMonitors);
        }
        else {
            return new NotRunningComServerStatusImpl(comServer);
        }
    }

    private List<ScheduledComPortMonitor> getScheduledComPortMonitors(ComServer comServer) {
        if (comServer instanceof OutboundCapableComServer) {
            return ((OutboundCapableComServer)comServer).getOutboundComPorts()
                    .stream()
                    .filter(ComPort::isActive)
                    .map(this.managementBeanFactory::findFor)
                    .flatMap(Functions.asStream())
                    .map(ScheduledComPortMonitor.class::cast)
                    .collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }

    private List<InboundComPortMonitor> getInboundComPortMonitors(ComServer comServer) {
        if (comServer instanceof InboundCapableComServer) {
            return ((InboundCapableComServer)comServer).getInboundComPorts()
                    .stream()
                    .filter(ComPort::isActive)
                    .map(this.managementBeanFactory::findFor)
                    .flatMap(Functions.asStream())
                    .map(InboundComPortMonitor.class::cast)
                    .collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }


}