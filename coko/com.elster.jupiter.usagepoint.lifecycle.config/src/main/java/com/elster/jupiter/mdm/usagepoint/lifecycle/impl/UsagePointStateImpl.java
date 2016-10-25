package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class UsagePointStateImpl implements UsagePointState {
    private final Thesaurus thesaurus;
    private State delegate;

    @Inject
    public UsagePointStateImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public UsagePointState init(State delegate) {
        this.delegate = delegate;
        return this;
    }

    @Override
    public long getId() {
        return this.delegate.getId();
    }

    @Override
    public boolean isInitial() {
        return this.delegate.isInitial();
    }

    @Override
    public boolean isCustom() {
        return this.delegate.isCustom();
    }

    @Override
    public String getName() {
        Optional<DefaultState> defaultState = getDefaultState();
        if (defaultState.isPresent()) {
            return this.thesaurus.getFormat(defaultState.get().getTranslation()).format();
        }
        return this.delegate.getName();
    }

    @Override
    public FiniteStateMachine getFiniteStateMachine() {
        return this.delegate.getFiniteStateMachine();
    }

    @Override
    public List<StateTransition> getOutgoingStateTransitions() {
        return this.delegate.getOutgoingStateTransitions();
    }

    @Override
    public List<ProcessReference> getOnEntryProcesses() {
        return this.delegate.getOnEntryProcesses();
    }

    @Override
    public List<ProcessReference> getOnExitProcesses() {
        return this.delegate.getOnExitProcesses();
    }

    @Override
    public long getVersion() {
        return this.delegate.getVersion();
    }

    @Override
    public Instant getCreationTimestamp() {
        return this.delegate.getCreationTimestamp();
    }

    @Override
    public Instant getModifiedTimestamp() {
        return this.delegate.getModifiedTimestamp();
    }

    @Override
    public Optional<DefaultState> getDefaultState() {
        if (isCustom()) {
            return Optional.empty();
        }
        return Stream.of(DefaultState.values())
                .filter(candidate -> candidate.getKey().equals(this.delegate.getName()))
                .findFirst();
    }
}
