package com.elster.jupiter.metering.readings;

public interface Reading extends BaseReading {
	String getReason();
	String getReadingTypeCode();
}
