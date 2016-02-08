package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;

import com.elster.insight.usagepoint.config.MetrologyContract;
import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DataAggregationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (12:56)
 */
public class DataAggregationServiceImpl implements DataAggregationService {

    private VirtualFactory virtualFactory;
    private TemporalAmountFactory temporalAmountFactory;
    private Map<MeterActivation, List<VirtualReadingTypeDeliverable>> deliverablesPerMeterActivation;

    @Override
    public List<? extends BaseReadingRecord> calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        this.deliverablesPerMeterActivation = new HashMap<>();
        this.getOverlappingMeterActivations(usagePoint, period).forEach(meterActivation -> this.prepare(meterActivation, contract, period));
        return Collections.emptyList();
    }

    private Stream<? extends MeterActivation> getOverlappingMeterActivations(UsagePoint usagePoint, Range<Instant> period) {
        return usagePoint.getMeterActivations().stream().filter(each -> each.overlaps(period));
    }

    private void prepare(MeterActivation meterActivation, MetrologyContract contract, Range<Instant> period) {
        this.virtualFactory.nextMeterActivation(meterActivation);
        this.deliverablesPerMeterActivation.put(meterActivation, new ArrayList<>());
        contract.getDeliverables().stream().forEach(deliverable -> this.prepare(meterActivation, deliverable, period));
    }

    private void prepare(MeterActivation meterActivation, ReadingTypeDeliverable deliverable, Range<Instant> period) {
        ExpressionNode preparedExpression = this.copyAndVirtualizeReferences(deliverable);
        this.deliverablesPerMeterActivation
                .get(meterActivation)
                .add(new VirtualReadingTypeDeliverable(
                        deliverable,
                        meterActivation,
                        period,
                        this.virtualFactory.meterActivationSequenceNumber(),
                        preparedExpression));
    }

    private ExpressionNode copyAndVirtualizeReferences(ReadingTypeDeliverable deliverable) {
        ServerFormula formula = (ServerFormula) deliverable.getFormula();
        CopyAndVirtualizeReferences visitor = new CopyAndVirtualizeReferences(this.virtualFactory, this.temporalAmountFactory, deliverable);
        return formula.expressionNode().accept(visitor);
    }

}