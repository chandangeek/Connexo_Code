package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides factory services for {@link ReadingTypeDeliverableForMeterActivation}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-22 (15:06)
 */
public interface ReadingTypeDeliverableForMeterActivationFactory {
    ReadingTypeDeliverableForMeterActivation from(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType);
}