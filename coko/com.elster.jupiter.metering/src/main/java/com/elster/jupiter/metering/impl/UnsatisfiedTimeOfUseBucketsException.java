/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to apply a {@link com.elster.jupiter.metering.config.MetrologyConfiguration}
 * to a {@link com.elster.jupiter.metering.UsagePoint}
 * but not all of the {@link com.elster.jupiter.metering.ReadingType#getTou() time of use buckets}
 * of the {@link com.elster.jupiter.metering.config.MetrologyContract#isMandatory() mandatory}
 * deliverables are satisfied.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-21 (13:12)
 */
public class UnsatisfiedTimeOfUseBucketsException extends LocalizedException {
    protected UnsatisfiedTimeOfUseBucketsException(Thesaurus thesaurus, Set<Long> missingEventCodes) {
        super(thesaurus, PrivateMessageSeeds.UNSATISFIED_TOU, missingEventCodes.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}