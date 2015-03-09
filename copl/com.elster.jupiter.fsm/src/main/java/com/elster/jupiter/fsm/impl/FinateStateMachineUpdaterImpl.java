package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.UnknownStateException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

/**
 * Provides an implementation for the {@link FinateStateMachineUpdater} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:50)
 */
public class FinateStateMachineUpdaterImpl extends FinateStateMachineBuilderImpl implements FinateStateMachineUpdater {

    private final Thesaurus thesaurus;

    public FinateStateMachineUpdaterImpl(DataModel dataModel, Thesaurus thesaurus, FinateStateMachineImpl updateTarget) {
        super(dataModel, updateTarget);
        this.thesaurus = thesaurus;
    }

    @Override
    public FinateStateMachineUpdater setName(String newName) {
        this.getUnderConstruction().setName(newName);
        return this;
    }

    @Override
    public FinateStateMachineUpdater setTopic(String newTopic) {
        this.getUnderConstruction().setTopic(newTopic);
        return this;
    }

    @Override
    public FinateStateMachineUpdater removeState(String obsoleteStateName) {
        FinateStateMachineImpl stateMachine = this.getUnderConstruction();
        State obsoleteState = stateMachine
                .getState(obsoleteStateName)
                .orElseThrow(() -> new UnknownStateException(this.thesaurus, stateMachine, obsoleteStateName));
        stateMachine.removeState(obsoleteState);
        return this;
    }

    @Override
    public FinateStateMachineUpdater removeState(State obsoleteState) {
        FinateStateMachineImpl stateMachine = this.getUnderConstruction();
        if (obsoleteState.getFinateStateMachine().getId() == stateMachine.getId()) {
            stateMachine.removeState(obsoleteState);
            return this;
        }
        else {
            throw new UnknownStateException(this.thesaurus, stateMachine, obsoleteState.getName());
        }
    }

    @Override
    public FinateStateMachine complete() {
        FinateStateMachine updated = super.complete();
        updated.save();
        return updated;
    }

}