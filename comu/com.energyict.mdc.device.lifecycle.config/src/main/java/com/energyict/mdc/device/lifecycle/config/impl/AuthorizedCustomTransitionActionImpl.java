package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.nls.Thesaurus;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Models the authorization of a custom {@link StateTransition} as an action.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (13:51)
 */
public class AuthorizedCustomTransitionActionImpl extends AuthorizedTransitionActionImpl {

    @Inject
    public AuthorizedCustomTransitionActionImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    public AuthorizedCustomTransitionActionImpl initialize(DeviceLifeCycleImpl deviceLifeCycle, StateTransition stateTransition) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.setStateTransition(stateTransition);
        return this;
    }

    @Override
    public boolean isStandard() {
        return false;
    }

}