package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceDataImporterContext", service = {DeviceDataImporterContext.class})
public class DeviceDataImporterContext {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile DeviceImportService deviceImportService;
    private volatile TopologyService topologyService;
    private volatile MeteringService meteringService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DeviceImportService getDeviceImportService() {
        return deviceImportService;
    }

    @Reference
    public void setDeviceImportService(DeviceImportService deviceImportService) {
        this.deviceImportService = deviceImportService;
    }

    public TopologyService getTopologyService() {
        return topologyService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public DeviceLifeCycleService getDeviceLifeCycleService() {
        return deviceLifeCycleService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
