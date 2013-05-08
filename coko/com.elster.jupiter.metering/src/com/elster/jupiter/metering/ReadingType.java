package com.elster.jupiter.metering;

public interface ReadingType {
	String getMRID();
	String getName();
	String getAliasName();
	boolean isCumulativeReadingType(ReadingType readingType);
}
