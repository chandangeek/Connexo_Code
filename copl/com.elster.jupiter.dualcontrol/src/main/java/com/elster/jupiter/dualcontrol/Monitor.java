/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol;

import java.util.List;

public interface Monitor {

    <T extends PendingUpdate> void request(T update, UnderDualControl<T> underDualControl);

    <T extends PendingUpdate> void approve(UnderDualControl<T> underDualControl);

    <T extends PendingUpdate> void reject(UnderDualControl<T> underDualControl);

    State getState();

    List<UserOperation> getOperations();

    long getId();

    boolean hasCurrentUserAccepted();
}
