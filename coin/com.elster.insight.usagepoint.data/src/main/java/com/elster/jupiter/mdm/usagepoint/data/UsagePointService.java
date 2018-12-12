/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointService {

    UsagePointValidation forValidation(UsagePoint usagePoint);

    UsagePointEstimation forEstimation(UsagePoint usagePoint);
}
