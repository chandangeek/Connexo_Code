/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.ServerMeteringService;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ReadingTypeDeliverableForMeterActivationFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-22 (15:08)
 */
class ReadingTypeDeliverableForMeterActivationFactoryImpl implements ReadingTypeDeliverableForMeterActivationFactory {

    private final ServerMeteringService meteringService;

    ReadingTypeDeliverableForMeterActivationFactoryImpl(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public ReadingTypeDeliverableForMeterActivationSet from(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType) {
        return new ReadingTypeDeliverableForMeterActivationSet(this.meteringService, mode, deliverable, meterActivationSet, meterActivationSequenceNumber, expressionNode, expressionReadingType);
    }

}