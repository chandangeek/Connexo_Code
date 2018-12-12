/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UnderDualControl<T extends PendingUpdate> {

    /**
     * @return the dual control Monitor instance for this object
     */
    Monitor getMonitor();

    /**
     * @return optionally, the pending update instance
     */
    Optional<T> getPendingUpdate();

    /**
     * @param pendingUpdate instance containing the changes for approval
     */
    void setPendingUpdate(T pendingUpdate);

    /**
     * Called when full approval has been reached, the current pending change should be applied.
     * @PostCondition the current pending change is empty.
     */
    void applyUpdate();

    /**
     * Called when the current pending change is rejected, the current pending change must be discarded.
     * @PostCondition the current pending change is empty.
     */
    void clearUpdate();
}
