/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptional situation where someone tries to change the configuration of a device
 * while there are still unresolved conflicts on the DeviceType
 */
public class CannotChangeDeviceConfigStillUnresolvedConflicts extends LocalizedException {

    public CannotChangeDeviceConfigStillUnresolvedConflicts(Thesaurus thesaurus, Device device, DeviceConfiguration destinationDeviceConfiguration) {
        super(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_NOT_ALL_CONFLICTS_SOLVED, device, destinationDeviceConfiguration);
    }
}
