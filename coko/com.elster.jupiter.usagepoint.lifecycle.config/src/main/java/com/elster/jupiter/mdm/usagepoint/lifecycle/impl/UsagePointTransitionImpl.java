package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroActionFactory;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroCheckFactory;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
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
import java.util.Optional;
import java.util.Set;

@Unique(message = "{" + MessageSeeds.Keys.TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE + "}")
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
    private Reference<UsagePointLifeCycle> lifeCycle = ValueReference.absent();
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

    private EnumSet<Level> levels = EnumSet.noneOf(Level.class);
    private Set<MicroAction> microActions;
    private Set<MicroCheck> microChecks;

    private final UsagePointMicroActionFactory microActionFactory;
    private final UsagePointMicroCheckFactory microCheckFactory;

    @Inject
    public UsagePointTransitionImpl(UsagePointMicroActionFactory microActionFactory, UsagePointMicroCheckFactory microCheckFactory) {
        this.microActionFactory = microActionFactory;
        this.microCheckFactory = microCheckFactory;
    }

    UsagePointTransitionImpl init(UsagePointLifeCycle lifeCycle, String name) {
        this.lifeCycle.set(lifeCycle);
        this.name = name;
        return this;
    }

    void setTransition(StateTransition fsmTransition) {
        this.fsmTransition.set(fsmTransition);
    }

    @Override
    public void postLoad() {
        postLoadLevel();
        postLoadChecks();
        postLoadActions();
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
                // The bit corresponding to the current microCheck is set so add it to the set.
                this.microChecks.add(this.microCheckFactory.from(key));
            }
            mask = mask << 1;
        }
    }

    private void postLoadActions() {
        this.microActions = new HashSet<>();
        int mask = 1;
        for (MicroAction.Key key : MicroAction.Key.values()) {
            if ((this.actionBits & mask) != 0) {
                // The bit corresponding to the current microAction is set so add it to the set.
                this.microActions.add(this.microActionFactory.from(key));
            }
            mask = mask << 1;
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
        return this.lifeCycle.get().getStates()
                .stream()
                .filter(state -> state.getId() == this.fsmTransition.get().getFrom().getId())
                .findFirst()
                .get();
    }

    @Override
    public UsagePointState getTo() {
        return this.lifeCycle.get().getStates()
                .stream()
                .filter(state -> state.getId() == this.fsmTransition.get().getTo().getId())
                .findFirst()
                .get();
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

    void setLevels(Set<UsagePointTransition.Level> transitionLevels) {
        this.levelBits = 0L;
        for (UsagePointTransition.Level level : transitionLevels) {
            this.levelBits |= (1L << level.ordinal());
        }
        postLoadLevel();
    }

    void setMicroChecks(Set<MicroCheck.Key> microCheckKeys) {
        this.checkBits = 0L;
        for (MicroCheck.Key key : microCheckKeys) {
            this.checkBits |= (1L << key.ordinal());
        }
        postLoadChecks();
    }

    void setMicroActions(Set<MicroAction.Key> microActionKeys) {
        this.actionBits = 0L;
        for (MicroAction.Key key : microActionKeys) {
            this.actionBits |= (1L << key.ordinal());
        }
        postLoadActions();
    }
}
