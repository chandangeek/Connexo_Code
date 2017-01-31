/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Models the information provided by and collected from a device
 * that constitute its address via one {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:26)
 */
public interface CollectedAddressProperties extends CollectedData {

    /**
     * Gets the {@link DeviceIdentifier} that uniquely identifies
     * the device for which address properties were collected.
     *
     * @return The DeviceIdentifier
     */
    public DeviceIdentifier getDeviceIdentifier ();

}