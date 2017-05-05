/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.properties;

import com.elster.jupiter.metering.ReadingType;

public class ReadingTypeReference {

    private final ReadingType readingType;

    public ReadingTypeReference(ReadingType readingType) {
        this.readingType = readingType;
    }

    public ReadingType getReadingType() {
        return readingType;
    }
}
