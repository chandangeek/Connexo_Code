package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.CustomState;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomState} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:43)
 */
public class CustomStateImpl extends StateImpl implements CustomState {

    @Inject
    public CustomStateImpl(ServerFinateStateMachineService finateStateMachineService, DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    public CustomStateImpl initialize(FinateStateMachine finateStateMachine, String name) {
        this.setFinateStateMachine(finateStateMachine);
        this.setName(name);
        return this;
    }

}