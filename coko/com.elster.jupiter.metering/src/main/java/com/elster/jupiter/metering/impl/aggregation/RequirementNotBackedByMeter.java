/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when the data aggregation
 * service is introspecting the calculation of a
 * {@link com.elster.jupiter.metering.config.MetrologyContract}
 * on a {@link com.elster.jupiter.metering.UsagePoint}
 * but one of the {@link com.elster.jupiter.metering.config.ReadingTypeRequirement}s
 * is not backed by a {@link com.elster.jupiter.metering.Meter}.
 * Most of the time this is because no actual meter is linked to the
 * UsagePoint yet. Therefore, this exception should only occur
 * during introspection that kicks in before validation of
 * {@link com.elster.jupiter.metering.config.MetrologyConfiguration}
 * and Meter on the UsagePoint vetoed the configuration or the meter.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-31 (09:47)
 */
public class RequirementNotBackedByMeter extends LocalizedException {
    protected RequirementNotBackedByMeter(Thesaurus thesaurus, String targetReadingType) {
        super(thesaurus, PrivateMessageSeeds.REQUIREMENT_NOT_BACKED_BY_METER, targetReadingType);
    }
}