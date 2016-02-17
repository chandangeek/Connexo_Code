package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.config.OperationNode;
import com.elster.jupiter.metering.impl.config.Operator;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (15:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MetrologyConfiguration configuration;
    @Mock
    private MetrologyContract contract;

    /**
     * Tests the simplest case:
     * Metrology configuration
     *    requirements:
     *       A+ ::= any Wh with flow = forward (aka consumption)
     *       A- ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (15m kWh) ::= A+ + A-
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A+ -> 15 min kWh
     *           A- -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    public void simplestNetConsumptionOfProsumer() {
        DataAggregationServiceImpl service = this.testInstance();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A+");
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A-");
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(
            new OperationNode(
                    Operator.PLUS,
                    new ReadingTypeRequirementNode(consumption),
                    new ReadingTypeRequirementNode(production)))
            .when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getInterval()).thenReturn(Interval.startAt(jan1st2015()));
        when(meterActivation.overlaps(year2016())).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        ReadingType readingType15min = this.mock15minReadingType();
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(readingType15min);
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(readingType15min);
        when(consumption.getMatchesFor(meterActivation)).thenReturn(Collections.singletonList(readingType15min));
        when(consumption.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn2));

        // Business method
        service.calculate(this.usagePoint, this.contract, year2016());

        // Asserts
        verify(this.virtualFactory).nextMeterActivation(meterActivation);
        verify(this.virtualFactory).requirementFor(consumption, netConsumption, IntervalLength.MINUTE15);
        verify(this.virtualFactory).requirementFor(production, netConsumption, IntervalLength.MINUTE15);
        verify(this.virtualFactory).deliverableFor(any(ReadingTypeDeliverableForMeterActivation.class), eq(IntervalLength.MINUTE15));
        verify(this.virtualFactory).allRequirements();
        verify(this.virtualFactory).allDeliverables();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Range<Instant> year2016() {
        return Range.atLeast(Instant.ofEpochMilli(1451606400000L));
    }

    private ReadingType mock15minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return meterActivationReadingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        return meterActivationReadingType;
    }

    private DataAggregationServiceImpl testInstance() {
        return new DataAggregationServiceImpl(this.virtualFactory);
    }

}