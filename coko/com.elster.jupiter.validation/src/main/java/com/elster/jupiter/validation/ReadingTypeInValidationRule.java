/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingTypeInValidationRule {

    ValidationRule getRule();

    ReadingType getReadingType();
}
