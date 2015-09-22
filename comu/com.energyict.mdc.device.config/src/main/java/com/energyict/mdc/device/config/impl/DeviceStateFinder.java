package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import java.util.Optional;


public class DeviceStateFinder implements CanFindByLongPrimaryKey<DeviceState> {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public DeviceStateFinder(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }


    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE_STATE;
    }

    @Override
    public Class<DeviceState> valueDomain() {
        return DeviceState.class;
    }

    @Override
    public Optional<DeviceState> findByPrimaryKey(long id) {
        Optional<State> stateOptional = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().stream().flatMap(dlc -> dlc.getFiniteStateMachine().getStates().stream()).filter(s -> s.getId() == id).findAny();
        return stateOptional.isPresent() ? Optional.of(new DeviceState(stateOptional.get())) : Optional.empty();
    }
}
