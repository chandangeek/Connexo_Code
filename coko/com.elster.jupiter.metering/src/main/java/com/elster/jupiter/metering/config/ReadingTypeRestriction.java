/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;

import java.util.function.Predicate;

public enum ReadingTypeRestriction implements Predicate<ReadingType> {
    REGULAR(ReadingType::isRegular),
    IRREGULAR(readingType -> !readingType.isRegular());

    private Predicate <ReadingType> predicate;

    ReadingTypeRestriction(Predicate <ReadingType> predicate) {
        this.predicate = predicate;
    }

    public boolean test(ReadingType readingType){
        return predicate.test(readingType);
    }
}
