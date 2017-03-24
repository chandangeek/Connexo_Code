/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingTypeInEstimationRule {

    EstimationRule getRule();

    ReadingType getReadingType();
}
