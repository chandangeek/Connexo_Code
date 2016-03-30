package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
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
public interface ServerMetrologyConfiguration extends MetrologyConfiguration {
    ReadingTypeDeliverable addReadingTypeDeliverable(String name, ReadingType readingType, Formula formula);
}