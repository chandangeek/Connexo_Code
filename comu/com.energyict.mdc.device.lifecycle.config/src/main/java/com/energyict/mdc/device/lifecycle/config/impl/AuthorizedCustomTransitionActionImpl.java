package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.StateTransition;

/**
 * Models the authorization of a custom {@link StateTransition} as an action.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (13:51)
 */
public class AuthorizedCustomTransitionActionImpl extends AuthorizedTransitionActionImpl {

    public AuthorizedCustomTransitionActionImpl initialize(DeviceLifeCycle deviceLifeCycle, StateTransition stateTransition) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.setStateTransition(stateTransition);
        return this;
    }

    @Override
    public boolean isStandard() {
        return false;
    }

}