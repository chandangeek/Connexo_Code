package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

/**
 * Provides the {@link ReadingTypeDeliverableForMeterActivation}
 * for a {@link ReadingTypeDeliverable} in the context of a {@link MeterActivation}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (12:24)
 */
interface ReadingTypeDeliverableForMeterActivationProvider {

    /**
     * Returns the {@link ReadingTypeDeliverableForMeterActivation} that is known to this provider
     * and that redefines the specified {@link ReadingTypeDeliverable} in the context
     * of the specified {@link MeterActivation}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param meterActivation The MeterActivation
     * @return The ReadingTypeDeliverableForMeterActivation
     */
    ReadingTypeDeliverableForMeterActivation from(ReadingTypeDeliverable deliverable, MeterActivation meterActivation);

}