/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import java.util.List;

public interface FindMultipleDevices<T extends BaseDevice< ? extends BaseChannel, ? extends BaseLoadProfile<?  extends BaseChannel>, ? extends  BaseRegister>> extends DeviceIdentifier {

    /**
     * <b>ALL</b> Devices which satisfy the criteria of the DeviceIdentifier.
     * This should only be used in exceptional situations when we know
     * duplicates exist.
     *
     * @return <b>ALL</b> Devices which satisfy the criteria of the DeviceIdentifier
     */
    public List<T> getAllDevices();

}