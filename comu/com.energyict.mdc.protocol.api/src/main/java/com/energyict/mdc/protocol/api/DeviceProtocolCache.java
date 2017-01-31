/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import java.io.Serializable;

/**
 * Defines a <i>object</i> that represents a generic cache for a {@link DeviceProtocol}.
 */
public interface DeviceProtocolCache extends Serializable {

    /**
     * Indicates that the content of this object changed during a communication session with a device.
     * The ComServer will use this when deciding to update the content in the DataBase or not.
     *
     * @return true if the content changed, false otherwise
     */
    public boolean isDirty();

    public void markClean();

    public void markDirty();

}