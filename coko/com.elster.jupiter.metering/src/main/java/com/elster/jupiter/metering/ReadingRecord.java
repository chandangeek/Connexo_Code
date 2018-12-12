/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.Reading;

public interface ReadingRecord extends BaseReadingRecord , Reading {
	@Override
	ReadingRecord filter(ReadingType readingType);
}
