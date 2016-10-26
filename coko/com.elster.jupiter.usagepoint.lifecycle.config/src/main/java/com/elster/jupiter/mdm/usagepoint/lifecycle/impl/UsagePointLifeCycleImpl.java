package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
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

    @Inject
    public UsagePointLifeCycleImpl(DataModel dataModel) {
        this.dataModel = dataModel;
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
                .map(state -> this.dataModel.getInstance(UsagePointStateImpl.class).init(state))
                .collect(Collectors.toList());
    }

    @Override
    public UsagePointState.UsagePointStateCreator newState(String name) {
        return new UsagePointStateCreatorImpl(this.dataModel, this, name);
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

    Optional<FiniteStateMachine> getStateMachine() {
        return this.stateMachine.getOptional();
    }

    void setStateMachine(FiniteStateMachine stateMachine) {
        this.stateMachine.set(stateMachine);
    }

    void save() {
        if (getId() > 0) {
            Save.UPDATE.save(this.dataModel, this);
        } else {
            Save.CREATE.save(this.dataModel, this);
        }
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Method is not implemented yet."); // TODO
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
