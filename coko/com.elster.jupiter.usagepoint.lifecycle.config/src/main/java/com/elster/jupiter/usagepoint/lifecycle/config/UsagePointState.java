/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointState extends HasId, HasName {

    boolean isInitial();

    List<ProcessReference> getOnEntryProcesses();

    List<ProcessReference> getOnExitProcesses();

    Optional<DefaultState> getDefaultState();

    boolean isDefault(DefaultState state);

    long getVersion();

    void remove();

    UsagePointStateUpdater startUpdate();

    UsagePointLifeCycle getLifeCycle();

    UsagePointStage getStage();

    @ProviderType
    interface UsagePointStateCreator<T extends UsagePointStateCreator> {

        T onEntry(StateChangeBusinessProcess process);

        T onExit(StateChangeBusinessProcess process);

        T setInitial();

        T setStage(UsagePointStage.Key stage);

        UsagePointState complete();
    }

    @ProviderType
    interface UsagePointStateUpdater extends UsagePointStateCreator<UsagePointStateUpdater> {
        UsagePointStateUpdater setName(String newName);

        UsagePointStateUpdater removeOnEntry(StateChangeBusinessProcess process);

        UsagePointStateUpdater removeOnExit(StateChangeBusinessProcess process);
    }
}
