/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-12 (15:09)
 */
public interface ReadingTypeDeliverableFactory {
    ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable);

    ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable, MetrologyConfiguration metrologyConfiguration);
}