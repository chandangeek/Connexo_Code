package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Provides an implementation for the {@link AuthorizedStandardTransitionAction} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (13:21)
 */
public class AuthorizedStandardTransitionActionImpl extends AuthorizedTransitionActionImpl implements AuthorizedStandardTransitionAction {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private TransitionType type;

    @Inject
    public AuthorizedStandardTransitionActionImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    public AuthorizedTransitionActionImpl initialize(DeviceLifeCycleImpl deviceLifeCycle, StateTransition stateTransition) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.setStateTransition(stateTransition);
        this.type = TransitionType.from(stateTransition).get();
        return this;
    }

    @Override
    public TransitionType getType() {
        return this.type;
    }

}