/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import java.time.Instant;

/**
 * @author sva
 * @since 7/04/2016 - 11:06
 */
public interface ActivatedBreakerStatus extends Effectivity {

    long getId();

    Device getDevice();

    BreakerStatus getBreakerStatus();

    Instant getLastChecked();

    void setLastChecked(Instant lastChecked);

    void save();
}
