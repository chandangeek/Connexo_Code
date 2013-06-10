package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasName;

public interface ReadingType extends HasName {
	String getMRID();
	String getAliasName();
	boolean isCumulativeReadingType(ReadingType readingType);
}
