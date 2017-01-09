package com.elster.jupiter.metering.config;

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

    long getVersion();

    /**
     * Returns the {@link MetrologyConfiguration} that defines this ReadingTypeDeliverable.
     *
     * @return The MetrologyContract
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
     *
     * @return The ReadingType
     */
    ReadingType getReadingType();

    DeliverableType getType();

    Updater startUpdate();

    @ProviderType
    interface Updater {
        Updater setName(String name);
        Updater setReadingType(ReadingType readingType);
        Updater setFormula(String formula);
        ReadingTypeDeliverable complete();
    }

}