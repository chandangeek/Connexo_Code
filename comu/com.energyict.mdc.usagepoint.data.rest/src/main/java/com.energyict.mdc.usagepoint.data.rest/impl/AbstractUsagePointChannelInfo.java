/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;

/**
 * Abstract class for usage point channel info data
 */
public abstract class AbstractUsagePointChannelInfo {
    public long id;
    public Long dataUntil;
    public ReadingTypeInfo readingType;
}
