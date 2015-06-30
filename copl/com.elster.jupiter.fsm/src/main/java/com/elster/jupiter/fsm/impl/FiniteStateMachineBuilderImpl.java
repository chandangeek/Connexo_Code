package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;

/**
 * Provides an implementation for the {@link FiniteStateMachineBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:15)
 */
public class FiniteStateMachineBuilderImpl implements FiniteStateMachineBuilder {

    private final DataModel dataModel;
    private final FiniteStateMachineImpl underConstruction;
    private BuildState state;

    public FiniteStateMachineBuilderImpl(DataModel dataModel, FiniteStateMachineImpl underConstruction) {
        super();
        this.dataModel = dataModel;
        this.underConstruction = underConstruction;
        this.state = new UnderConstruction();
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected FiniteStateMachineImpl getUnderConstruction() {
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
    public FiniteStateMachineImpl complete() {
        this.state.complete();
        this.state = new Complete();
        return this.underConstruction;
    }

    @Override
    public FiniteStateMachine complete(State initial) {
        this.underConstruction.setInitialState((StateImpl) initial);
        return this.complete();
    }

    protected interface BuildState {
        StateBuilder newState(boolean custom, String name);
        FiniteStateMachine complete();
    }

    private class UnderConstruction implements BuildState {
        @Override
        public StateBuilder newState(boolean custom, String name) {
            return doNewState(custom, name);
        }

        @Override
        public FiniteStateMachine complete() {
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
        public FiniteStateMachine complete() {
            illegalStateException();
            return null;
        }

        private void illegalStateException() {
            throw new IllegalStateException("The finite state machine building process is already complete");
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
        public StateBuilder onEntry(StateChangeBusinessProcess process) {
            this.underConstruction.addOnEntry(process);
            return this;
        }

        @Override
        public StateBuilder onExit(StateChangeBusinessProcess process) {
            this.underConstruction.addOnExit(process);
            return this;
        }

        @Override
        public State complete() {
            FiniteStateMachineBuilderImpl.this.underConstruction.add(this.underConstruction);
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
            return this.transitionTo(state, Optional.<String>empty());
        }

        @Override
        public StateBuilder transitionTo(State state, String name) {
            return this.transitionTo(state, Optional.of(name));
        }

        private StateBuilder transitionTo(State state, Optional<String> name) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, state, this.eventType);
            name.ifPresent(stateTransition::setName);
            underConstruction.add(stateTransition);
            return this.continuation;
        }

        @Override
        public StateBuilder transitionTo(State state, TranslationKey translationKey) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, state, this.eventType);
            stateTransition.setTranslationKey(translationKey.getKey());
            underConstruction.add(stateTransition);
            return this.continuation;
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder) {
            return this.transitionTo((ServerStateBuilder) stateBuilder, Optional.<String>empty());
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder, String name) {
            return this.transitionTo((ServerStateBuilder) stateBuilder, Optional.of(name));
        }

        private StateBuilder transitionTo(ServerStateBuilder stateBuilder, Optional<String> name) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, stateBuilder.getUnderConstruction(), this.eventType);
            name.ifPresent(stateTransition::setName);
            underConstruction.add(stateTransition);
            return this.continuation;
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder, TranslationKey translationKey) {
            return this.transitionTo((ServerStateBuilder) stateBuilder, translationKey);
        }

        private StateBuilder transitionTo(ServerStateBuilder stateBuilder, TranslationKey translationKey) {
            StateTransitionImpl stateTransition = dataModel.getInstance(StateTransitionImpl.class).initialize(underConstruction, this.from, stateBuilder.getUnderConstruction(), this.eventType);
            stateTransition.setTranslationKey(translationKey.getKey());
            underConstruction.add(stateTransition);
            return this.continuation;
        }
    }

}