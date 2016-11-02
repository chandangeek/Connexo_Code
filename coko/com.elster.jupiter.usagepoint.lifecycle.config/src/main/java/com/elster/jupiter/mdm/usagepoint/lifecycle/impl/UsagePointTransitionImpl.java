package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Unique(message = "{" + MessageSeeds.Keys.TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE + "}")
@HasDifferentStates(message = "{" + MessageSeeds.Keys.TRANSITION_FROM_AND_TO_ARE_THE_SAME + "}")
public class UsagePointTransitionImpl implements UsagePointTransition, PersistenceAware {

    public enum Fields {
        // Common fields
        NAME("name"),
        LIFE_CYCLE("lifeCycle"),
        FSM_TRANSITION("fsmTransition"),
        LEVELS("levelBits"),
        CHECKS("checkBits"),
        ACTIONS("actionBits"),;

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
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointLifeCycleImpl> lifeCycle = ValueReference.absent();
    private Reference<StateTransition> fsmTransition = ValueReference.absent();
    @SuppressWarnings("unused")
    private long levelBits;
    @SuppressWarnings("unused")
    private long checkBits;
    @SuppressWarnings("unused")
    private long actionBits;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private UsagePointState fromState;
    private UsagePointState toState;
    private EnumSet<Level> levels = EnumSet.noneOf(Level.class);
    private Set<MicroAction> microActions;
    private Set<MicroCheck> microChecks;

    private final DataModel dataModel;
    private final UsagePointLifeCycleService usagePointLifeCycleService;

    @Inject
    public UsagePointTransitionImpl(DataModel dataModel, UsagePointLifeCycleService usagePointLifeCycleService) {
        this.dataModel = dataModel;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    UsagePointTransitionImpl init(UsagePointLifeCycleImpl lifeCycle, String name, UsagePointState fromState, UsagePointState toState) {
        this.lifeCycle.set(lifeCycle);
        this.name = name;
        this.fromState = fromState;
        this.toState = toState;
        return this;
    }

    @Override
    public void postLoad() {
        postLoadLevel();
        postLoadChecks();
        postLoadActions();
        postLoadStates();
    }

    private void postLoadLevel() {
        int mask = 1;
        for (Level level : Level.values()) {
            if ((this.levelBits & mask) != 0) {
                // The bit corresponding to the current level is set so add it to the set.
                this.levels.add(level);
            }
            mask = mask << 1;
        }
    }

    private void postLoadChecks() {
        this.microChecks = new HashSet<>();
        int mask = 1;
        for (MicroCheck.Key key : MicroCheck.Key.values()) {
            if ((this.checkBits & mask) != 0) {
                this.microChecks.add(this.usagePointLifeCycleService.getMicroCheckByKey(key));
            }
            mask = mask << 1;
        }
    }

    private void postLoadActions() {
        this.microActions = new HashSet<>();
        int mask = 1;
        for (MicroAction.Key key : MicroAction.Key.values()) {
            if ((this.actionBits & mask) != 0) {
                this.microActions.add(this.usagePointLifeCycleService.getMicroActionByKey(key));
            }
            mask = mask << 1;
        }
    }

    private void postLoadStates() {
        long fromId = this.fsmTransition.get().getFrom().getId();
        long toId = this.fsmTransition.get().getTo().getId();
        List<UsagePointState> states = this.lifeCycle.get().getStates();
        for (int i = 0; i < states.size() && (this.fromState == null || this.toState == null); i++) {
            UsagePointState state = states.get(i);
            if (state.getId() == fromId) {
                this.fromState = state;
            } else if (state.getId() == toId) {
                this.toState = state;
            }
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public UsagePointState getFrom() {
        return this.fromState;
    }

    @Override
    public UsagePointState getTo() {
        return this.toState;
    }

    @Override
    public Optional<StandardStateTransitionEventType> getTriggeredBy() {
        return this.fsmTransition.getOptional()
                .map(StateTransition::getEventType)
                .filter(et -> et instanceof StandardStateTransitionEventType)
                .map(StandardStateTransitionEventType.class::cast);
    }

    @Override
    public UsagePointLifeCycle getLifeCycle() {
        return this.lifeCycle.get();
    }

    @Override
    public Set<MicroAction> getActions() {
        return Collections.unmodifiableSet(this.microActions);
    }

    @Override
    public Set<MicroCheck> getChecks() {
        return Collections.unmodifiableSet(this.microChecks);
    }

    @Override
    public Set<Level> getLevels() {
        return Collections.unmodifiableSet(this.levels);
    }

    @Override
    public void remove() {
        this.lifeCycle.get().removeTransition(this);
        StateTransition stateTransition = this.fsmTransition.get();
        State fsmFromState = stateTransition.getFrom();
        StateTransitionEventType eventType = stateTransition.getEventType();
        FiniteStateMachineUpdater stateMachineUpdater = fsmFromState.getFiniteStateMachine().startUpdate();
        stateMachineUpdater.state(fsmFromState.getId()).prohibit(eventType).complete();
        stateMachineUpdater.complete();
        if (eventType instanceof CustomStateTransitionEventType) {
            eventType.delete();
        }
    }

    @Override
    public UsagePointTransitionUpdater startUpdate() {
        return this.dataModel.getInstance(UsagePointTransitionUpdaterImpl.class).init(this.lifeCycle.get(), this);
    }

    @Override
    public void performTransition(UsagePoint usagePoint, Map<String, Object> properties, Instant transitionTime) {
        Optional<CustomStateTransitionEventType> eventType = this.fsmTransition.getOptional()
                .map(StateTransition::getEventType)
                .filter(et -> et instanceof CustomStateTransitionEventType)
                .map(CustomStateTransitionEventType.class::cast);
        if (eventType.isPresent()) {
            eventType.get().newInstance(this.lifeCycle.get().getStateMachine(), usagePoint.getMRID(), UsagePoint.class.getName(),
                    this.fsmTransition.get().getFrom().getName(), transitionTime, properties).publish();
        }
        // TODO throw ex?
    }

    void setLevels(Set<UsagePointTransition.Level> transitionLevels) {
        this.levelBits = 0L;
        if (transitionLevels != null) {
            for (UsagePointTransition.Level level : transitionLevels) {
                this.levelBits |= (1L << level.ordinal());
            }
        }
        postLoadLevel();
    }

    void setMicroChecks(Set<MicroCheck.Key> microCheckKeys) {
        this.checkBits = 0L;
        if (microCheckKeys != null) {
            for (MicroCheck.Key key : microCheckKeys) {
                this.checkBits |= (1L << key.ordinal());
            }
        }
        postLoadChecks();
    }

    void setMicroActions(Set<MicroAction.Key> microActionKeys) {
        this.actionBits = 0L;
        if (microActionKeys != null) {
            for (MicroAction.Key key : microActionKeys) {
                this.actionBits |= (1L << key.ordinal());
            }
        }
        postLoadActions();
    }

    StateTransition getFsmTransition() {
        return this.fsmTransition.get();
    }

    void setFsmTransition(StateTransition fsmTransition) {
        this.fsmTransition.set(fsmTransition);
        if (fsmTransition != null) {
            postLoadStates();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointTransitionImpl that = (UsagePointTransitionImpl) o;
        return this.id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (this.id ^ (this.id >>> 32));
    }
}
