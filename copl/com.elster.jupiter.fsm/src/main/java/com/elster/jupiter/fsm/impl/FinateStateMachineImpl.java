package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.impl.constraints.AtLeastOneState;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides an implementation for the {@link FinateStateMachine} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:29)
 */
@Unique(message = MessageSeeds.Keys.UNIQUE_FINATE_STATE_MACHINE_NAME, groups = { Save.Create.class, Save.Update.class })
@AtLeastOneState(message = MessageSeeds.Keys.AT_LEAST_ONE_STATE, groups = { Save.Create.class, Save.Update.class })
public class FinateStateMachineImpl implements FinateStateMachine {

    public enum Fields {
        NAME("name"),
        TOPIC("topic"),
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

    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String topic;
    @Valid
    private List<State> states = new ArrayList<>();
    @Valid
    private List<StateTransition> transitions = new ArrayList<>();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public FinateStateMachineImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FinateStateMachineImpl initialize(String name, String topic) {
        this.name = name;
        this.topic = topic;
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
    public String getTopic() {
        return this.topic;
    }

    void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public List<State> getStates() {
        return Collections.unmodifiableList(this.states);
    }

    @Override
    public Optional<State> getState(String name) {
        return this.states.stream().filter(s -> name.equals(s.getName())).findFirst();
    }

    void add(State state) {
        this.states.add(state);
    }

    @Override
    public List<StateTransition> getTransitions() {
        return Collections.unmodifiableList(this.transitions);
    }

    void add(StateTransition stateTransition) {
        this.transitions.add(stateTransition);
    }

    @Override
    public FinateStateMachineUpdater update() {
        return new FinateStateMachineUpdaterImpl(this.dataModel, this);
    }

    @Override
    public void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }

}