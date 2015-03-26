package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.UnsupportedStateTransitionException;
import com.elster.jupiter.fsm.impl.constraints.AtLeastOneState;
import com.elster.jupiter.fsm.impl.constraints.ExactlyOneInitialState;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link FiniteStateMachine} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:29)
 */
@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_FINITE_STATE_MACHINE_NAME + "}", groups = { Save.Create.class, Save.Update.class })
@AtLeastOneState(groups = { Save.Create.class, Save.Update.class })
@ExactlyOneInitialState(groups = { Save.Create.class, Save.Update.class })
public class FiniteStateMachineImpl implements FiniteStateMachine {

    public enum Fields {
        NAME("name"),
        STATES("states"),
        TRANSITIONS("transitions");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @Valid
    private List<StateImpl> states = new ArrayList<>();
    @Valid
    private List<StateTransitionImpl> transitions = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public FiniteStateMachineImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public FiniteStateMachineImpl initialize(String name) {
        this.name = name;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreationTimestamp() {
        return createTime;
    }

    @Override
    public Instant getModifiedTimestamp() {
        return modTime;
    }

    @Override
    public String getName() {
        return this.name;
    }

    void setName(String newName) {
        this.name = newName;
    }

    @Override
    public List<State> getStates() {
        return Collections.unmodifiableList(this.states);
    }

    @Override
    public State getInitialState() {
        return this.findInitialState().orElseThrow(
                () -> new IllegalStateException(this.thesaurus.getString(MessageSeeds.Keys.EXACTLY_ONE_INITIAL_STATE, "A finite state machine must have exactly one initial state")));
    }

    void setInitialState(StateImpl state) {
        this.findInitialState().ifPresent(s -> s.setInitial(false));
        state.setInitial(true);
    }

    private Optional<StateImpl> findInitialState() {
        return this.states
                .stream()
                .filter(State::isInitial)
                .findFirst();
    }

    @Override
    public Optional<State> getState(String name) {
        Optional<StateImpl> state = this.findInternalState(name);
        if (state.isPresent()) {
            return Optional.of(state.get());
        }
        else {
            return Optional.empty();
        }
    }

    Optional<StateImpl> findInternalState(String name) {
        return this.states.stream().filter(s -> name.equals(s.getName())).findFirst();
    }

    Optional<StateImpl> findInternalState(long id) {
        return this.states.stream().filter(s -> s.getId() == id).findFirst();
    }

    void add(StateImpl state) {
        this.states.add(state);
    }

    void removeState(StateImpl obsoleteState) {
        this.removeObsoleteTransitions(obsoleteState);
        obsoleteState.prepareDelete();
        this.states.remove(obsoleteState);
    }

    private void removeObsoleteTransitions(StateImpl obsoleteState) {
        this.transitions.removeAll(this.transitions
                .stream()
                .filter(t -> this.relatesTo(t, obsoleteState))
                .collect(Collectors.toList()));
    }

    private boolean relatesTo(StateTransition transition, StateImpl state) {
        return transition.getFrom().getId() == state.getId() || transition.getTo().getId() == state.getId();
    }

    @Override
    public List<StateTransition> getTransitions() {
        return Collections.unmodifiableList(this.transitions);
    }

    void add(StateTransitionImpl stateTransition) {
        this.transitions.add(stateTransition);
    }

    void removeTransition(StateImpl state, StateTransitionEventType eventType) {
        Optional<StateTransitionImpl> stateTransition = this.transitions
                .stream()
                .filter(t -> this.relatesTo(t, state))
                .filter(t -> t.getEventType().getId() == eventType.getId())
                .findFirst();
        if (stateTransition.isPresent()) {
            this.transitions.remove(stateTransition.get());
        }
        else {
            throw new UnsupportedStateTransitionException(this.thesaurus, this, state, eventType);
        }
    }

    @Override
    public FiniteStateMachineUpdater update() {
        return new FiniteStateMachineUpdaterImpl(this.dataModel, this.thesaurus, this);
    }

    @Override
    public void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void delete() {
        this.deleteAllTransitions();
        this.deleteAllStates();
        this.dataModel.remove(this);
    }

    private void deleteAllTransitions() {
        this.transitions.forEach(StateTransitionImpl::prepareDelete);
        this.transitions.clear();
    }

    private void deleteAllStates() {
        this.states.forEach(StateImpl::prepareDelete);
        this.states.clear();
    }

}