package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

/**
 * Adds behavior to {@link MetrologyConfiguration} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-30 (10:00)
 */
interface ServerMetrologyConfiguration extends MetrologyConfiguration {
    ReadingTypeDeliverable addReadingTypeDeliverable(String name, DeliverableType deliverableType, ReadingType readingType, Formula formula);
    void deliverableUpdated(ReadingTypeDeliverableImpl deliverable);
    void contractUpdated(MetrologyContractImpl contract);
}