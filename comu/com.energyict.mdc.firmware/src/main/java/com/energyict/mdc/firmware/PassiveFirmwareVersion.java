/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public interface PassiveFirmwareVersion extends Effectivity {

    long getId();

    Device getDevice();

    FirmwareVersion getFirmwareVersion();

    Instant getLastChecked();

    void setLastChecked(Instant lastChecked);

    Instant getActivateDate();

    void setActivateDate(Instant activateDate);

    void save();
}
