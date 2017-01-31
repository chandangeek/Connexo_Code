/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointStateImpl implements UsagePointState {

    public enum Fields {
        LIFE_CYCLE("lifeCycle"),
        STATE("fsmState"),
        STAGE("stage"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final DataModel dataModel;

    private Reference<State> fsmState = ValueReference.absent();
    private Reference<UsagePointLifeCycleImpl> lifeCycle = ValueReference.absent();

    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private UsagePointStage.Key stage;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public UsagePointStateImpl(Thesaurus thesaurus,
                               EventService eventService,
                               DataModel dataModel) {
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    public UsagePointStateImpl init(UsagePointLifeCycleImpl lifeCycle, State fsmState, UsagePointStage.Key stage) {
        this.lifeCycle.set(lifeCycle);
        this.fsmState.set(fsmState);
        this.id = fsmState.getId();
        this.stage = stage;
        lifeCycle.addState(this);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public boolean isInitial() {
        return this.fsmState.get().isInitial();
    }

    @Override
    public String getName() {
        Optional<DefaultState> defaultState = getDefaultState();
        if (defaultState.isPresent()) {
            return this.thesaurus.getFormat(defaultState.get().getTranslation()).format();
        }
        return this.fsmState.get().getName();
    }

    @Override
    public List<ProcessReference> getOnEntryProcesses() {
        return this.fsmState.get().getOnEntryProcesses();
    }

    @Override
    public List<ProcessReference> getOnExitProcesses() {
        return this.fsmState.get().getOnExitProcesses();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public Optional<DefaultState> getDefaultState() {
        if (this.fsmState.get().isCustom()) {
            return Optional.empty();
        }
        return Stream.of(DefaultState.values())
                .filter(candidate -> candidate.getKey().equals(this.fsmState.get().getName()))
                .findFirst();
    }

    @Override
    public boolean isDefault(DefaultState state) {
        if (state == null || this.fsmState.get().isCustom()) {
            return false;
        }
        return state.getKey().equals(this.fsmState.get().getName());
    }

    @Override
    public void remove() {
        this.eventService.postEvent(EventType.LIFE_CYCLE_STATE_BEFORE_DELETE.topic(), this);
        List<UsagePointTransition> linkedTransitions = this.lifeCycle.get().getTransitions()
                .stream()
                .filter(transition -> transition.getFrom().getId() == getId()
                        || transition.getTo().getId() == getId())
                .collect(Collectors.toList());
        if (!linkedTransitions.isEmpty()) {
            throw UsagePointStateRemoveException.stateHasLinkedTransitions(this.thesaurus, linkedTransitions);
        }
        List<UsagePointState> allStates = this.lifeCycle.get().getStates();
        if (allStates.size() == 1 && allStates.contains(this)) {
            throw UsagePointStateRemoveException.stateIsTheLastState(this.thesaurus);
        }
        if (isInitial()) {
            throw UsagePointStateRemoveException.stateIsInitial(this.thesaurus);
        }
        this.fsmState.get().getFiniteStateMachine().startUpdate().removeState(this.fsmState.get()).complete();
        this.lifeCycle.get().removeState(this);
        this.eventService.postEvent(EventType.LIFE_CYCLE_STATE_DELETED.topic(), this);
    }

    @Override
    public UsagePointStateUpdater startUpdate() {
        return new UsagePointStateUpdaterImpl(this);
    }

    @Override
    public UsagePointLifeCycle getLifeCycle() {
        return this.lifeCycle.get();
    }

    @Override
    public UsagePointStage getStage() {
        return new UsagePointStageImpl(this.stage, this.thesaurus);
    }

    void setStage(UsagePointStage.Key stage) {
        this.stage = stage;
    }

    State getState() {
        return this.fsmState.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointStateImpl that = (UsagePointStateImpl) o;
        return this.fsmState != null ? this.fsmState.equals(that.fsmState) : that.fsmState == null;
    }

    @Override
    public int hashCode() {
        return this.fsmState != null ? this.fsmState.hashCode() : 0;
    }

    void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this);
        } else {
            Save.CREATE.save(this.dataModel, this);
        }
    }
}
