/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when
 * data aggregation is requested for a {@link ReadingTypeDeliverable}
 * whose formula is not using only constant like expressions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-24 (11:53)
 */
public class VirtualUsagePointsOnlySupportConstantLikeExpressionsException extends LocalizedException {
    public VirtualUsagePointsOnlySupportConstantLikeExpressionsException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.VIRTUAL_USAGE_POINT_ONLY_SUPPORT_CONSTANT_LIKE_EXPRESSIONS);
    }
}