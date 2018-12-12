/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import java.time.Instant;

/**
 * Abstract class for usage point channel info data
 */
public abstract class AbstractUsagePointChannelInfo {
    public long id;
    public Instant dataUntil;
    public ReadingTypeInfo readingType;
}
