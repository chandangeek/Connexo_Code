/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload.time;

import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

public interface TimeProvider {
    Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue);
}
