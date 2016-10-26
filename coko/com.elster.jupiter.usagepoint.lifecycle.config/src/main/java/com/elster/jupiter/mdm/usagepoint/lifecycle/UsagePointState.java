package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Optional;

public interface UsagePointState extends HasId, HasName {
    boolean isInitial();

    List<ProcessReference> getOnEntryProcesses();

    List<ProcessReference> getOnExitProcesses();

    long getVersion();

    Optional<DefaultState> getDefaultState();

    boolean isDefault(DefaultState state);

    void remove();

    UsagePointStateUpdater startUpdate();

    interface UsagePointStateCreator<T extends UsagePointStateCreator> {

        T onEntry(StateChangeBusinessProcess process);

        T onExit(StateChangeBusinessProcess process);

        T setInitial();

        UsagePointState complete();
    }

    interface UsagePointStateUpdater extends UsagePointStateCreator<UsagePointStateUpdater> {
        UsagePointStateUpdater setName(String newName);

        UsagePointStateUpdater removeOnEntry(StateChangeBusinessProcess process);

        UsagePointStateUpdater removeOnExit(StateChangeBusinessProcess process);
    }
}
