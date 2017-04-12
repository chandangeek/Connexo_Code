/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Models the exceptional situation that occurs when
 * data aggregation of a {@link MetrologyContract}
 * is requested for a period during which the contract
 * was not active on the usage point.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-24 (11:53)
 */
public class TimeOfUseBucketInconsitencyException extends LocalizedException {
    public TimeOfUseBucketInconsitencyException(Thesaurus thesaurus, int requestedBucket, int providedBucket, ReadingTypeDeliverable deliverable, UsagePoint usagePoint, Range<Instant> period) {
        super(thesaurus, MessageSeeds.TIME_OF_USE_BUCKET_INCONSISTENCY, requestedBucket, providedBucket, deliverable.getName(), usagePoint.getMRID(), period.toString());
    }
}