/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

public interface Reading extends BaseReading {
	String getReason();
	String getReadingTypeCode();
	String getText();
}
