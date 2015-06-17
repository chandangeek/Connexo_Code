package com.energyict.mdc.issue.datavalidation;

import java.time.Instant;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

@ProviderType
public interface NotEstimatedBlock {
    
    Channel getChannel();

    ReadingType getReadingType();

    Instant getStartTime();
    
    Instant getEndTime();
}
