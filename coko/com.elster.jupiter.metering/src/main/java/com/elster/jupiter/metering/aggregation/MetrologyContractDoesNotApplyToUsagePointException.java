/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Models the exceptional situation that occurs when
 * data aggregation of a {@link MetrologyContract}
 * is requested for a period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-24 (11:53)
 */
public class MetrologyContractDoesNotApplyToUsagePointException extends LocalizedException {
    public MetrologyContractDoesNotApplyToUsagePointException(Thesaurus thesaurus, MetrologyContract contract, UsagePoint usagePoint, Range<Instant> period) {
        super(thesaurus, MessageSeeds.CONTRACT_NOT_ACTIVE, contract.getMetrologyPurpose().getName(), usagePoint.getName(), period.toString());
    }
}