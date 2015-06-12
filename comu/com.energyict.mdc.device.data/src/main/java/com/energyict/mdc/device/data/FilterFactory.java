package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by bvn on 4/15/15.
 */
public class FilterFactory {

    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public FilterFactory(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, MeteringGroupsService meteringGroupsService) {
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringGroupsService = meteringGroupsService;
    }

    public ConnectionTaskFilterSpecification buildFilterFromMessage(ConnectionTaskFilterSpecificationMessage primitiveFilter) {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (primitiveFilter.currentStates!=null) {
            filter.taskStatuses.addAll(primitiveFilter.currentStates.stream().map(TaskStatus::valueOf).collect(toList()));
        }

        filter.comPortPools = new HashSet<>();
        if (primitiveFilter.comPortPools!=null) {
            // already optimized
            for (ComPortPool comPortPool : engineConfigurationService.findAllComPortPools()) {
                filter.comPortPools.addAll(primitiveFilter.comPortPools.stream().filter(comPortPoolId -> comPortPool.getId() == comPortPoolId).map(comPortPoolId -> comPortPool).collect(Collectors.toList()));
            }
        }

        if (primitiveFilter.connectionTypes!=null) {
            filter.connectionTypes = primitiveFilter.connectionTypes
                    .stream()
                    .map(protocolPluggableService::findConnectionTypePluggableClass)
                    .flatMap(asStream())
                    .collect(Collectors.toSet());
        }

        filter.latestResults = EnumSet.noneOf(ComSession.SuccessIndicator.class);
        if (primitiveFilter.latestResults!=null) {
            filter.latestResults=primitiveFilter.latestResults.stream().map(ComSession.SuccessIndicator::valueOf).collect(toSet());
        }

        filter.latestStatuses = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);
        if (primitiveFilter.latestStates!=null) {
            filter.latestStatuses.addAll(primitiveFilter.latestStates.stream().map(ConnectionTask.SuccessIndicator::valueOf).collect(toSet()));
        }

        filter.deviceTypes = new HashSet<>();
        if (primitiveFilter.deviceTypes!=null) {
            filter.deviceTypes.addAll(
                    primitiveFilter.deviceTypes.stream()
                            .map(deviceConfigurationService::findDeviceType)
                            .flatMap(asStream())
                            .collect(toList()));
        }

        if (primitiveFilter.startIntervalFrom!=null || primitiveFilter.startIntervalTo!=null) {
            filter.lastSessionStart = Interval.of(primitiveFilter.startIntervalFrom, primitiveFilter.startIntervalTo);
        }

        filter.deviceGroups = new HashSet<>();
        if (primitiveFilter.deviceGroups!=null) {
            filter.deviceGroups.addAll(primitiveFilter.deviceGroups.stream().map(meteringGroupsService::findEndDeviceGroup).filter(java.util.Optional::isPresent).map(java.util.Optional::get).collect(toSet()));
        }

        if (primitiveFilter.finishIntervalFrom!= null || primitiveFilter.finishIntervalTo!=null) {
            filter.lastSessionEnd = Interval.of(primitiveFilter.finishIntervalFrom, primitiveFilter.finishIntervalTo);
        }

        return filter;
    }


}
