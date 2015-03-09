package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.UnknownStateException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link FinateStateMachineUpdater} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:50)
 */
public class FinateStateMachineUpdaterImpl extends FinateStateMachineBuilderImpl implements FinateStateMachineUpdater {

    private final Thesaurus thesaurus;
    private final List<StateUpdaterImpl> completedStateUpdaters = new ArrayList<>();

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
        StateImpl obsoleteState = this.findStateIfExists(obsoleteStateName, stateMachine);
        stateMachine.removeState(obsoleteState);
        return this;
    }

    /**
     * Finds the {@link State} with the specified name and throws
     * an {@link UnknownStateException} when the State does not exist.
     *
     * @param stateName The name of the State
     * @param stateMachine The {@link FinateStateMachine}
     * @return The State
     */
    private StateImpl findStateIfExists(String stateName, FinateStateMachineImpl stateMachine) {
        return stateMachine
                    .findInternalState(stateName)
                    .orElseThrow(() -> new UnknownStateException(this.thesaurus, stateMachine, stateName));
    }

    @Override
    public FinateStateMachineUpdater removeState(State obsoleteState) {
        FinateStateMachineImpl stateMachine = this.getUnderConstruction();
        if (obsoleteState.getFinateStateMachine().getId() == stateMachine.getId()) {
            stateMachine.removeState((StateImpl) obsoleteState);
            return this;
        }
        else {
            throw new UnknownStateException(this.thesaurus, stateMachine, obsoleteState.getName());
        }
    }

    @Override
    public StateUpdater state(String name) {
        return new StateUpdaterImpl(this.findStateIfExists(name, this.getUnderConstruction()));
    }

    @Override
    public FinateStateMachine complete() {
        FinateStateMachine updated = super.complete();
        this.completedStateUpdaters.forEach(StateUpdaterImpl::save);
        updated.save();
        return updated;
    }

    private class StateUpdaterImpl implements StateUpdater {
        private final StateImpl underConstruction;

        private StateUpdaterImpl(StateImpl underConstruction) {
            super();
            this.underConstruction = underConstruction;
        }

        @Override
        public StateUpdater setName(String newName) {
            this.underConstruction.setName(newName);
            return this;
        }

        @Override
        public StateUpdater prohibit(StateTransitionEventType eventType) {
            FinateStateMachineUpdaterImpl.this.getUnderConstruction().removeTransition(this.underConstruction, eventType);
            return this;
        }

        @Override
        public State complete() {
            completedStateUpdaters.add(this);
            return this.underConstruction;
        }

        private void save() {
            this.underConstruction.save();
        }

    }

}