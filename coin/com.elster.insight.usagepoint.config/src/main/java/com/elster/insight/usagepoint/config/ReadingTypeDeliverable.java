package com.elster.insight.usagepoint.config;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

/**
 * Specifies details of a {@link com.elster.jupiter.metering.ReadingType}
 * that will be delivered from the {@link ReadingTypeRequirement}s.
 * A ReadingTypeDeliverable therefore has a formula that indicates
 * how the deliverable should be calculated from the ReadingTypeRequirements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:02)
 */
public interface ReadingTypeDeliverable extends HasId, HasName {

    /**
     * Returns the {@link MetrologyConfiguration} that defined
     * this ReadingTypeDeliverable.
     *
     * @return The MetrologyConfiguration
     */
    MetrologyConfiguration getMetrologyConfiguration();

}