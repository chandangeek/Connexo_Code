package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComTaskCompletionOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.CompletionCode;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link DashboardService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:27)
 */
@Component(name = "com.energyict.mdc.dashboard", service = {DashboardService.class}, property = "name=DBS")
public class DashboardServiceImpl implements DashboardService {

    private volatile EngineModelService engineModelService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceDataService deviceDataService;
    private volatile ProtocolPluggableService protocolPluggableService;

    public DashboardServiceImpl() {
        super();
    }

    @Inject
    public DashboardServiceImpl(EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, DeviceDataService deviceDataService, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setDeviceDataService(deviceDataService);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Override
    public ConnectionStatusOverview getConnectionStatusOverview() {
        ConnectionStatusOverviewImpl overview = new ConnectionStatusOverviewImpl();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus));
        }
        return overview;
    }

    @Override
    public ComTaskCompletionOverview getComTaskCompletionOverview() {
        ComTaskCompletionOverviewImpl overview = new ComTaskCompletionOverviewImpl();
        for (CompletionCode completionCode : CompletionCode.values()) {
            overview.add(new CounterImpl<>(completionCode));
        }
        return overview;
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown() {
        ComPortPoolBreakdownImpl breakdown = new ComPortPoolBreakdownImpl();
        for (ComPortPool comPortPool : this.availableComPortPools()) {
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comPortPool));
        }
        return breakdown;
    }

    private List<ComPortPool> availableComPortPools () {
        return this.engineModelService.findAllComPortPools();
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown() {
        ConnectionTypeBreakdownImpl breakdown = new ConnectionTypeBreakdownImpl();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : this.availableConnectionTypes()) {
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(connectionTypePluggableClass));
        }
        return breakdown;
    }

    private List<ConnectionTypePluggableClass> availableConnectionTypes () {
        return this.protocolPluggableService.findAllConnectionTypePluggableClasses();
    }

    @Override
    public DeviceTypeBreakdown getDeviceTypeBreakdown() {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        for (DeviceType deviceType : this.availableDeviceTypes()) {
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(deviceType));
        }
        return breakdown;
    }

    private List<DeviceType> availableDeviceTypes () {
        return this.deviceConfigurationService.findAllDeviceTypes().find();
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}