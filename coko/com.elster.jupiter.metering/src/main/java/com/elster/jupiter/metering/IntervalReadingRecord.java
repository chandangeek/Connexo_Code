/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.IntervalReading;

public interface IntervalReadingRecord extends BaseReadingRecord , IntervalReading {
	  @Override
	  IntervalReadingRecord filter(ReadingType readingType);
}
