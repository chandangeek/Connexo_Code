/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

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
    protected BuildState state;

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

    @Override
    public StateBuilder newCustomState(String name, Stage stage) {
        return this.state.newState(true, name, stage);
    }

    @Override
    public StateBuilder newStandardState(String symbolicName, Stage stage) {
        return this.state.newState(false, symbolicName, stage);
    }

    private StateBuilder doNewState(boolean custom, String name) {
        StateImpl underConstruction = this.newInitializedState(custom, name);
        return new StateBuilderImpl(underConstruction);
    }

    private StateBuilder doNewState(boolean custom, String name, Stage stage) {
        StateImpl underConstruction = this.newInitializedState(custom, name, stage);
        return new StateBuilderImpl(underConstruction);
    }

    protected StateImpl newInitializedState(boolean custom, String name) {
        return this.dataModel.getInstance(StateImpl.class).initialize(this.underConstruction, custom, name);
    }

    protected StateImpl newInitializedState(boolean custom, String name, Stage stage) {
        return this.dataModel.getInstance(StateImpl.class).initialize(this.underConstruction, custom, name, stage);
    }


    private void checkStageRequired(boolean stageGiven) {
        if((!underConstruction.getStageSet().isPresent() && stageGiven) || (underConstruction.getStageSet().isPresent() && !stageGiven)) {
            throw new IllegalStateException();
        }
    }


    private void checkIfStageExistsInFSMUnderContruction(Stage stage) {
        underConstruction.getStageSet().get().getStages()
                .stream()
                .filter(stage::equals)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The StageSet of the FiniteStateMachine does not contain the given stage"));
    }

    @Override
    public FiniteStateMachine complete(State initial) {
        this.underConstruction.setInitialState(initial);
        this.state.complete();
        this.state = new Complete();
        this.underConstruction.save();
        return this.underConstruction;
    }

    protected interface BuildState {
        StateBuilder newState(boolean custom, String name);
        StateBuilder newState(boolean custom, String name, Stage stage);
        FiniteStateMachine complete();
    }

    private class UnderConstruction implements BuildState {
        @Override
        public StateBuilder newState(boolean custom, String name) {
            checkStageRequired(false);
            return doNewState(custom, name);
        }

        @Override
        public StateBuilder newState(boolean custom, String name, Stage stage) {
            checkStageRequired(true);
            checkIfStageExistsInFSMUnderContruction(stage);
            return doNewState(custom, name, stage);
        }

        @Override
        public FiniteStateMachine complete() {
            return underConstruction;
        }

    }

    protected class Complete implements BuildState {
        @Override
        public StateBuilder newState(boolean custom, String name) {
            illegalStateException();
            return null;
        }

        @Override
        public StateBuilder newState(boolean custom, String name, Stage stage) {
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
        public StateBuilder onEntry(BpmProcessDefinition process) {
            this.underConstruction.addOnEntry(process);
            return this;
        }

        @Override
        public StateBuilder onExit(BpmProcessDefinition process) {
            this.underConstruction.addOnExit(process);
            return this;
        }

        @Override
        public StateBuilder onEntry(EndPointConfiguration endPointConfiguration) {
            this.underConstruction.addOnEntry(endPointConfiguration);
            return this;
        }

        @Override
        public StateBuilder onExit(EndPointConfiguration endPointConfiguration) {
            this.underConstruction.addOnExit(endPointConfiguration);
            return this;
        }

        @Override
        public State complete() {
            FiniteStateMachineBuilderImpl.this.underConstruction.validateAndAdd(this.underConstruction);
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