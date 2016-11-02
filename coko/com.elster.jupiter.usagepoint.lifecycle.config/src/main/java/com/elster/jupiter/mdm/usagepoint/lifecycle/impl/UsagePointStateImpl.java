package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointStateImpl implements UsagePointState {
    private final Thesaurus thesaurus;
    private State fsmState;
    private UsagePointLifeCycle lifeCycle;

    @Inject
    public UsagePointStateImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public UsagePointState init(UsagePointLifeCycle lifeCycle, State fsmState) {
        this.lifeCycle = lifeCycle;
        this.fsmState = fsmState;
        return this;
    }

    @Override
    public long getId() {
        return this.fsmState.getId();
    }

    @Override
    public boolean isInitial() {
        return this.fsmState.isInitial();
    }

    @Override
    public String getName() {
        Optional<DefaultState> defaultState = getDefaultState();
        if (defaultState.isPresent()) {
            return this.thesaurus.getFormat(defaultState.get().getTranslation()).format();
        }
        return this.fsmState.getName();
    }

    @Override
    public List<ProcessReference> getOnEntryProcesses() {
        return this.fsmState.getOnEntryProcesses();
    }

    @Override
    public List<ProcessReference> getOnExitProcesses() {
        return this.fsmState.getOnExitProcesses();
    }

    @Override
    public long getVersion() {
        return this.fsmState.getVersion();
    }

    @Override
    public Optional<DefaultState> getDefaultState() {
        if (this.fsmState.isCustom()) {
            return Optional.empty();
        }
        return Stream.of(DefaultState.values())
                .filter(candidate -> candidate.getKey().equals(this.fsmState.getName()))
                .findFirst();
    }

    @Override
    public boolean isDefault(DefaultState state) {
        if (state == null || this.fsmState.isCustom()) {
            return false;
        }
        return state.getKey().equals(this.fsmState.getName());
    }

    @Override
    public void remove() {
        List<UsagePointTransition> linkedTransitions = this.lifeCycle.getTransitions()
                .stream()
                .filter(transition -> transition.getFrom().getId() == getId()
                        || transition.getTo().getId() == getId())
                .collect(Collectors.toList());
        if (!linkedTransitions.isEmpty()) {
            throw UsagePointStateRemoveException.stateHasLinkedTransitions(this.thesaurus, linkedTransitions);
        }
        List<UsagePointState> allStates = this.lifeCycle.getStates();
        if (allStates.size() == 1 && allStates.contains(this)) {
            throw UsagePointStateRemoveException.stateIsTheLastState(this.thesaurus);
        }
        if (isInitial()) {
            throw UsagePointStateRemoveException.stateIsInitial(this.thesaurus);
        }
        this.fsmState.getFiniteStateMachine().startUpdate().removeState(this.fsmState).complete();
    }

    @Override
    public UsagePointStateUpdater startUpdate() {
        return new UsagePointStateUpdaterImpl(this);
    }

    State getState() {
        return this.fsmState;
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
}
