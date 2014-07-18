package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.impl.monitor.ComServerMonitor;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortImplMBean;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link StatusService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (11:24)
 */
@Component(name = "com.energyict.mdc.engine.status", service = StatusService.class, property = "name=STS")
public class StatusServiceImpl implements StatusService {

    private volatile Clock clock;
    private volatile EngineModelService engineModelService;
    private volatile ManagementBeanFactory managementBeanFactory;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Override
    public ComServerStatus getStatus() {
        Optional<ComServer> comServer = this.engineModelService.findComServerBySystemName();
        if (comServer.isPresent()) {
            return this.getStatus(comServer.get());
        }
        else {
            return new UnknownComServerStatusImpl();
        }
    }

    private ComServerStatus getStatus(ComServer comServer) {
        Optional<ComServerMonitorImplMBean> monitor = this.managementBeanFactory.findFor(comServer);
        if (monitor.isPresent()) {
            List<ScheduledComPortMonitor> comPortMonitors = this.getComPortMonitors(comServer);
            return new RunningComServerStatusImpl(clock, comServer, (ComServerMonitor) monitor.get(), comPortMonitors);
        }
        else {
            return new NotRunningComServerStatusImpl(comServer);
        }
    }

    private List<ScheduledComPortMonitor> getComPortMonitors(ComServer comServer) {
        if (comServer instanceof OutboundCapableComServer) {
            return this.getComPortMonitors((OutboundCapableComServer) comServer);
        }
        else {
            return Collections.emptyList();
        }
    }

    private List<ScheduledComPortMonitor> getComPortMonitors(OutboundCapableComServer comServer) {
        List<OutboundComPort> comPorts = comServer.getOutboundComPorts();
        List<ScheduledComPortMonitor> monitors = new ArrayList<>(comPorts.size()); // Normally all comports have a monitor
        for (OutboundComPort comPort : comPorts) {
            Optional<ScheduledComPortImplMBean> monitor = this.managementBeanFactory.findFor(comPort);
            if (monitor.isPresent()) {
                monitors.add((ScheduledComPortMonitor) monitor);
            }
        }
        return monitors;
    }

}