package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.NotEmpty;
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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Predicates;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Clock;
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
        OBSOLETE_TIMESTAMP("obsoleteTimestamp"),
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
    private final Clock clock;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @Valid
    private List<StateImpl> states = new ArrayList<>();
    @Valid
    private List<StateTransitionImpl> transitions = new ArrayList<>();
    private Instant obsoleteTimestamp;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public FiniteStateMachineImpl(DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    public FiniteStateMachineImpl initialize(String name) {
        setName(name);
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
    public boolean isObsolete() {
        return this.obsoleteTimestamp != null;
    }

    @Override
    public Instant getObsoleteTimestamp() {
        return this.obsoleteTimestamp;
    }

    @Override
    public String getName() {
        return this.name;
    }

    void setName(String newName) {
        if (!Checks.is(newName).emptyOrOnlyWhiteSpace()){
            this.name = newName.trim();
        } else {
            this.name = null;
        }
    }

    @Override
    public List<State> getStates() {
        return this.states.stream().filter(Predicates.not(StateImpl::isObsolete)).collect(Collectors.toList());
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
                .filter(Predicates.not(StateImpl::isObsolete))
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

    void validateAndAdd(StateImpl state) {
        Save.CREATE.validate(this.dataModel, state);
        this.add(state);
    }

    void removeState(StateImpl obsoleteState) {
        this.removeObsoleteTransitions(obsoleteState);
        obsoleteState.makeObsolete();
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

    void validateAndAdd(StateTransitionImpl stateTransition) {
        Save.CREATE.validate(this.dataModel, stateTransition);
        this.add(stateTransition);
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
    public FiniteStateMachineUpdater startUpdate() {
        return new FiniteStateMachineUpdaterImpl(this.dataModel, this.thesaurus, this);
    }

    @Override
    public void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTimestamp = this.clock.instant();
        this.obsoleteAllStates();
        this.dataModel.update(this, Fields.OBSOLETE_TIMESTAMP.fieldName());
    }

    private void obsoleteAllStates() {
        this.states.forEach(StateImpl::makeObsolete);
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