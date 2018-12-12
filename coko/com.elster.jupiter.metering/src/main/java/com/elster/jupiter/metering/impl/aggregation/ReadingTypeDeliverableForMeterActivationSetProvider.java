/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

/**
 * Provides the {@link ReadingTypeDeliverableForMeterActivationSet}
 * for a {@link ReadingTypeDeliverable} in the context of a {@link MeterActivationSet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (12:24)
 */
interface ReadingTypeDeliverableForMeterActivationSetProvider {

    /**
     * Returns the {@link ReadingTypeDeliverableForMeterActivationSet} that is known to this provider
     * and that redefines the specified {@link ReadingTypeDeliverable} in the context
     * of the specified {@link MeterActivation}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param meterActivationSet The MeterActivationSet
     * @return The ReadingTypeDeliverableForMeterActivation
     */
    ReadingTypeDeliverableForMeterActivationSet from(ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet);

}