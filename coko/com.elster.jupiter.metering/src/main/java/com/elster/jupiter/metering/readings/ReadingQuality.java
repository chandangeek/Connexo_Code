/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;

public interface ReadingQuality {
	String getComment();
	// returns systemId.category.subCategory identifier of the readingQuality's type
	String getTypeCode();

    default ReadingQualityType getType() {
            return new ReadingQualityType(getTypeCode());
    }
}
