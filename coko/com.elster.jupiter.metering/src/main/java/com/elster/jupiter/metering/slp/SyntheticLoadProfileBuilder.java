/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.slp;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Unit;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;

@ProviderType
public interface SyntheticLoadProfileBuilder {
    SyntheticLoadProfileBuilder withDescription(String description);

    SyntheticLoadProfileBuilder withReadingType(ReadingType readingType);

    SyntheticLoadProfile build();
}
