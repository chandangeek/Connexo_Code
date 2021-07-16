/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.obis.ObisCode;

import java.time.Instant;

/**
 * @author sva
 * @since 7/04/2016 - 11:06
 */
public interface ActivatedBreakerStatus extends Effectivity {
   ObisCode BREAKER_STATUS_OBIS_CODE = ObisCode.fromString("0.0.96.3.10.255");

    long getId();

    Device getDevice();

    BreakerStatus getBreakerStatus();

    Instant getLastChecked();

    void setLastChecked(Instant lastChecked);

    void save();
}
