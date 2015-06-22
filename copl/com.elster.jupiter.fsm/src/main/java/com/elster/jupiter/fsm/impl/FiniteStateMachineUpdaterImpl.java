package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.UnknownStateException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link FiniteStateMachineUpdater} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-09 (09:50)
 */
public class FiniteStateMachineUpdaterImpl extends FiniteStateMachineBuilderImpl implements FiniteStateMachineUpdater {

    private final Thesaurus thesaurus;
    private final List<StateUpdaterImpl> completedStateUpdaters = new ArrayList<>();
    private final List<AddState> completedNewStates = new ArrayList<>();
    private final List<StateTransitionImpl> completedNewTransitions = new ArrayList<>();

    public FiniteStateMachineUpdaterImpl(DataModel dataModel, Thesaurus thesaurus, FiniteStateMachineImpl updateTarget) {
        super(dataModel, updateTarget);
        this.thesaurus = thesaurus;
    }

    @Override
    public FiniteStateMachineUpdater setName(String newName) {
        this.getUnderConstruction().setName(newName);
        return this;
    }

    @Override
    public FiniteStateMachineUpdater removeState(String obsoleteStateName) {
        FiniteStateMachineImpl stateMachine = this.getUnderConstruction();
        StateImpl obsoleteState = this.findStateIfExists(obsoleteStateName, stateMachine);
        stateMachine.removeState(obsoleteState);
        return this;
    }

    /**
     * Finds the {@link State} with the specified name and throws
     * an {@link UnknownStateException} when the State does not exist.
     *
     * @param stateName The name of the State
     * @param stateMachine The {@link FiniteStateMachine}
     * @return The State
     */
    private StateImpl findStateIfExists(String stateName, FiniteStateMachineImpl stateMachine) {
        return stateMachine
                    .findInternalState(stateName)
                    .orElseThrow(() -> new UnknownStateException(this.thesaurus, stateMachine, stateName));
    }

    /**
     * Finds the {@link State} with the specified id and throws
     * an {@link UnknownStateException} when the State does not exist.
     *
     * @param id The id of the State
     * @param stateMachine The {@link FiniteStateMachine}
     * @return The State
     */
    private StateImpl findStateIfExists(long id, FiniteStateMachineImpl stateMachine) {
        return stateMachine
                    .findInternalState(id)
                    .orElseThrow(() -> new UnknownStateException(this.thesaurus, stateMachine, id));
    }

