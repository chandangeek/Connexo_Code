package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
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
    private int levelBits;
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
    private EnumSet<MicroCheck> checks = EnumSet.noneOf(MicroCheck.class);
    private EnumSet<MicroAction> actions = EnumSet.noneOf(MicroAction.class);

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
        postLoadLevelEnumSet();
        this.postLoadChecksEnumSet();
        this.postLoadActionsEnumSet();
    }

    private void postLoadLevelEnumSet() {
        int mask = 1;
        for (Level level : Level.values()) {
            if ((this.levelBits & mask) != 0) {
                // The bit corresponding to the current level is set so add it to the set.
                this.levels.add(level);
            }
            mask = mask << 1;
        }
    }

    private void postLoadChecksEnumSet() {
        int mask = 1;
        for (MicroCheck microCheck : MicroCheck.values()) {
            if ((this.checkBits & mask) != 0) {
                // The bit corresponding to the current microCheck is set so add it to the set.
                this.checks.add(microCheck);
            }
            mask = mask << 1;
        }
    }

    private void postLoadActionsEnumSet() {
        int mask = 1;
        for (MicroAction microAction : MicroAction.values()) {
            if ((this.actionBits & mask) != 0) {
                // The bit corresponding to the current microAction is set so add it to the set.
                this.actions.add(microAction);
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
    public Set<Level> getLevels() {
        return Collections.unmodifiableSet(this.levels);
    }
}
