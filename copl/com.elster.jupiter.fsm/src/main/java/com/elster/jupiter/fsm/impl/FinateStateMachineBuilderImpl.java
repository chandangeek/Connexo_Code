package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
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

    @Override
    public StateBuilder newState(String name) {
        return this.state.newState(name);
    }

    private StateBuilder doNewState(String name) {
        CustomStateImpl underConstruction = this.dataModel.getInstance(CustomStateImpl.class).initialize(this.underConstruction, name);
        return new StateBuilderImpl(underConstruction);
    }

    @Override
    public TransitionSourceBuilder on(StateTransitionEventType eventType) {
        return new TransitionSourceBuilderImpl(eventType);
    }

    @Override
    public FinateStateMachine complete() {
        this.state.complete();
        this.state = new Complete();
        return this.underConstruction;
    }

    private interface BuildState {
        StateBuilder newState(String name);
        FinateStateMachine complete();
        void completed(StateBuilder stateBuilder);
    }

    private class UnderConstruction implements BuildState {
        private StateBuilder stateUnderConstruction;

        @Override
        public StateBuilder newState(String name) {
            if (this.stateUnderConstruction == null) {
                this.stateUnderConstruction = doNewState(name);
                return this.stateUnderConstruction;
            }
            else {
                throw new IllegalStateException("The ongoing state building process should be completed first");
            }
        }

        @Override
        public void completed(StateBuilder stateBuilder) {
            if (this.stateUnderConstruction == stateBuilder) {
                this.stateUnderConstruction = null;
            }
            else {
                throw new IllegalStateException("CodingException: receiving completed event from unknown StateBuilder");
            }
        }

        @Override
        public FinateStateMachine complete() {
            return underConstruction;
        }

    }

    private class Complete implements BuildState {
        @Override
        public StateBuilder newState(String name) {
            illegalStateException();
            return null;
        }

        @Override
        public void completed(StateBuilder stateBuilder) {
            illegalStateException();
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

    private class StateBuilderImpl implements StateBuilder {
        private final StateImpl underConstruction;

        private StateBuilderImpl(StateImpl underConstruction) {
            super();
            this.underConstruction = underConstruction;
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
            FinateStateMachineBuilderImpl.this.state.completed(this);
            return this.underConstruction;
        }
    }

    private class TransitionSourceBuilderImpl implements TransitionSourceBuilder {
        private final StateTransitionEventType eventType;

        private TransitionSourceBuilderImpl(StateTransitionEventType eventType) {
            super();
            this.eventType = eventType;
        }

        @Override
        public TransitionTargetBuilder transitionFrom(State state) {
            return new TransitionTargetBuilderImpl(this.eventType, state);
        }
    }

    private class TransitionTargetBuilderImpl implements TransitionTargetBuilder {
        private final StateTransitionEventType eventType;
        private final State from;

        private TransitionTargetBuilderImpl(StateTransitionEventType eventType, State from) {
            super();
            this.eventType = eventType;
            this.from = from;
        }

        @Override
        public StateTransition to(State state) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, state, this.eventType);
            underConstruction.add(stateTransition);
            return stateTransition;
        }
    }
}