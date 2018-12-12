/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EnumeratedUsagePointGroup extends UsagePointGroup, EnumeratedGroup<UsagePoint> {
    String TYPE_IDENTIFIER = "EUG";
}
