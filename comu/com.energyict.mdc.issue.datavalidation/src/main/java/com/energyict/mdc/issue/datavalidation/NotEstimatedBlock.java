package com.energyict.mdc.issue.datavalidation;

import java.time.Instant;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

public interface NotEstimatedBlock {
    
    Channel getChannel();

    ReadingType getReadingType();

    Instant getStartTime();
    
    Instant getEndTime();
}
