package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.fsm.State;

import java.util.Optional;

public interface UsagePointState extends State {
    Optional<DefaultState> getDefaultState();
}
