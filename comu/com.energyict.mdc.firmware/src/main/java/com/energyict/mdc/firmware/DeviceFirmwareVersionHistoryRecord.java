/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;

/**
 * Represents an {@link ActivatedFirmwareVersion} as part of a device's firmware history
 * Date: 1/04/2016
 * Time: 14:14
 */
public interface DeviceFirmwareVersionHistoryRecord extends Effectivity {

    Device getDevice();
    FirmwareVersion getFirmwareVersion();
    Instant getLastChecked();

}
