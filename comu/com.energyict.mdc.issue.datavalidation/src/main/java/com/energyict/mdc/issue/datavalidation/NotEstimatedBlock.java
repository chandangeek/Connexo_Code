/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface NotEstimatedBlock {
    
    Channel getChannel();

    ReadingType getReadingType();

    Instant getStartTime();
    
    Instant getEndTime();
}
