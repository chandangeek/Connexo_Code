package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME + "}")
public class UsagePointLifeCycleImpl implements UsagePointLifeCycle {

    public enum Fields {
        NAME("name"),
        OBSOLETE_TIME("obsoleteTime"),
        STATE_MACHINE("stateMachine"),
        TRANSITIONS("transitions");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH - 4, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}") // 4 is the length for 'UPL_' prefix for FSM
    private String name;
    @SuppressWarnings("unused")
    private Instant obsoleteTime;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    @Valid
    private List<UsagePointTransition> transitions = new ArrayList<>();

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UsagePointLifeCycleImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }

    @Override
    public List<UsagePointTransition> getTransitions() {
        return Collections.unmodifiableList(this.transitions);
    }

    @Override
    public List<UsagePointState> getStates() {
        return this.stateMachine.get().getStates()
                .stream()
                .map(state -> this.dataModel.getInstance(UsagePointStateImpl.class).init(this, state))
                .collect(Collectors.toList());
    }

    @Override
    public UsagePointState.UsagePointStateCreator newState(String name) {
        return this.dataModel.getInstance(UsagePointStateCreatorImpl.class).init(this, name);
    }

    @Override
    public UsagePointTransition.UsagePointTransitionCreator newTransition(String name, UsagePointState from, UsagePointState to) {
        return this.dataModel.getInstance(UsagePointTransitionCreatorImpl.class).init(this, name, from, to);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public long getId() {
        return this.id;
    }

    void setName(String name) {
        this.name = name;
        if (!Checks.is(this.name).emptyOrOnlyWhiteSpace()) {
            this.name = this.name.trim();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    FiniteStateMachine getStateMachine() {
        return this.stateMachine.get();
    }

    void setStateMachine(FiniteStateMachine stateMachine) {
        this.stateMachine.set(stateMachine);
    }

    void save() {
        if (getId() > 0) {
            Save.UPDATE.save(this.dataModel, this);
            this.eventService.postEvent(EventType.LIFE_CYCLE_UPDATED.topic(), this);
        } else {
            Save.CREATE.save(this.dataModel, this);
            this.eventService.postEvent(EventType.LIFE_CYCLE_CREATED.topic(), this);
        }
    }

    void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public void remove() {
        this.eventService.postEvent(EventType.LIFE_CYCLE_BEFORE_DELETE.topic(), this);
        this.dataModel.remove(this);
        this.eventService.postEvent(EventType.LIFE_CYCLE_DELETED.topic(), this);
    }

    void addTransition(UsagePointTransitionImpl transition) {
        Save.CREATE.validate(this.dataModel, transition);
        this.transitions.add(transition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointLifeCycleImpl that = (UsagePointLifeCycleImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
