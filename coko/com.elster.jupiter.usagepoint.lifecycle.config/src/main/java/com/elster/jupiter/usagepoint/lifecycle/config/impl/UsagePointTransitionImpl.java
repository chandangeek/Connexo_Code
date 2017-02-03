/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Unique(message = "{" + MessageSeeds.Keys.TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE + "}")
@HasDifferentStates(message = "{" + MessageSeeds.Keys.TRANSITION_FROM_AND_TO_ARE_THE_SAME + "}")
public class UsagePointTransitionImpl implements UsagePointTransition, PersistenceAware {

    public enum Fields {
        NAME("name"),
        LIFE_CYCLE("lifeCycle"),
        FSM_TRANSITION("fsmTransition"),
        LEVELS("levelBits"),
        CHECKS("microCheckUsages"),
        ACTIONS("microActionUsages"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointLifeCycleImpl> lifeCycle = ValueReference.absent();
    private Reference<StateTransition> fsmTransition = ValueReference.absent();

    private long levelBits;

    private List<UsagePointTransitionMicroCheckUsageImpl> microCheckUsages = new ArrayList<>();

    private List<UsagePointTransitionMicroActionUsageImpl> microActionUsages = new ArrayList<>();

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

    private final DataModel dataModel;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public UsagePointTransitionImpl(DataModel dataModel, UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
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
        return DecoratedStream.decorate(this.microActionUsages.stream())
                .distinct(UsagePointTransitionMicroActionUsageImpl::getKey)
                .map(UsagePointTransitionMicroActionUsageImpl::getAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<MicroCheck> getChecks() {
        return DecoratedStream.decorate(this.microCheckUsages.stream())
                .distinct(UsagePointTransitionMicroCheckUsageImpl::getKey)
                .map(UsagePointTransitionMicroCheckUsageImpl::getCheck)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Level> getLevels() {
        return Collections.unmodifiableSet(this.levels);
    }

    @Override
    public void remove() {
        this.microCheckUsages.clear();
        this.microActionUsages.clear();
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
    public void doTransition(String sourceId, String sourceType, Instant transitionTime, Map<String, Object> properties) {
        Optional<CustomStateTransitionEventType> eventType = this.fsmTransition.getOptional()
                .map(StateTransition::getEventType)
                .filter(et -> et instanceof CustomStateTransitionEventType)
                .map(CustomStateTransitionEventType.class::cast);
        if (eventType.isPresent()) {
            eventType.get().newInstance(this.lifeCycle.get().getStateMachine(), sourceId, sourceType,
                    this.fsmTransition.get().getFrom().getName(), transitionTime, properties).publish();
        }
    }

    @Override
    public List<PropertySpec> getMicroActionsProperties() {
        return DecoratedStream.decorate(this.getActions().stream())
                .flatMap(microAction -> microAction.getPropertySpecs().stream())
                .distinct(PropertySpec::getName)
                .collect(Collectors.toList());
    }

    void setLevels(Set<UsagePointTransition.Level> transitionLevels) {
        this.levelBits = 0L;
        this.levels.clear();
        if (transitionLevels != null) {
            for (UsagePointTransition.Level level : transitionLevels) {
                this.levelBits |= (1L << level.ordinal());
            }
        }
        postLoadLevel();
    }

    void setMicroChecks(Set<String> microCheckKeys) {
        this.microCheckUsages.clear();
        if (microCheckKeys != null) {
            microCheckKeys.stream()
                    .map(this.usagePointLifeCycleConfigurationService::getMicroCheckByKey)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(check -> this.dataModel.getInstance(UsagePointTransitionMicroCheckUsageImpl.class).init(this, check))
                    .forEach(this.microCheckUsages::add);
        }
    }

    void setMicroActions(Set<String> microActionKeys) {
        this.microActionUsages.clear();
        if (microActionKeys != null) {
            microActionKeys.stream()
                    .map(this.usagePointLifeCycleConfigurationService::getMicroActionByKey)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(action -> this.dataModel.getInstance(UsagePointTransitionMicroActionUsageImpl.class).init(this, action))
                    .forEach(this.microActionUsages::add);
        }
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
