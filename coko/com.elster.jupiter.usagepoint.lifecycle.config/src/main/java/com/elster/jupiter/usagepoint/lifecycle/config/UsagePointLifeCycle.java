/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointLifeCycle extends HasId, HasName {

    Optional<Instant> getObsoleteTime();

    default boolean isObsolete() {
        return getObsoleteTime().isPresent();
    }

    void setName(String name);

    void save();

    void remove();

    List<UsagePointTransition> getTransitions();

    List<State> getStates();

    FiniteStateMachineUpdater getUpdater();

    UsagePointTransition.UsagePointTransitionCreator newTransition(String name, State from, State to);

    boolean isDefault();

    void markAsDefault();

    long getVersion();

    void removeState(State state);
}
