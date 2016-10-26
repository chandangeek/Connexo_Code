package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
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
    public String getName() {
        Optional<DefaultState> defaultState = getDefaultState();
        if (defaultState.isPresent()) {
            return this.thesaurus.getFormat(defaultState.get().getTranslation()).format();
        }
        return this.delegate.getName();
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
    public Optional<DefaultState> getDefaultState() {
        if (this.delegate.isCustom()) {
            return Optional.empty();
        }
        return Stream.of(DefaultState.values())
                .filter(candidate -> candidate.getKey().equals(this.delegate.getName()))
                .findFirst();
    }

    @Override
    public boolean isDefault(DefaultState state) {
        if (state == null || this.delegate.isCustom()) {
            return false;
        }
        return this.delegate.getName().equals(state.getKey());
    }

    @Override
    public void remove() {
        this.delegate.getFiniteStateMachine().startUpdate().removeState(this.delegate);
    }

    @Override
    public UsagePointStateUpdater startUpdate() {
        return new UsagePointStateUpdaterImpl(this);
    }

    State getState() {
        return this.delegate;
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
        return this.delegate != null ? this.delegate.equals(that.delegate) : that.delegate == null;
    }

    @Override
    public int hashCode() {
        return this.delegate != null ? this.delegate.hashCode() : 0;
    }
}
