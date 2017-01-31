/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

public interface ReadingTypeDeliverableFactory {
    ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable);

    ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable, MetrologyConfiguration metrologyConfiguration);
}