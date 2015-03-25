package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.DefaultDeviceLifeCycleState;
import com.elster.jupiter.fsm.DeviceLifeCycleState;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link DeviceLifeCycleState} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:20)
 */
public class DeviceLifeCycleStateImpl extends StateImpl implements DeviceLifeCycleState {

    private DefaultDeviceLifeCycleState defaultDeviceLifeCycleState = DefaultDeviceLifeCycleState.Initial;

    @Inject
    public DeviceLifeCycleStateImpl(ServerFinateStateMachineService finateStateMachineService, DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public String getName() {
        return this.getDefaultState().name();
    }

    @Override
    public DefaultDeviceLifeCycleState getDefaultState() {
        return this.defaultDeviceLifeCycleState;
    }

}