/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.CalendarTimeSeries;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.metering.impl.config.ServerFormulaBuilder;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method
 * for reading type that requires time of use support that is not backed by the meter
 * but can be provided by the Calendar that is linked to the UsagePoint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-09 (16:19)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateTimeOfUseTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private MeterRole meterRole;
    @Mock
    private MetrologyContract contract;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private QueryExecutor<EffectiveMetrologyConfigurationOnUsagePoint> queryExecutor;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private CalendarService calendarService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MeteringDataModelService meteringDataModelService;
    @Mock
    private NlsService nlsService;

    private ServerMetrologyConfigurationService metrologyConfigurationService;

    @Before
    public void initializeMocks() throws SQLException {
        when(this.usagePoint.getName()).thenReturn("DataAggregationServiceImplCalculateTimeOfUseTest");
        when(this.usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.effectiveMetrologyConfiguration));
        when(this.metrologyPurpose.getName()).thenReturn("DataAggregationServiceImplCalculateTimeOfUseTest");
        when(this.contract.getMetrologyPurpose()).thenReturn(this.metrologyPurpose);
        when(this.meteringService.getDataModel()).thenReturn(this.dataModel);
        when(this.dataModel.getConnection(true)).thenReturn(this.connection);
        when(this.connection.prepareStatement(anyString())).thenReturn(this.preparedStatement);
        when(this.preparedStatement.executeQuery()).thenReturn(this.resultSet);
        when(this.dataModel.getInstance(CalculatedReadingRecordFactory.class)).thenReturn(new CalculatedReadingRecordFactoryImpl(this.dataModel, meteringService));
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringDataModelService, this.dataModel, this.thesaurus);
        when(this.configuration.getContracts()).thenReturn(Collections.singletonList(this.contract));
        when(this.dataModel.query(eq(EffectiveMetrologyConfigurationOnUsagePoint.class), anyVararg())).thenReturn(this.queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(this.effectiveMetrologyConfiguration));
        when(this.effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(this.configuration);
        when(this.effectiveMetrologyConfiguration.getRange()).thenReturn(year2016());
        when(this.effectiveMetrologyConfiguration.getInterval()).thenReturn(Interval.of(year2016()));
        when(this.usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(this.usagePoint.getCalendarUsages()).thenReturn(Collections.emptyList());
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * deliverables:
     * peakConsumption (15m kWh) ::= A-(peak)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     */
    @Test
    public void simplestNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();

        // Setup configuration requirements
        ReadingType consumptionReadingType15min = this.mock15minReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0");
        FullySpecifiedReadingTypeRequirement consumption = mock(FullySpecifiedReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        when(consumption.getReadingType()).thenReturn(consumptionReadingType15min);
        when(this.configuration.getRequirements()).thenReturn(Collections.singletonList(consumption));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));

        // Setup configuration deliverables
        ReadingTypeDeliverable peakConsumption = mock(ReadingTypeDeliverable.class);
        when(peakConsumption.getName()).thenReturn("peakConsumption");
        ReadingType peakConsumptionReadingType = this.mock15minReadingType("0.0.2.1.4.2.12.0.0.0.0.22.0.0.0.3.72.0");   // 22 is code for peak tariff
        when(peakConsumptionReadingType.getTou()).thenReturn(22);
        when(peakConsumption.getReadingType()).thenReturn(peakConsumptionReadingType);
        ServerFormulaBuilder peakFormulaBuilder = this.newFormulaBuilder();
        ExpressionNode peakNode = peakFormulaBuilder.requirement(consumption).create();
        ServerFormula peakFormula = mock(ServerFormula.class);
        when(peakFormula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(peakNode).when(peakFormula).getExpressionNode();
        when(peakConsumption.getFormula()).thenReturn(peakFormula);

        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(peakConsumption));

        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(this.meterRole));
        when(meterActivation.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        Interval year2015 = Interval.startAt(jan1st2015());
        when(meterActivation.getInterval()).thenReturn(year2015);
        when(meterActivation.getRange()).thenReturn(year2015.toClosedOpenRange());
        when(meterActivation.overlaps(aggregationPeriod)).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        ChannelContract consumptionChannel = mock(ChannelContract.class);
        when(consumptionChannel.getMainReadingType()).thenReturn(consumptionReadingType15min);
        when(consumptionChannel.getZoneId()).thenReturn(ZoneOffset.UTC);
        TimeSeries consumptionTimeSeries = mock(TimeSeries.class);
        when(consumptionTimeSeries.isRegular()).thenReturn(true);
        when(consumptionChannel.getTimeSeries()).thenReturn(consumptionTimeSeries);

        // Setup Calendar usage
        Calendar calendar = mock(Calendar.class);
        ServerCalendarUsage calendarUsage = mock(ServerCalendarUsage.class);
        when(calendarUsage.overlaps(any(Range.class))).thenReturn(true);
        when(this.usagePoint.getCalendarUsages()).thenReturn(Collections.singletonList(calendarUsage));
        CalendarTimeSeries calendarTimeSeries = mock(CalendarTimeSeries.class);
        when(calendarTimeSeries.getCalendar()).thenReturn(calendar);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(calendarTimeSeries.joinSql(any(TimeSeries.class), any(Event.class), any(Range.class), anyVararg())).thenReturn(sqlFragment);
        when(calendar.toTimeSeries(Duration.ofMinutes(15), ZoneOffset.UTC)).thenReturn(calendarTimeSeries);

        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getCalendar()).thenReturn(calendar);
        when(meterActivationSet.getUsagePoint()).thenReturn(this.usagePoint);
        when(meterActivationSet.getRange()).thenReturn(aggregationPeriod);
        List<Channel> channels = Collections.singletonList(consumptionChannel);
        VirtualReadingTypeRequirement virtualConsumption =
                new VirtualReadingTypeRequirement(
                        this.thesaurus,
                        Formula.Mode.AUTO,
                        consumption,
                        peakConsumption,
                        channels,
                        VirtualReadingType.from(peakConsumptionReadingType),
                        meterActivationSet,
                        aggregationPeriod,
                        1);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(peakConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(consumption.getMatchesFor(channelsContainer)).thenReturn(Collections.singletonList(consumptionReadingType15min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(consumptionChannel));
        when(this.virtualFactory.allRequirements()).thenReturn(Collections.singletonList(virtualConsumption));

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(calendarTimeSeries).joinSql(eq(consumptionTimeSeries), eventArgumentCaptor.capture(), any(Range.class), anyVararg());
        assertThat(eventArgumentCaptor.getValue()).isNotNull();
        assertThat(eventArgumentCaptor.getValue().getCode()).isEqualTo(22);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    /**
     * Simular to the simplest case above but the requirement
     * is configured to produce monthly values:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * deliverables:
     * peakConsumption (15m kWh) ::= A-(peak)
     * Device:
     * meter activations:
     * Jan 1st 2015 -> forever
     * A- -> 15 min kWh
     *
     * @see #simplestNetConsumptionOfProsumer()
     */
    @Test
    public void monthlyNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();

        // Setup configuration requirements
        ReadingType consumptionReadingType15min = this.mock15minReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0");
        FullySpecifiedReadingTypeRequirement consumption = mock(FullySpecifiedReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        when(consumption.getReadingType()).thenReturn(consumptionReadingType15min);
        when(this.configuration.getRequirements()).thenReturn(Collections.singletonList(consumption));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));

        // Setup configuration deliverables
        ReadingTypeDeliverable peakConsumption = mock(ReadingTypeDeliverable.class);
        when(peakConsumption.getName()).thenReturn("peakConsumption");
        ReadingType peakConsumptionReadingType = this.mockMonhtlyReadingType("13.0.0.1.4.2.12.0.0.0.0.22.0.0.0.3.72.0");   // 22 is code for peak tariff
        when(peakConsumptionReadingType.getTou()).thenReturn(22);
        when(peakConsumption.getReadingType()).thenReturn(peakConsumptionReadingType);
        ServerFormulaBuilder peakFormulaBuilder = this.newFormulaBuilder();
        ExpressionNode peakNode = peakFormulaBuilder.requirement(consumption).create();
        ServerFormula peakFormula = mock(ServerFormula.class);
        when(peakFormula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(peakNode).when(peakFormula).getExpressionNode();
        when(peakConsumption.getFormula()).thenReturn(peakFormula);

        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(peakConsumption));

        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(this.meterRole));
        when(meterActivation.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        Interval year2015 = Interval.startAt(jan1st2015());
        when(meterActivation.getInterval()).thenReturn(year2015);
        when(meterActivation.getRange()).thenReturn(year2015.toClosedOpenRange());
        when(meterActivation.overlaps(aggregationPeriod)).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        ChannelContract consumptionChannel = mock(ChannelContract.class);
        when(consumptionChannel.getMainReadingType()).thenReturn(consumptionReadingType15min);
        when(consumptionChannel.getZoneId()).thenReturn(ZoneOffset.UTC);
        TimeSeries consumptionTimeSeries = mock(TimeSeries.class);
        when(consumptionTimeSeries.isRegular()).thenReturn(true);
        when(consumptionChannel.getTimeSeries()).thenReturn(consumptionTimeSeries);

        // Setup Calendar usage
        Calendar calendar = mock(Calendar.class);
        ServerCalendarUsage calendarUsage = mock(ServerCalendarUsage.class);
        when(calendarUsage.overlaps(any(Range.class))).thenReturn(true);
        when(this.usagePoint.getCalendarUsages()).thenReturn(Collections.singletonList(calendarUsage));
        CalendarTimeSeries calendarTimeSeries = mock(CalendarTimeSeries.class);
        when(calendarTimeSeries.getCalendar()).thenReturn(calendar);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(calendarTimeSeries.joinSql(any(TimeSeries.class), any(Event.class), any(Range.class), anyVararg())).thenReturn(sqlFragment);
        when(calendar.toTimeSeries(Period.ofMonths(1), ZoneOffset.UTC)).thenReturn(calendarTimeSeries);

        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getCalendar()).thenReturn(calendar);
        when(meterActivationSet.getUsagePoint()).thenReturn(this.usagePoint);
        when(meterActivationSet.getRange()).thenReturn(aggregationPeriod);
        List<Channel> channels = Collections.singletonList(consumptionChannel);
        VirtualReadingTypeRequirement virtualConsumption =
                new VirtualReadingTypeRequirement(
                        this.thesaurus,
                        Formula.Mode.AUTO,
                        consumption,
                        peakConsumption,
                        channels,
                        VirtualReadingType.from(peakConsumptionReadingType),
                        meterActivationSet,
                        aggregationPeriod,
                        1);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(peakConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(consumption.getMatchesFor(channelsContainer)).thenReturn(Collections.singletonList(consumptionReadingType15min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(consumptionChannel));
        when(this.virtualFactory.allRequirements()).thenReturn(Collections.singletonList(virtualConsumption));

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(calendarTimeSeries).joinSql(eq(consumptionTimeSeries), eventArgumentCaptor.capture(), any(Range.class), anyVararg());
        assertThat(eventArgumentCaptor.getValue()).isNotNull();
        assertThat(eventArgumentCaptor.getValue().getCode()).isEqualTo(22);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Instant feb1st2015() {
        return Instant.ofEpochMilli(1454284800000L);
    }

    private Range<Instant> year2016() {
        return Range.atLeast(Instant.ofEpochMilli(1451606400000L));
    }

    private ReadingType mock15minReadingType(String mRID) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        return readingType;
    }

    private ReadingType mockMonhtlyReadingType(String mRID) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        return readingType;
    }

    private DataAggregationServiceImpl testInstance() {
        return new DataAggregationServiceImpl(
                this.calendarService,
                this.customPropertySetService,
                this.meteringService,
                new InstantTruncaterFactory(this.meteringService),
                SqlBuilderFactoryImpl::new,
                this::getVirtualFactory,
                this::getReadingTypeDeliverableForMeterActivationFactory);
    }

    private ReadingTypeDeliverableForMeterActivationFactory getReadingTypeDeliverableForMeterActivationFactory() {
        return new ReadingTypeDeliverableForMeterActivationFactoryImpl(this.meteringService);
    }

    private VirtualFactory getVirtualFactory() {
        return this.virtualFactory;
    }

    private ServerFormulaBuilder newFormulaBuilder() {
        return this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
    }

}