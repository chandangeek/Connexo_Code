package com.elster.insight.usagepoint.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

/**
 * Specifies details of a {@link com.elster.jupiter.metering.ReadingType}
 * that will be delivered from the {@link ReadingTypeRequirement}s.
 * A ReadingTypeDeliverable therefore has a formula that indicates
 * how the deliverable should be calculated from the ReadingTypeRequirements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:02)
 */
@ProviderType
public interface ReadingTypeDeliverable extends HasId, HasName {

    /**
     * Returns the {@link MetrologyConfiguration} that defined
     * this ReadingTypeDeliverable.
     *
     * @return The MetrologyConfiguration
     */
    MetrologyConfiguration getMetrologyConfiguration();

    /**
     * Returns the {@link Formula} that defines
     * how to calculate this ReadingTypeDeliverable.
     *
     * @return The Formula
     */
    Formula getFormula();

    /**
     * Gets the {@link ReadingType} that will be delivered.
     * @return
     */
    ReadingType getReadingType();

}