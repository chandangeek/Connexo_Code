package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;

public interface ReadingType extends IdentifiedObject {
	boolean isCumulativeReadingType(ReadingType readingType);

    long getVersion();
}
