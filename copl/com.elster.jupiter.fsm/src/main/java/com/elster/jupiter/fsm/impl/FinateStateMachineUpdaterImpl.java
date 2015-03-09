package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineUpdater;
import com.elster.jupiter.orm.DataModel;

/**
 * Provides an implementation for the {@link FinateStateMachineUpdater} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:50)
 */
public class FinateStateMachineUpdaterImpl extends FinateStateMachineBuilderImpl implements FinateStateMachineUpdater {

    public FinateStateMachineUpdaterImpl(DataModel dataModel, FinateStateMachineImpl updateTarget) {
        super(dataModel, updateTarget);
    }

    @Override
    public FinateStateMachineUpdater setName(String newName) {
        this.getState().setName(newName);
        return this;
    }

    @Override
    public FinateStateMachineUpdater setTopic(String newTopic) {
        this.getState().setTopic(newTopic);
        return this;
    }

    @Override
    public FinateStateMachine complete() {
        FinateStateMachine updated = super.complete();
        updated.save();
        return updated;
    }

}