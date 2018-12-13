/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.data.Device;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link com.energyict.mdc.upl.meterdata.Device device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:50)
 */
@ProviderType
public interface DeviceRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link com.energyict.mdc.upl.meterdata.Device device}.
     *
     * @return The device
     */
    Device getDevice();

}