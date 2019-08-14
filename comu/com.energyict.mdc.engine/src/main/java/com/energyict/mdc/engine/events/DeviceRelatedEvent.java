/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;

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