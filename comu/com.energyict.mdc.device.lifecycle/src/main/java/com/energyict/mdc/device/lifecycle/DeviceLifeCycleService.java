package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.FinateStateMachine;

/**
 * Provides services to manage {@link DeviceLifeCycle}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:22)
 */
public interface DeviceLifeCycleService {

    String COMPONENT_NAME = "DLC";

    public DeviceLifeCycle newDeviceLifeCycleUsing(FinateStateMachine finateStateMachine);

}