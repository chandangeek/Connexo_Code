/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReadingTypeDeliverableForMeterActivationSet#appendDefinitionTo(ClauseAwareSqlBuilder)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-25 (10:04)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDeliverableForMeterActivationSetDefinitionTest {

    private static final long DELIVERABLE_ID = 97L;

    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private ReadingType readingType;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private ClauseAwareSqlBuilder clauseAwareSqlBuilder;
    @Mock
    private ServerMeteringService meteringService;

    private SqlBuilder withClauseSqlBuilder;

    private SqlBuilder selectClauseSqlBuilder;
    private Range<Instant> aggregationPeriod = Range.openClosed(Instant.ofEpochMilli(1456786800000L), Instant.ofEpochMilli(1459461600000L));
    private ServerExpressionNode expressionNode = new NumericalConstantNode(BigDecimal.TEN);

    @Before
    public void initializeMocks() {
        when(this.deliverable.getId()).thenReturn(DELIVERABLE_ID);
        when(this.deliverable.getReadingType()).thenReturn(this.readingType);
        when(this.readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMRID()).thenReturn(ReadingTypeDeliverableForMeterActivationSetDefinitionTest.class.getSimpleName());
        when(this.meterActivationSet.getRange()).thenReturn(this.aggregationPeriod);
        this.withClauseSqlBuilder = new SqlBuilder();
        when(this.clauseAwareSqlBuilder
                .with(
                        anyString(),
                        any(Optional.class),
                        anyVararg()))
                .thenReturn(this.withClauseSqlBuilder);
        this.selectClauseSqlBuilder = new SqlBuilder();
        when(this.clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseSqlBuilder);
    }

    @Test
    public void appendDefinitionTo() {
        VirtualReadingType expressionReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(expressionReadingType);

        // Business method
        testInstance.appendDefinitionTo(this.clauseAwareSqlBuilder);

        // Asserts
        verify(this.clauseAwareSqlBuilder)
                .with(
                        eq("rod97_1"),
                        any(Optional.class),
                        eq(SqlConstants.TimeSeriesColumnNames.ID.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.VERSIONCOUNT.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.RECORDTIME.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.READINGQUALITY.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName()),
                        eq(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName()));
        assertThat(this.withClauseSqlBuilder.getText()).isEqualTo("SELECT -1, 0, 0, 0, 0, '',  ? , sysdate  FROM dual");
        String selectClause = this.selectClauseSqlBuilder.getText().replace("\n", " ");
        assertThat(selectClause).isEqualTo("'ReadingTypeDeliverableForMeterActivationSetDefinitionTest', rod97_1.value, rod97_1.localdate, rod97_1.timestamp, rod97_1.readingQuality, 1, rod97_1.sourceChannels   FROM rod97_1");
    }

    @Test
    public void appendDefinitionToWithAutoTimeBasedAggregation() {
        VirtualReadingType expressionReadingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(expressionReadingType);

        // Business method
        testInstance.appendDefinitionTo(this.clauseAwareSqlBuilder);

        // Asserts
        String selectClause = this.selectClauseSqlBuilder.getText().replace("\n", " ");
        assertThat(selectClause).isEqualToIgnoringCase("'ReadingTypeDeliverableForMeterActivationSetDefinitionTest', SUM(rod97_1.value), TRUNC(rod97_1.localdate, 'MONTH'), MAX(rod97_1.timestamp), max(rod97_1.readingQuality), count(*), max(rod97_1.sourceChannels)   FROM rod97_1 GROUP BY TRUNC(rod97_1.localdate, 'MONTH')");
    }

    @Test
    public void appendDefinitionToWithExpertTimeBasedAggregation() {
        VirtualReadingType expressionReadingType = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(mock(ReadingType.class));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualRequirement.sqlName()).thenReturn("rid97_1001_1");
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(this.deliverable), any(VirtualReadingType.class))).thenReturn(virtualRequirement);
        ServerExpressionNode expertExpression = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, this.deliverable, this.meterActivationSet);
        this.expressionNode = new TimeBasedAggregationNode(expertExpression, AggregationFunction.AVG, IntervalLength.MONTH1);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(Formula.Mode.EXPERT, expressionReadingType);

        // Business method
        testInstance.appendDefinitionTo(this.clauseAwareSqlBuilder);

        // Asserts
        String withSelectClause = this.withClauseSqlBuilder.getText().replace("\n", " ");
        assertThat(withSelectClause).isEqualToIgnoringCase("SELECT -1, MAX(rid97_1001_1.timestamp), 0, 0, GREATEST(rid97_1001_1.readingQuality), rid97_1001_1.sourceChannels, AVG(), TRUNC(rid97_1001_1.localdate, 'MONTH')  FROM rid97_1001_1 GROUP BY TRUNC(rid97_1001_1.LOCALDATE, 'MONTH')");
    }

    private ReadingTypeDeliverableForMeterActivationSet testInstance(VirtualReadingType virtualReadingType) {
        return this.testInstance(Formula.Mode.AUTO, virtualReadingType);
    }

    private ReadingTypeDeliverableForMeterActivationSet testInstance(Formula.Mode mode, VirtualReadingType virtualReadingType) {
        return new ReadingTypeDeliverableForMeterActivationSet(
                this.meteringService,
                mode,
                this.deliverable,
                this.meterActivationSet,
                1,
                this.expressionNode,
                virtualReadingType);
    }

}