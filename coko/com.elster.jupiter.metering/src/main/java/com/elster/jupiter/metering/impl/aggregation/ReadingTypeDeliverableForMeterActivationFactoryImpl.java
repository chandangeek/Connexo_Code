package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ReadingTypeDeliverableForMeterActivationFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-22 (15:08)
 */
public class ReadingTypeDeliverableForMeterActivationFactoryImpl implements ReadingTypeDeliverableForMeterActivationFactory {

    @Override
    public ReadingTypeDeliverableForMeterActivation from(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType) {
        return new ReadingTypeDeliverableForMeterActivation(mode, deliverable, meterActivation, requestedPeriod,  meterActivationSequenceNumber, expressionNode, expressionReadingType);
    }

}