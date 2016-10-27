package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsagePointLifeCycle extends HasId, HasName {

    Optional<Instant> getObsoleteTime();

    default boolean isObsolete() {
        return getObsoleteTime().isPresent();
    }

    void remove();

    List<UsagePointTransition> getTransitions();

    List<UsagePointState> getStates();

    UsagePointState.UsagePointStateCreator newState(String name);

    UsagePointTransition.UsagePointTransitionCreator newTransition(String name, UsagePointState from, UsagePointState to);

    long getVersion();
}
