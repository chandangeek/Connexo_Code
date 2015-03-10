package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
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
    private final List<AddState> completedNewStates = new ArrayList<>();
    private final List<StateTransitionImpl> completedNewTransitions = new ArrayList<>();

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
    public StateBuilder newCustomState(String name) {
        return this.newState(true, name);
    }

    @Override
    public StateBuilder newStandardState(String name) {
        return this.newState(false, name);
    }

    public StateBuilder newState(boolean custom, String name) {
        return new AddState(this.getUnderConstruction(), this.newInitializedState(custom, name));
    }

    @Override
    public FinateStateMachineImpl complete() {
        FinateStateMachineImpl updated = super.complete();
        this.completedStateUpdaters.forEach(StateUpdaterImpl::save);
        this.completedNewStates.forEach(AddState::save);
        this.completedNewTransitions.forEach(updated::add);
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
        public StateUpdater onEntry(String deploymentId, String processId) {
            this.underConstruction.addOnEntry(deploymentId, processId);
            return this;
        }

        @Override
        public StateUpdater removeOnEntry(String deploymentId, String processId) {
            this.underConstruction.removeOnEntry(deploymentId, processId);
            return this;
        }

        @Override
        public StateUpdater onExit(String deploymentId, String processId) {
            this.underConstruction.addOnExit(deploymentId, processId);
            return this;
        }

        @Override
        public StateUpdater removeOnExit(String deploymentId, String processId) {
            this.underConstruction.removeOnExit(deploymentId, processId);
            return this;
        }

        @Override
        public FinateStateMachineUpdater.TransitionBuilder on(StateTransitionEventType eventType) {
            return new AddTransitionToExistingState(this, eventType, this.underConstruction);
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

    private class AddState implements ServerStateBuilder {
        private final FinateStateMachineImpl stateMachine;
        private final StateImpl underConstruction;
        private final List<StateTransitionImpl> transitionsUnderConstruction = new ArrayList<>();

        private AddState(FinateStateMachineImpl stateMachine, StateImpl underConstruction) {
            super();
            this.stateMachine = stateMachine;
            this.underConstruction = underConstruction;
        }

        @Override
        public StateImpl getUnderConstruction() {
            return this.underConstruction;
        }

        @Override
        public StateBuilder onEntry(String deploymentId, String processId) {
            this.underConstruction.addOnEntry(deploymentId, processId);
            return this;
        }

        @Override
        public StateBuilder onExit(String deploymentId, String processId) {
            this.underConstruction.addOnExit(deploymentId, processId);
            return this;
        }

        @Override
        public FinateStateMachineBuilder.TransitionBuilder on(StateTransitionEventType eventType) {
            return new AddTransitionToNewState(this, eventType, this.underConstruction);
        }

        public StateBuilder addTransition(State from, State to, StateTransitionEventType eventType) {
            this.transitionsUnderConstruction.add(getDataModel().getInstance(StateTransitionImpl.class).initialize(this.stateMachine, from, to, eventType));
            return this;
        }

        @Override
        public State complete() {
            FinateStateMachineUpdaterImpl.this.completedNewStates.add(this);
            FinateStateMachineUpdaterImpl.this.completedNewTransitions.addAll(this.transitionsUnderConstruction);
            return this.underConstruction;
        }

        private void save() {
            this.stateMachine.add(this.underConstruction);
        }

    }

    private class AddTransitionToNewState implements FinateStateMachineBuilder.TransitionBuilder {

        private final AddState continuation;
        private final StateTransitionEventType eventType;
        private final State from;

        private AddTransitionToNewState(AddState continuation, StateTransitionEventType eventType, State from) {
            super();
            this.continuation = continuation;
            this.eventType = eventType;
            this.from = from;
        }

        @Override
        public StateBuilder transitionTo(State state) {
            return this.continuation.addTransition(this.from, state, this.eventType);
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder) {
            return this.transitionTo((ServerStateBuilder) stateBuilder);
        }

        private StateBuilder transitionTo(ServerStateBuilder stateBuilder) {
            return this.continuation.addTransition(this.from, stateBuilder.getUnderConstruction(), this.eventType);
        }

    }

    private class AddTransitionToExistingState implements FinateStateMachineUpdater.TransitionBuilder {
        private final StateUpdater continuation;
        private final StateTransitionEventType eventType;
        private final State from;

        private AddTransitionToExistingState(StateUpdater continuation, StateTransitionEventType eventType, State from) {
            super();
            this.continuation = continuation;
            this.eventType = eventType;
            this.from = from;
        }

        @Override
        public StateUpdater transitionTo(String stateName) {
            return this.transitionTo(findStateIfExists(stateName, getUnderConstruction()));
        }

        @Override
        public StateUpdater transitionTo(State state) {
            FinateStateMachineImpl stateMachine = getUnderConstruction();
            StateTransitionImpl stateTransition = this.newInitializedTransition(state, stateMachine);
            if (isPersistent(state)) {
                stateMachine.add(stateTransition);
            }
            else {
                FinateStateMachineUpdaterImpl.this.completedNewTransitions.add(stateTransition);
            }
            return this.continuation;
        }

        private boolean isPersistent(State state) {
            return state.getId() != 0;
        }

        private StateTransitionImpl newInitializedTransition(State state, FinateStateMachineImpl stateMachine) {
            return getDataModel().getInstance(StateTransitionImpl.class).initialize(stateMachine, this.from, state, this.eventType);
        }

        @Override
        public StateUpdater transitionTo(StateBuilder stateBuilder) {
            return this.transitionTo((ServerStateBuilder) stateBuilder);
        }

        private StateUpdater transitionTo(ServerStateBuilder stateBuilder) {
            FinateStateMachineImpl stateMachine = getUnderConstruction();
            StateTransitionImpl stateTransition = this.newInitializedTransition(stateBuilder.getUnderConstruction(), stateMachine);
            FinateStateMachineUpdaterImpl.this.completedNewTransitions.add(stateTransition);
            return this.continuation;
        }
    }

}