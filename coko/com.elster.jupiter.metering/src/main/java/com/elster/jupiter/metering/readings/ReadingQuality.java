package com.elster.jupiter.metering.readings;

public interface ReadingQuality {
	String getComment();
	// returns systemId.category.subCategory identifier of the readingQuality's type
	String getTypeCode();
}
