/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.google.common.collect.Range;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract class for {@link UsagePointResourceRegisterDataTest} and {@link UsagePointResourceChannelDataTest}
 */
public abstract class AbstractUsagePointResourceChannelDataTest extends UsagePointApplicationJerseyTest {

    static final Instant timeStamp = ZonedDateTime.of(2016, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault())
            .toInstant();

    private static final Duration MIN15 = Duration.ofMinutes(15);
    static final Range<Instant> interval_1 = Range.openClosed(timeStamp, timeStamp.plus(MIN15));
    static final Range<Instant> interval_2 = Range.openClosed(timeStamp.plus(MIN15), timeStamp.plus(MIN15.multipliedBy(2)));
    static final Range<Instant> interval_3 = Range.openClosed(timeStamp.plus(MIN15.multipliedBy(2)), timeStamp.plus(MIN15
            .multipliedBy(3)));
    static final Range<Instant> interval_4 = Range.openClosed(timeStamp.plus(MIN15.multipliedBy(3)), timeStamp.plus(MIN15
            .multipliedBy(4)));
    static final Range<Instant> interval_5 = Range.openClosed(timeStamp.plus(MIN15.multipliedBy(4)), timeStamp.plus(MIN15
            .multipliedBy(5)));


    private static final String UP_NAME = "UP0001";

    @Mock
    UsagePoint usagePoint;
    @Mock
    ReadingType readingType;
    @Mock
    ValidationEvaluator validationEvaluator;
    @Mock
    Meter meter, meter_2;
    @Mock
    ChannelsContainer upChannelsContainer;
    @Mock
    Channel aggregatedChannel;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private Formula formula;
    @Mock
    private ReadingTypeRequirementNode expressionNode;
    @Mock
    private FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement;
    @Mock
    private MeterActivation meterActivation, meterActivation_2;
    @Mock
    private ChannelsContainer meterChannelsContainer, meterChannelsContainer_2;
    @Mock
    private Channel sourceChannel, sourceChannel_2;


    void beforeSetup(){
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(UP_NAME)).thenReturn(Optional.of(usagePoint));
        when(validationService.getEvaluator(meter)).thenReturn(validationEvaluator);
        when(validationService.getEvaluator(meter_2)).thenReturn(validationEvaluator);
        when(validationEvaluator.getLastChecked(any(), any())).thenReturn(Optional.empty());

        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);

        when(usagePoint.getName()).thenReturn(UP_NAME);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));

        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(upChannelsContainer));
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(deliverable.getFormula()).thenReturn(formula);
        when(formula.getExpressionNode()).thenReturn(expressionNode);
        when(expressionNode.accept(any())).then(invocationOnMock -> {
            ExpressionNode.Visitor visitor = (ExpressionNode.Visitor) invocationOnMock.getArguments()[0];
            visitor.visitRequirement(expressionNode);
            return Void.TYPE;
        });
        when(expressionNode.getReadingTypeRequirement()).thenReturn(fullySpecifiedReadingTypeRequirement);


        when(aggregatedChannel.isRegular()).thenReturn(true);
        when(aggregatedChannel.getIntervalLength()).thenReturn(Optional.of(MIN15));
        when(aggregatedChannel.getMainReadingType()).thenReturn(readingType);


        when(meterChannelsContainer.getRange()).thenReturn(Ranges.closedOpen(interval_1.lowerEndpoint(), interval_3.upperEndpoint()));
        when(meterChannelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(meterChannelsContainer_2.getRange()).thenReturn(Ranges.closedOpen(interval_5.lowerEndpoint(), null));
        when(meterChannelsContainer_2.getMeter()).thenReturn(Optional.of(meter_2));

        when(usagePoint.getMeterActivations()).thenReturn(Arrays.asList(meterActivation, meterActivation_2));

        when(meterActivation.getRange()).thenReturn(Ranges.closedOpen(interval_1.lowerEndpoint(), interval_3.upperEndpoint()));
        when(meterActivation.getChannelsContainer()).thenReturn(meterChannelsContainer);
        when(meterActivation_2.getRange()).thenReturn(Ranges.closedOpen(interval_5.lowerEndpoint(), null));
        when(meterActivation_2.getChannelsContainer()).thenReturn(meterChannelsContainer_2);

        when(fullySpecifiedReadingTypeRequirement.getMatchingChannelsFor(meterChannelsContainer)).thenReturn(Collections
                .singletonList(sourceChannel));
        when(sourceChannel.getChannelsContainer()).thenReturn(meterChannelsContainer);
        when(sourceChannel.getMainReadingType()).thenReturn(readingType);

        when(fullySpecifiedReadingTypeRequirement.getMatchingChannelsFor(meterChannelsContainer_2)).thenReturn(Collections
                .singletonList(sourceChannel_2));
        when(sourceChannel_2.getChannelsContainer()).thenReturn(meterChannelsContainer_2);
        when(sourceChannel_2.getMainReadingType()).thenReturn(readingType);
    }

    String buildFilter() throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", timeStamp.toEpochMilli())
                .property("intervalEnd", timeStamp.plus(45, ChronoUnit.MINUTES).toEpochMilli())
                .create();
    }

    MeterActivation mockMeterActivationWithRange(Instant start, Instant end) {
        MeterActivation meterActivation = mock(MeterActivation.class);
        Range<Instant> range = Ranges.closedOpen(start, end);
        when(meterActivation.getRange()).thenReturn(range);
        return meterActivation;
    }
}
