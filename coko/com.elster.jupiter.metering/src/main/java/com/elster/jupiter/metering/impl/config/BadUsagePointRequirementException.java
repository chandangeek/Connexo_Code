/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.exception.MessageSeed;

public class BadUsagePointRequirementException extends LocalizedException {
    private BadUsagePointRequirementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static BadUsagePointRequirementException underlyingSearchablePropertyNotFound(Thesaurus thesaurus, String propertyName) {
        return new BadUsagePointRequirementException(thesaurus, MessageSeeds.SEARCHABLE_PROPERTY_NOT_FOUND, propertyName);
    }

    public static BadUsagePointRequirementException badValue(Thesaurus thesaurus, InvalidValueException ex) {
        return new BadUsagePointRequirementException(thesaurus, MessageSeeds.SEARCHABLE_PROPERTY_NOT_FOUND, ex.getLocalizedMessage());
    }
}
