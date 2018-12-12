/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;

/**
 * Created by akuryuk on 27.04.2016.
 */
public interface CustomUsagePointMeterActivationValidator {
    boolean validateActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint);
}
