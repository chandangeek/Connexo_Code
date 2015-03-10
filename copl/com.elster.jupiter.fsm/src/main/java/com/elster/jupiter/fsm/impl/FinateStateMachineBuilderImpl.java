package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

/**
 * Provides an implementation for the {@link FinateStateMachineBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:15)
 */
public class FinateStateMachineBuilderImpl implements FinateStateMachineBuilder {

    private final DataModel dataModel;
    private final FinateStateMachineImpl underConstruction;
    private BuildState state;

    public FinateStateMachineBuilderImpl(DataModel dataModel, FinateStateMachineImpl underConstruction) {
        super();
        this.dataModel = dataModel;
        this.underConstruction = underConstruction;
        this.state = new UnderConstruction();
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected FinateStateMachineImpl getUnderConstruction() {
        return underConstruction;
    }

    @Override
    public StateBuilder newCustomState(String name) {
        return this.state.newState(true, name);
    }

    @Override
    public StateBuilder newStandardState(String symbolicName) {
        return this.state.newState(false, symbolicName);
    }

    private StateBuilder doNewState(boolean custom, String name) {
        StateImpl underConstruction = this.newInitializedState(custom, name);
        return new StateBuilderImpl(underConstruction);
    }

    protected StateImpl newInitializedState(boolean custom, String name) {
        return this.dataModel.getInstance(StateImpl.class).initialize(this.underConstruction, custom, name);
    }

    @Override
    public FinateStateMachineImpl complete() {
        this.state.complete();
        this.state = new Complete();
        return this.underConstruction;
    }

    protected interface BuildState {
        StateBuilder newState(boolean custom, String name);
        FinateStateMachine complete();
    }

    private class UnderConstruction implements BuildState {
        @Override
        public StateBuilder newState(boolean custom, String name) {
            return doNewState(custom, name);
        }

        @Override
        public FinateStateMachine complete() {
            return underConstruction;
        }

    }

    private class Complete implements BuildState {
        @Override
        public StateBuilder newState(boolean custom, String name) {
            illegalStateException();
            return null;
        }

        @Override
        public FinateStateMachine complete() {
            illegalStateException();
            return null;
        }

        private void illegalStateException() {
            throw new IllegalStateException("The finate state machine building process is already complete");
        }

    };

    private class StateBuilderImpl implements ServerStateBuilder {
        private final StateImpl underConstruction;

        private StateBuilderImpl(StateImpl underConstruction) {
            super();
            this.underConstruction = underConstruction;
        }

        @Override
        public StateImpl getUnderConstruction() {
            return this.underConstruction;
        }

        @Override
        public TransitionBuilder on(StateTransitionEventType eventType) {
            return new TransitionBuilderImpl(this, eventType, this.underConstruction);
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
        public State complete() {
            FinateStateMachineBuilderImpl.this.underConstruction.add(this.underConstruction);
            return this.underConstruction;
        }
    }

    private class TransitionBuilderImpl implements TransitionBuilder {
        private final StateBuilder continuation;
        private final StateTransitionEventType eventType;
        private final State from;

        private TransitionBuilderImpl(StateBuilder continuation, StateTransitionEventType eventType, State from) {
            super();
            this.continuation = continuation;
            this.eventType = eventType;
            this.from = from;
        }

        @Override
        public StateBuilder transitionTo(State state) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, state, this.eventType);
            underConstruction.add(stateTransition);
            return this.continuation;
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder) {
            return this.transitionTo((ServerStateBuilder) stateBuilder);
        }

        private StateBuilder transitionTo(ServerStateBuilder stateBuilder) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, stateBuilder.getUnderConstruction(), this.eventType);
            underConstruction.add(stateTransition);
            return this.continuation;
        }
    }

}