    @Override
    public FiniteStateMachineUpdater removeState(State obsoleteState) {
        FiniteStateMachineImpl stateMachine = this.getUnderConstruction();
        if (obsoleteState.getFiniteStateMachine().getId() == stateMachine.getId()) {
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
    public StateUpdater state(long id) {
        return new StateUpdaterImpl(this.findStateIfExists(id, this.getUnderConstruction()));
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
    public FiniteStateMachineImpl complete() {
        FiniteStateMachineImpl updated = super.complete();
        this.completedStateUpdaters.forEach(StateUpdaterImpl::save);
        this.completedNewStates.forEach(AddState::save);
        this.completedNewTransitions.forEach(updated::add);
        updated.save();
        return updated;
    }

    @Override
    public FiniteStateMachine complete(State initial) {
        Optional<State> oldInitialState =
                getUnderConstruction()
                        .getStates()
                        .stream()
                        .filter(State::isInitial)
                        .findFirst();
        getUnderConstruction().setInitialState((StateImpl) initial);
        if (oldInitialState.isPresent() && !stateWillBeAutoSaved(oldInitialState.get())){
            new StateUpdaterImpl((StateImpl) oldInitialState.get()).complete();
        }
        if (!stateWillBeAutoSaved(initial)){
            new StateUpdaterImpl((StateImpl) initial).complete();
        }
        return complete();
    }

    private boolean stateWillBeAutoSaved(State candidate){
        // state will be auto saved if it is a new state or it is already modified
        return candidate.getId() == 0 || this.completedStateUpdaters.stream().anyMatch(updater -> updater.hasTheSameState(candidate));
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
        public FiniteStateMachineUpdater.TransitionBuilder on(StateTransitionEventType eventType) {
            return new AddTransitionToExistingState(this, eventType, this.underConstruction);
        }

        @Override
        public StateUpdater prohibit(StateTransitionEventType eventType) {
            FiniteStateMachineUpdaterImpl.this.getUnderConstruction().removeTransition(this.underConstruction, eventType);
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

        boolean hasTheSameState(State candidate){
            return candidate!= null && this.underConstruction.getId() == candidate.getId();
        }
    }

    private class AddState implements ServerStateBuilder {
        private final FiniteStateMachineImpl stateMachine;
        private final StateImpl underConstruction;
        private final List<StateTransitionImpl> transitionsUnderConstruction = new ArrayList<>();

        private AddState(FiniteStateMachineImpl stateMachine, StateImpl underConstruction) {
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
        public FiniteStateMachineBuilder.TransitionBuilder on(StateTransitionEventType eventType) {
            return new AddTransitionToNewState(this, eventType, this.underConstruction);
        }

        public StateBuilder addTransition(State from, State to, StateTransitionEventType eventType, Optional<String> name) {
            StateTransitionImpl stateTransition = getDataModel().getInstance(StateTransitionImpl.class).initialize(this.stateMachine, from, to, eventType);
            name.ifPresent(stateTransition::setName);
            this.transitionsUnderConstruction.add(stateTransition);
            return this;
        }

        public StateBuilder addTransition(State from, State to, StateTransitionEventType eventType, TranslationKey translationKey) {
            StateTransitionImpl stateTransition = getDataModel().getInstance(StateTransitionImpl.class).initialize(this.stateMachine, from, to, eventType);
            stateTransition.setTranslationKey(translationKey.getKey());
            this.transitionsUnderConstruction.add(stateTransition);
            return this;
        }

        @Override
        public State complete() {
            FiniteStateMachineUpdaterImpl.this.completedNewStates.add(this);
            FiniteStateMachineUpdaterImpl.this.completedNewTransitions.addAll(this.transitionsUnderConstruction);
            return this.underConstruction;
        }

        private void save() {
            this.stateMachine.validateAndAdd(this.underConstruction);
        }

    }

    private class AddTransitionToNewState implements FiniteStateMachineBuilder.TransitionBuilder {

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
            return this.continuation.addTransition(this.from, state, this.eventType, Optional.empty());
        }

        @Override
        public StateBuilder transitionTo(State state, String name) {
            return this.continuation.addTransition(this.from, state, this.eventType, Optional.of(name));
        }

        @Override
        public StateBuilder transitionTo(State state, TranslationKey translationKey) {
            return this.continuation.addTransition(this.from, state, this.eventType, translationKey);
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
            return this.continuation.addTransition(this.from, stateBuilder.getUnderConstruction(), this.eventType, name);
        }

        @Override
        public StateBuilder transitionTo(StateBuilder stateBuilder, TranslationKey translationKey) {
            return this.transitionTo((ServerStateBuilder) stateBuilder, translationKey);
        }

        private StateBuilder transitionTo(ServerStateBuilder stateBuilder, TranslationKey translationKey) {
            return this.continuation.addTransition(this.from, stateBuilder.getUnderConstruction(), this.eventType, translationKey);
        }
    }

    private class AddTransitionToExistingState implements FiniteStateMachineUpdater.TransitionBuilder {
        private final StateUpdater continuation;
        private final StateTransitionEventType eventType;
        private final State from;
        private String transitionName;

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
        public StateUpdater transitionTo(long stateId) {
            return this.transitionTo(findStateIfExists(stateId, getUnderConstruction()));
        }

        @Override
        public FiniteStateMachineUpdater.TransitionBuilder setName(String transitionName) {
            this.transitionName = transitionName;
            return this;
        }

        @Override
        public StateUpdater transitionTo(State state) {
            FiniteStateMachineImpl stateMachine = getUnderConstruction();
            StateTransitionImpl stateTransition = this.newInitializedTransition(state, stateMachine);
            stateTransition.setName(this.transitionName);
            if (isPersistent(state)) {
                stateMachine.validateAndAdd(stateTransition);
            }
            else {
                FiniteStateMachineUpdaterImpl.this.completedNewTransitions.add(stateTransition);
            }
            return this.continuation;
        }

        private boolean isPersistent(State state) {
            return state.getId() != 0;
        }

        private StateTransitionImpl newInitializedTransition(State state, FiniteStateMachineImpl stateMachine) {
            return getDataModel().getInstance(StateTransitionImpl.class).initialize(stateMachine, this.from, state, this.eventType);
        }

        @Override
        public StateUpdater transitionTo(StateBuilder stateBuilder) {
            return this.transitionTo((ServerStateBuilder) stateBuilder);
        }

        private StateUpdater transitionTo(ServerStateBuilder stateBuilder) {
            FiniteStateMachineImpl stateMachine = getUnderConstruction();
            StateTransitionImpl stateTransition = this.newInitializedTransition(stateBuilder.getUnderConstruction(), stateMachine);
            FiniteStateMachineUpdaterImpl.this.completedNewTransitions.add(stateTransition);
            return this.continuation;
        }
    }

}