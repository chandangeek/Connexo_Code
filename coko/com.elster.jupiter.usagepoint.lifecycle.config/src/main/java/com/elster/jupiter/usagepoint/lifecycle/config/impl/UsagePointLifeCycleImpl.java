/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.Checks;

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

import static com.elster.jupiter.util.conditions.Where.where;

@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME + "}")
public class UsagePointLifeCycleImpl implements UsagePointLifeCycle {

    public enum Fields {
        NAME("name"),
        OBSOLETE_TIME("obsoleteTime"),
        STATE_MACHINE("stateMachine"),
        TRANSITIONS("transitions"),
        DEFAULT("isDefault");

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
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.TEXT_FEILD_CHARS)
    private String name;
    @SuppressWarnings("unused")
    private Instant obsoleteTime;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    @Valid
    private List<UsagePointTransitionImpl> transitions = new ArrayList<>();
    private boolean isDefault;

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
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleImpl(DataModel dataModel, EventService eventService, Clock clock, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
        this.thesaurus = thesaurus;
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
    public List<State> getStates() {
        return Collections.unmodifiableList(this.stateMachine.get().getStates());
    }

    @Override
    public FiniteStateMachineUpdater getUpdater() {
        return stateMachine.get().startUpdate();

    }

    @Override
    public UsagePointTransition.UsagePointTransitionCreator newTransition(String name, State from, State to) {
        return this.dataModel.getInstance(UsagePointTransitionCreatorImpl.class).init(this, name, from, to);
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public void markAsDefault() {
        this.isDefault = true;
        this.dataModel.query(UsagePointLifeCycleImpl.class).select(where(Fields.DEFAULT.fieldName()).isEqualTo(true))
                .forEach(lifeCycle -> {
                    lifeCycle.isDefault = false;
                    lifeCycle.save();
                });
        save();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (!Checks.is(this.name).emptyOrOnlyWhiteSpace()) {
            this.name = this.name.trim();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    public FiniteStateMachine getStateMachine() {
        return this.stateMachine.get();
    }

    public void setStateMachine(FiniteStateMachine stateMachine) {
        this.stateMachine.set(stateMachine);
    }

    @Override
    public void save() {
        if (getId() > 0) {
            Save.UPDATE.save(this.dataModel, this);
            this.eventService.postEvent(EventType.LIFE_CYCLE_UPDATED.topic(), this);
        } else {
            Save.CREATE.save(this.dataModel, this);
            this.eventService.postEvent(EventType.LIFE_CYCLE_CREATED.topic(), this);
        }
    }

    void touch() {
        if (getId() > 0) {
            this.dataModel.touch(this);
        }
    }

    @Override
    public void remove() {
        if (getId() > 0) {
            if (this.isDefault) {
                throw UsagePointLifeCycleRemoveException.lifeCycleIsDefault(this.thesaurus);
            }
            this.eventService.postEvent(EventType.LIFE_CYCLE_BEFORE_DELETE.topic(), this);
            new ArrayList<>(this.transitions).forEach(UsagePointTransitionImpl::remove); // can't just clear due to event type
            //update fsm
            this.stateMachine.get().startUpdate().complete();
            this.obsoleteTime = this.clock.instant();
            this.save();
            this.stateMachine.get().makeObsolete();
            this.eventService.postEvent(EventType.LIFE_CYCLE_DELETED.topic(), this);
        }
    }

    void addState(String name, Stage stage) {
        this.getStateMachine().startUpdate().newCustomState(name, stage).complete();
    }

    @Override
    public void removeState(State state) {
        this.eventService.postEvent(EventType.LIFE_CYCLE_STATE_BEFORE_DELETE.topic(), state);
        List<UsagePointTransition> linkedTransitions = this.getTransitions()
                .stream()
                .filter(transition -> transition.getFrom().getId() == state.getId()
                        || transition.getTo().getId() == state.getId())
                .collect(Collectors.toList());
        if (!linkedTransitions.isEmpty()) {
            throw UsagePointStateRemoveException.stateHasLinkedTransitions(this.thesaurus, linkedTransitions);
        }
        if (getStates().size() == 1 && getStates().contains(state)) {
            throw UsagePointStateRemoveException.stateIsTheLastState(this.thesaurus);
        }
        if (stateMachine.get().getInitialState().equals(state)) {
            throw UsagePointStateRemoveException.stateIsInitial(this.thesaurus);
        }
        this.getStateMachine().startUpdate().removeState(state).complete();
        this.eventService.postEvent(EventType.LIFE_CYCLE_STATE_DELETED.topic(), state);
        touch();
    }

    void addTransition(UsagePointTransitionImpl transition) {
        Save.CREATE.validate(this.dataModel, transition);
        this.transitions.add(transition);
    }

    void removeTransition(UsagePointTransitionImpl transition) {
        this.transitions.remove(transition);
        touch();
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
