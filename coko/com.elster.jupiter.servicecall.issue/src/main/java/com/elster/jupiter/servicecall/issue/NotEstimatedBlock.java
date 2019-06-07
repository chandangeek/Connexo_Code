/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface NotEstimatedBlock {
    
//    Channel getChannel();
//
//    ReadingType getReadingType();

    Instant getStartTime();
    
    Instant getEndTime();
}
