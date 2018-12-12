/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides factory services for {@link ReadingTypeDeliverableForMeterActivationSet}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-22 (15:06)
 */
public interface ReadingTypeDeliverableForMeterActivationFactory {
    ReadingTypeDeliverableForMeterActivationSet from(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType);
}