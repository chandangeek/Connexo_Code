package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.PendingUpdate;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UnderDualControl;
import com.elster.jupiter.dualcontrol.UserOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonitorImpl implements Monitor {

    private State state;

    private List<UserOperationImpl> userOperations = new ArrayList<>();

    @Override
    public <T extends PendingUpdate> void request(T update, UnderDualControl<T> underDualControl) {

    }

    @Override
    public <T extends PendingUpdate> void approve(UnderDualControl<T> underDualControl) {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public <T extends PendingUpdate> void reject(UnderDualControl<T> underDualControl) {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public List<UserOperation> getUserOperations() {
        return Collections.unmodifiableList(userOperations);
    }
}
