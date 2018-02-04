/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.time.Interval;

import ch.iec.tc57._2011.getmeterreadings.EndDevice;
import ch.iec.tc57._2011.getmeterreadings.EndDeviceGroup;
import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.Name;
import ch.iec.tc57._2011.getmeterreadings.UsagePointGroup;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsFaultMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsResponseMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.meterreadings.IntervalBlock;
import ch.iec.tc57._2011.meterreadings.IntervalReading;
import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadings.Reading;
import ch.iec.tc57._2011.meterreadings.ReadingQuality;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GetUsagePointReadingsTest extends AbstractMockActivator {
    @Rule
    public TestRule snowy = Using.timeZoneOfMcMurdo();

    private static final String USAGE_POINT_MRID = "Vâlâiom mă şel mardă";
    private static final String USAGE_POINT_NAME = "Biciadiom bravinta mă";
    private static final String ANOTHER_MRID = "Matână zabagandă";
    private static final String ANOTHER_NAME = "Io roma, io barvală";
    private static final String BILLING_NAME = "Billing";
    private static final String INFORMATION_NAME = "Information";
    private static final String CHECK_NAME = "Check";
    private static final String DAILY_MRID = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DAILY_FULL_ALIAS_NAME = "[Daily] Secondary Delta A+ (kWh)";
    private static final String MONTHLY_MRID = "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MONTHLY_FULL_ALIAS_NAME = "[Monthly] Secondary Delta A+ (kWh)";
    private static final String MIN15_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MIN15_FULL_ALIAS_NAME = "[15-minute] Secondary Delta A+ (kWh)";
    private static final String BULK_MRID = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String BULK_FULL_ALIAS_NAME = "Secondary Bulk A+ (kWh)";
    private static final String COMMENT = "Validated with rule 13";

    private static final ZonedDateTime MAY_1ST = ZonedDateTime.of(2017, 5, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime JUNE_1ST = MAY_1ST.with(Month.JUNE);
    private static final ZonedDateTime JULY_1ST = MAY_1ST.with(Month.JULY);

    private static final ReadingQualityType SUSPECT = new ReadingQualityType("3.5.258");
    private static final ReadingQualityType RULE_13_FAILED = new ReadingQualityType("3.6.13");
    private static final ReadingQualityType REMOVED = new ReadingQualityType("3.7.3");
    private static final ReadingQualityType ERROR_CODE = new ReadingQualityType("3.5.257");
    private static final ReadingQualityType INFERRED = new ReadingQualityType("3.11.1");

    private final ObjectFactory getMeterReadingsMessageObjectFactory = new ObjectFactory();

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ReadingType dailyReadingType, monthlyReadingType, min15ReadingType, registerReadingType;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration1, metrologyConfiguration2;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1, effectiveMC2;
    @Mock
    private MetrologyContract billing, information, check;
    @Mock
    private MetrologyPurpose billingPurpose, informationPurpose, checkPurpose;
    @Mock
    private MetrologyContractChannelsContainer billingContainer1, billingContainer2, informationContainer, checkContainer;
    @Mock
    private ReadingTypeDeliverable dailyDeliverable, monthlyDeliverable, min15Deliverable, registerDeliverable;
    @Mock
    private AggregatedChannel dailyChannel, monthlyChannel, min15Channel, registerChannel;
    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord dailyReading1, dailyReading2, dailyReading9,
            dailyReading10, dailyReading11, dailyReadingJune1, dailyReadingJuly1;
    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord monthlyReading1, monthlyReadingJune1, monthlyReadingJuly1;
    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord min15Reading2, min15Reading2_15, min15Reading9, min15Reading10;
    @Mock
    private ReadingRecord calculatedReading2, calculatedReading3, calculatedReading9;
    @Mock
    private ReadingRecord persistedReading2, persistedReading3, persistedReading8;
    @Mock
    private ReadingQualityRecord suspect1, suspect2, rule13Failed2, removed7, errorCode2_15,
            inferred2, inferred2_15, inferred3, inferred9, inferred10;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingQualityFetcher dailyReadingQualityFetcher, min15ReadingQualityFetcher, registerReadingQualityFetcher;
    @Captor
    private ArgumentCaptor<Range<Instant>> rangeCaptor;

    @Before
    public void setUp() throws Exception {
        when(clock.instant()).thenReturn(JUNE_1ST.toInstant());
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        mockMetrologyConfigurations();
        mockUsagePoint();
    }

    private void mockUsagePoint() {
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID);
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(effectiveMC1, effectiveMC2));
    }

    private void mockEffectiveMetrologyConfigurationsWithData() {
        mockChannelContainers();
        when(effectiveMC1.getInterval()).thenReturn(Interval.of(Range.openClosed(MAY_1ST.minusMonths(1).toInstant(), mayDay(9))));
        when(effectiveMC1.getChannelsContainer(billing)).thenReturn(Optional.of(billingContainer1));
        when(effectiveMC1.getChannelsContainer(information)).thenReturn(Optional.of(informationContainer));
        when(effectiveMC1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration1);
        when(effectiveMC2.getInterval()).thenReturn(Interval.of(Range.greaterThan(mayDay(9))));
        when(effectiveMC2.getChannelsContainer(billing)).thenReturn(Optional.of(billingContainer2));
        when(effectiveMC2.getChannelsContainer(check)).thenReturn(Optional.of(checkContainer));
        when(effectiveMC2.getMetrologyConfiguration()).thenReturn(metrologyConfiguration2);
    }

    private void mockMetrologyConfigurations() {
        mockMetrologyContracts();
        when(metrologyConfiguration1.getContracts()).thenReturn(Arrays.asList(billing, information));
        when(metrologyConfiguration2.getContracts()).thenReturn(Arrays.asList(billing, check));
    }

    private void mockMetrologyContracts() {
        mockMetrologyPurposes();
        mockDeliverables();
        when(billing.getMetrologyPurpose()).thenReturn(billingPurpose);
        when(billing.getDeliverables()).thenReturn(Arrays.asList(dailyDeliverable, monthlyDeliverable));
        when(information.getMetrologyPurpose()).thenReturn(informationPurpose);
        when(information.getDeliverables()).thenReturn(Arrays.asList(min15Deliverable, registerDeliverable));
        when(check.getMetrologyPurpose()).thenReturn(checkPurpose);
        when(check.getDeliverables()).thenReturn(Collections.singletonList(min15Deliverable));
    }

    private void mockMetrologyPurposes() {
        when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Arrays.asList(billingPurpose, informationPurpose, checkPurpose));
        when(billingPurpose.getName()).thenReturn(BILLING_NAME);
        when(informationPurpose.getName()).thenReturn(INFORMATION_NAME);
        when(checkPurpose.getName()).thenReturn(CHECK_NAME);
    }

    private void mockDeliverables() {
        when(dailyDeliverable.getReadingType()).thenReturn(dailyReadingType);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(monthlyDeliverable.getReadingType()).thenReturn(monthlyReadingType);
        mockReadingType(monthlyReadingType, MONTHLY_MRID, MONTHLY_FULL_ALIAS_NAME, true);
        when(min15Deliverable.getReadingType()).thenReturn(min15ReadingType);
        mockReadingType(min15ReadingType, MIN15_MRID, MIN15_FULL_ALIAS_NAME, true);
        when(registerDeliverable.getReadingType()).thenReturn(registerReadingType);
        mockReadingType(registerReadingType, BULK_MRID, BULK_FULL_ALIAS_NAME, false);
    }

    private void mockReadingType(ReadingType mock, String mRID, String fullAliasName, boolean regular) {
        when(mock.getMRID()).thenReturn(mRID);
        when(mock.getFullAliasName()).thenReturn(fullAliasName);
        when(mock.isRegular()).thenReturn(regular);
        when(mock.getAccumulation()).thenReturn(regular ? Accumulation.DELTADELTA : Accumulation.BULKQUANTITY);
        when(mock.getAggregate()).thenReturn(regular ? Aggregate.SUM : Aggregate.NOTAPPLICABLE);
        when(mock.getArgument()).thenReturn(regular ? new RationalNumber(1, 3) : RationalNumber.NOTAPPLICABLE);
        when(mock.getCommodity()).thenReturn(regular ? Commodity.ELECTRICITY_SECONDARY_METERED : Commodity.ELECTRICITY_PRIMARY_METERED);
        when(mock.getConsumptionTier()).thenReturn(regular ? 1 : 2);
        when(mock.getCpp()).thenReturn(regular ? 1 : 2);
        when(mock.getCurrency()).thenReturn(Currency.getInstance(regular ? "RUB" : "XXX"));
        when(mock.getFlowDirection()).thenReturn(regular ? FlowDirection.TOTAL : FlowDirection.FORWARD);
        when(mock.getInterharmonic()).thenReturn(regular ? RationalNumber.NOTAPPLICABLE : new RationalNumber(2, 3));
        when(mock.getMacroPeriod()).thenReturn(regular ? MacroPeriod.DAILY : MacroPeriod.NOTAPPLICABLE);
        when(mock.getMeasurementKind()).thenReturn(regular ? MeasurementKind.POWER : MeasurementKind.ENERGY);
        when(mock.getMeasuringPeriod()).thenReturn(regular ? TimeAttribute.NOTAPPLICABLE : TimeAttribute.HOUR24);
        when(mock.getMultiplier()).thenReturn(regular ? MetricMultiplier.KILO : MetricMultiplier.ZERO);
        when(mock.getPhases()).thenReturn(regular ? Phase.NOTAPPLICABLE : Phase.PHASES1);
        when(mock.getTou()).thenReturn(regular ? 1 : 2);
        when(mock.getUnit()).thenReturn(regular ? ReadingTypeUnit.WATTHOUR : ReadingTypeUnit.WATT);
    }

    private static void assertReadingType(ch.iec.tc57._2011.meterreadings.ReadingType rt, String fullAliasName, boolean regular) {
        assertThat(rt.getAccumulation()).isEqualTo(regular ? "Delta data" : "Bulk quantity");
        assertThat(rt.getAggregate()).isEqualTo(regular ? "Sum" : "Not applicable");
        if (regular) {
            assertThat(rt.getArgument().getNumerator().longValue()).isEqualTo(1L);
            assertThat(rt.getArgument().getDenominator().longValue()).isEqualTo(3L);
        } else {
            assertThat(rt.getArgument()).isNull();
        }
        assertThat(rt.getCommodity()).isEqualTo(regular ? "Electricity secondary metered" : "Electricity primary metered");
        assertThat(rt.getConsumptionTier().longValue()).isEqualTo(regular ? 1L : 2L);
        assertThat(rt.getCpp().longValue()).isEqualTo(regular ? 1L : 2L);
        assertThat(rt.getCurrency()).isEqualTo(regular ? "RUB" : "XXX");
        assertThat(rt.getFlowDirection()).isEqualTo(regular ? "Total" : "Forward");
        if (regular) {
            assertThat(rt.getInterharmonic()).isNull();
        } else {
            assertThat(rt.getInterharmonic().getNumerator().longValue()).isEqualTo(2L);
            assertThat(rt.getInterharmonic().getDenominator().longValue()).isEqualTo(3L);
        }
        assertThat(rt.getMacroPeriod()).isEqualTo(regular ? "Daily" : "Not applicable");
        assertThat(rt.getMeasurementKind()).isEqualTo(regular ? "Power" : "Energy");
        assertThat(rt.getMeasuringPeriod()).isEqualTo(regular ? "Not applicable" : "24-hour");
        assertThat(rt.getMultiplier()).isEqualTo(regular ? "*10^3" : "*10^0");
        assertThat(rt.getPhases()).isEqualTo(regular ? "Phase-NotApplicable" : "Phase-S1");
        assertThat(rt.getTou().longValue()).isEqualTo(regular ? 1L : 2L);
        assertThat(rt.getUnit()).isEqualTo(regular ? "Watt hours" : "Watt");
        assertThat(rt.getNames().get(0).getName()).isEqualTo(fullAliasName);
    }

    private static void assertReadingQualityTypeDescription(ch.iec.tc57._2011.meterreadings.ReadingQualityType rqt,
                                                            String system, String category, String subcategory) {
        assertThat(rqt.getSystemId()).isEqualTo(system);
        assertThat(rqt.getCategory()).isEqualTo(category);
        assertThat(rqt.getSubCategory()).isEqualTo(subcategory);
    }

    private void mockChannelContainers() {
        mockAggregatedChannels();
        when(billingContainer1.getInterval()).thenReturn(Interval.of(Range.openClosed(MAY_1ST.minusMonths(1).toInstant(), mayDay(9))));
        when(billingContainer1.getChannels()).thenReturn(Arrays.asList(dailyChannel, monthlyChannel));
        when(billingContainer2.getInterval()).thenReturn(Interval.of(Range.greaterThan(mayDay(10))));
        when(billingContainer2.getChannels()).thenReturn(Arrays.asList(dailyChannel, monthlyChannel));
        when(informationContainer.getInterval()).thenReturn(Interval.of(Range.openClosed(mayDay(2), mayDay(9))));
        when(informationContainer.getChannels()).thenReturn(Arrays.asList(min15Channel, registerChannel));
        when(checkContainer.getInterval()).thenReturn(Interval.of(Range.openClosed(mayDay(9), mayDay(15))));
        when(checkContainer.getChannels()).thenReturn(Collections.singletonList(min15Channel));
    }

    private void mockAggregatedChannels() {
        when(dailyChannel.isRegular()).thenReturn(true);
        when(dailyChannel.getMainReadingType()).thenReturn(dailyReadingType);
        when(monthlyChannel.isRegular()).thenReturn(true);
        when(monthlyChannel.getMainReadingType()).thenReturn(monthlyReadingType);
        when(min15Channel.isRegular()).thenReturn(true);
        when(min15Channel.getMainReadingType()).thenReturn(min15ReadingType);
        when(registerChannel.isRegular()).thenReturn(false);
        when(registerChannel.getMainReadingType()).thenReturn(registerReadingType);

        mockIntervalReadings();
        when(dailyChannel.getAggregatedIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        dailyReading1, dailyReading2, dailyReading9, dailyReading10, dailyReading11, dailyReadingJune1, dailyReadingJuly1)
                        .collect(Collectors.toList()));
        when(monthlyChannel.getAggregatedIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        monthlyReading1, monthlyReadingJune1, monthlyReadingJuly1)
                        .collect(Collectors.toList()));
        when(min15Channel.getAggregatedIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        min15Reading2, min15Reading2_15, min15Reading9, min15Reading10)
                        .collect(Collectors.toList()));

        mockRegisterReadings();
        when(registerChannel.getCalculatedRegisterReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        calculatedReading2, calculatedReading3, calculatedReading9)
                        .collect(Collectors.toList()));
        when(registerChannel.getPersistedRegisterReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        persistedReading2, persistedReading3, persistedReading8)
                        .collect(Collectors.toList()));

        mockReadingQualities();
        when(dailyChannel.findReadingQualities()).thenReturn(dailyReadingQualityFetcher);
        when(dailyReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect1, suspect2, removed7, rule13Failed2));
        when(monthlyChannel.findReadingQualities()).thenAnswer(invocation -> FakeBuilder.initBuilderStub(Stream.empty(), ReadingQualityFetcher.class));
        when(min15Channel.findReadingQualities()).thenReturn(min15ReadingQualityFetcher);
        when(min15ReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect2, errorCode2_15));
        when(registerChannel.findReadingQualities()).thenReturn(registerReadingQualityFetcher);
        when(registerReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect1, suspect2, removed7));
    }

    @SafeVarargs
    private static <T> Stream<T> filterInRange(Supplier<Range<Instant>> rangeSupplier,
                                               Function<? super T, Instant> instantRetriever,
                                               T... whatToFilter) {
        Range<Instant> range = rangeSupplier.get();
        return Arrays.stream(whatToFilter)
                .filter(item -> range.contains(instantRetriever.apply(item)));
    }

    private void mockIntervalReadings() {
        mockIntervalReading(dailyReading1, Range.openClosed(MAY_1ST.minusDays(1).toInstant(), MAY_1ST.toInstant()), 1.05);
        mockIntervalReading(dailyReading2, Range.openClosed(MAY_1ST.toInstant(), mayDay(2)), 2.05);
        mockIntervalReading(dailyReading9, Range.openClosed(mayDay(8), mayDay(9)), 9.05);
        mockIntervalReading(dailyReading10, Range.openClosed(mayDay(9), mayDay(10)), 10.05);
        mockIntervalReading(dailyReading11, Range.openClosed(mayDay(10), mayDay(11)), 11.05);
        mockIntervalReading(dailyReadingJune1, Range.openClosed(mayDay(31), JUNE_1ST.toInstant()), 1.06);
        mockIntervalReading(dailyReadingJuly1, Range.openClosed(JULY_1ST.minusDays(1).toInstant(), JULY_1ST.toInstant()), 1.07);

        mockIntervalReading(monthlyReading1, Range.openClosed(MAY_1ST.minusMonths(1).toInstant(), MAY_1ST.toInstant()), 5.1);
        mockIntervalReading(monthlyReadingJune1, Range.openClosed(MAY_1ST.toInstant(), JUNE_1ST.toInstant()), 6.1);
        mockIntervalReading(monthlyReadingJuly1, Range.openClosed(JUNE_1ST.toInstant(), JULY_1ST.toInstant()), 7.1);

        mockIntervalReading(min15Reading2, Range.openClosed(mayDay(2).minus(15, ChronoUnit.MINUTES), mayDay(2)), 0.052, inferred2);
        mockIntervalReading(min15Reading2_15, Range.openClosed(mayDay(2), mayDay(2).plus(15, ChronoUnit.MINUTES)), 0.05215, inferred2_15, errorCode2_15);
        mockIntervalReading(min15Reading9, Range.openClosed(mayDay(9).minus(15, ChronoUnit.MINUTES), mayDay(9)), 0.059, inferred9);
        mockIntervalReading(min15Reading10, Range.openClosed(mayDay(10).minus(15, ChronoUnit.MINUTES), mayDay(10)), 0.06, inferred10);
    }

    private void mockRegisterReadings() {
        mockRegisterReading(calculatedReading2, Range.openClosed(MAY_1ST.toInstant(), mayDay(2)), 2.2, inferred2);
        mockRegisterReading(calculatedReading3, Range.openClosed(mayDay(2), mayDay(3)), 3.3, inferred3);
        mockRegisterReading(calculatedReading9, Range.openClosed(mayDay(8), mayDay(9)), 9.9, inferred9);
        mockRegisterReading(persistedReading2, Range.openClosed(MAY_1ST.toInstant(), mayDay(2)), 22.22);
        mockRegisterReading(persistedReading3, Range.openClosed(mayDay(2), mayDay(3)), 33.33);
        mockRegisterReading(persistedReading8, Range.openClosed(mayDay(7), mayDay(8)), 8.8);
    }

    private void mockReadingQualities() {
        mockReadingQuality(suspect1, SUSPECT, MAY_1ST.toInstant(), null);
        mockReadingQuality(suspect2, SUSPECT, mayDay(2), null);
        mockReadingQuality(rule13Failed2, RULE_13_FAILED, mayDay(2), COMMENT);
        mockReadingQuality(removed7, REMOVED, mayDay(7), null);
        mockReadingQuality(errorCode2_15, ERROR_CODE, mayDay(2).plus(15, ChronoUnit.MINUTES), "I'm tired");
        mockReadingQuality(inferred2, INFERRED, mayDay(2), null);
        mockReadingQuality(inferred2_15, INFERRED, mayDay(2).plus(15, ChronoUnit.MINUTES), null);
        mockReadingQuality(inferred3, INFERRED, mayDay(3), null);
        mockReadingQuality(inferred9, INFERRED, mayDay(9), null);
        mockReadingQuality(inferred10, INFERRED, mayDay(10), null);
    }

    private void mockIntervalReading(AggregatedChannel.AggregatedIntervalReadingRecord mock,
                                     Range<Instant> interval, double value, ReadingQualityRecord... qualities) {
        when(mock.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(mock.getReportedDateTime()).thenReturn(interval.upperEndpoint().plusSeconds(1));
        when(mock.getValue()).thenReturn(BigDecimal.valueOf(value));
        doReturn(Arrays.asList(qualities)).when(mock).getReadingQualities();
    }

    private void mockRegisterReading(ReadingRecord mock, Range<Instant> interval, double value, ReadingQualityRecord... qualities) {
        when(mock.getTimeStamp()).thenReturn(interval.upperEndpoint());
        when(mock.getReportedDateTime()).thenReturn(interval.upperEndpoint().plusSeconds(1));
        when(mock.getValue()).thenReturn(BigDecimal.valueOf(value));
        when(mock.getTimePeriod()).thenReturn(Optional.of(interval));
        doReturn(Arrays.asList(qualities)).when(mock).getReadingQualities();
    }

    private void mockReadingQuality(ReadingQualityRecord mock, ReadingQualityType type, Instant timestamp, String comment) {
        when(mock.getType()).thenReturn(type);
        when(mock.getReadingTimestamp()).thenReturn(timestamp);
        when(mock.getTimestamp()).thenReturn(timestamp.plusSeconds(3));
        when(mock.getComment()).thenReturn(comment);
    }

    private void assertFaultMessage(RunnableWithFaultMessage action, String expectedCode, String expectedDetailedMessage) {
        try {
            // Business method
            action.run();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to get readings");
            MeterReadingsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getDetails()).isEqualTo(expectedDetailedMessage);
            assertThat(error.getCode()).isEqualTo(expectedCode);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e.toString());
        }
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }

    @Test
    public void testNoRequest() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestType();
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings' is required.");
    }

    @Test
    public void testEndDevice() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest().get();
        meterReadingsRequestType.getGetMeterReadings().getEndDevice().add(new EndDevice());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_ELEMENT.getErrorCode(),
                "Element 'EndDevice' under 'GetMeterReadings' is not supported.");
    }

    @Test
    public void testEndDeviceGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest().get();
        meterReadingsRequestType.getGetMeterReadings().getEndDeviceGroup().add(new EndDeviceGroup());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_ELEMENT.getErrorCode(),
                "Element 'EndDeviceGroup' under 'GetMeterReadings' is not supported.");
    }

    @Test
    public void testUsagePointGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest().get();
        meterReadingsRequestType.getGetMeterReadings().getUsagePointGroup().add(new UsagePointGroup());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_ELEMENT.getErrorCode(),
                "Element 'UsagePointGroup' under 'GetMeterReadings' is not supported.");
    }

    @Test
    public void testNoUsagePoints() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'GetMeterReadings.UsagePoint' cannot be empty.");
    }

    @Test
    public void testEmptyUsagePointMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(" \t \n \r ", USAGE_POINT_NAME).get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.UsagePoint[0].mRID' is empty or contains only white spaces.");
    }

    @Test
    public void testUsagePointIsNotFoundByMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(ANOTHER_MRID, USAGE_POINT_NAME).get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                "No usage point is found by MRID '" + ANOTHER_MRID + "'.");
    }

    @Test
    public void testEmptyUsagePointIdentifyingName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(null, " \r \n \t ").get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.UsagePoint[0].Names[?(@.NameType.name=='UsagePointName')].name' is empty or contains only white spaces.");
    }

    @Test
    public void testSeveralUsagePointIdentifyingNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(null, USAGE_POINT_NAME).get();
        Name another = GetMeterReadingsRequestBuilder.name(ANOTHER_NAME, UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType()).orElse(null);
        request.getGetMeterReadings().getUsagePoint().get(0).getNames().add(another);
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_LIST_SIZE.getErrorCode(),
                "The list of 'GetMeterReadings.UsagePoint[0].Names[?(@.NameType.name=='UsagePointName')]' has unsupported size. Must be of size 1.");
    }

    @Test
    public void testNoMRIDAndNameInUsagePoint() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(null, null).get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' with 'NameType.name' = 'UsagePointName' is required under 'GetMeterReadings.UsagePoint[0]' for identification purpose.");
    }

    @Test
    public void testUsagePointIsNotFoundByName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest().withUsagePoint(null, ANOTHER_NAME).get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                "No usage point is found by name '" + ANOTHER_NAME + "'.");
    }

    @Test
    public void testEmptyReadingTypeMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType("\t\n \t\r", DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.ReadingType[1].mRID' is empty or contains only white spaces.");
    }

    @Test
    public void testEmptyReadingTypeFullAliasName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withReadingType(null, DAILY_FULL_ALIAS_NAME)
                .withReadingType(DAILY_MRID, null)
                .withReadingType(null, "\r \n \t")
                .get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.ReadingType[2].Names[0].name' is empty or contains only white spaces.");
    }

    @Test
    public void testSeveralReadingTypeFullAliasNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withReadingType(null, DAILY_FULL_ALIAS_NAME)
                .get();
        Name oneMoreName = GetMeterReadingsRequestBuilder.name(DAILY_MRID).orElse(null);
        request.getGetMeterReadings().getReadingType().get(0).getNames().add(oneMoreName);
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_LIST_SIZE.getErrorCode(),
                "The list of 'GetMeterReadings.ReadingType[0].Names' has unsupported size. Must be of size 1.");
    }

    @Test
    public void testNoMRIDAndNameInReadingType() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withReadingType(null, DAILY_FULL_ALIAS_NAME)
                .withReadingType(DAILY_MRID, null)
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(null, null)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under 'GetMeterReadings.ReadingType[3]' for identification purpose.");
    }

    @Test
    public void testNoReadings() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'GetMeterReadings.Reading' cannot be empty.");
    }

    @Test
    public void testNoTimePeriodInReading() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .get();
        request.getGetMeterReadings().getReading().get(0).setTimePeriod(null);
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Reading[0].timePeriod' is required.");
    }

    @Test
    public void testNoTimePeriodStartInReading() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(null, JULY_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Reading[1].timePeriod.start' is required.");
    }

    @Test
    public void testUnsupportedHybridReadingSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(null, MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.HYBRID.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[2].source' contains unsupported value 'Hybrid'. Must be one of: 'System'.");
    }

    @Test
    public void testUnsupportedMeterReadingSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(null, MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withTimePeriod(null, JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.METER.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[3].source' contains unsupported value 'Meter'. Must be one of: 'System'.");
    }

    @Test
    public void testInvalidTimePeriod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(JULY_1ST.toInstant(), JUNE_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode(),
                "Can't construct a valid time period: provided start '2017-07-01T00:00:00+12:00' is after or coincides with the end '2017-06-01T00:00:00+12:00'.");
    }

    @Test
    public void testInvalidTimePeriodWithNoEnd() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), null)
                .withTimePeriod(JULY_1ST.toInstant(), null)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode(),
                "Can't construct a valid time period: provided start '2017-07-01T00:00:00+12:00' is after or coincides with the end '2017-06-01T00:00:00+12:00'.");
    }

    @Test
    public void testInvalidEmptyTimePeriod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), MAY_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode(),
                "Can't construct a valid time period: provided start '2017-05-01T00:00:00+12:00' is after or coincides with the end '2017-05-01T00:00:00+12:00'.");
    }

    @Test
    public void testUnknownPurposeNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, "Yo", "Billing", "Brother", "C'mon", "Gimme", "Information")
                .withTimePeriod(MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_PURPOSES_WITH_NAMES.getErrorCode(),
                "No metrology purposes are found for names: 'Brother', 'C'mon', 'Gimme', 'Yo'.");
    }

    @Test
    public void testLocalizedException() throws Exception {
        final String ERROR_CODE = "Eric Cartman";
        final String ERROR = "Screw you guys, I'm going home";
        mockEffectiveMetrologyConfigurationsWithData();
        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn(ERROR);
        when(localizedException.getErrorCode()).thenReturn(ERROR_CODE);
        doThrow(localizedException).when(dailyChannel).getAggregatedIntervalReadings(any());

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, "Billing", "Information")
                .withTimePeriod(MAY_1ST.toInstant(), null)
                .withTimePeriod(JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(BULK_MRID, DAILY_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                ERROR_CODE,
                ERROR);
    }

    @Test
    public void testConstraintViolationException() throws Exception {
        final String ERROR = "Oh my God, they killed Kenny!";
        mockEffectiveMetrologyConfigurationsWithData();
        VerboseConstraintViolationException exception = mock(VerboseConstraintViolationException.class);
        when(exception.getLocalizedMessage()).thenReturn(ERROR);
        doThrow(exception).when(registerChannel).getCalculatedRegisterReadings(any());

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, "Billing", "Information")
                .withTimePeriod(MAY_1ST.toInstant(), null)
                .withTimePeriod(JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(BULK_MRID, DAILY_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage),
                null,
                ERROR);
    }

    @Test
    public void testSuccessCaseWithUsagePointMRIDAndFullFiltering() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, "Billing", "Information")
                .withTimePeriod(MAY_1ST.toInstant(), null)
                .withTimePeriod(JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(BULK_MRID)
                .withReadingTypeFullAliasNames(DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertReadingTypes(meterReadings.getReadingType(), dailyReadingType, registerReadingType);
        List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> readingQualityTypes = meterReadings.getReadingQualityType();
        assertReadingQualityTypeCodes(readingQualityTypes, SUSPECT.getCode(), RULE_13_FAILED.getCode(), REMOVED.getCode(), INFERRED.getCode());
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, SUSPECT.getCode()),
                "MDM", "Reasonability", "Suspect");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, RULE_13_FAILED.getCode()),
                "MDM", "Validation", "Validated with specific rule");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, REMOVED.getCode()),
                "MDM", "Edited", "Manually rejected");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, INFERRED.getCode()),
                "MDM", "Derived", "Derived - inferred");

        List<MeterReading> purposeReadings = meterReadings.getMeterReading();
        assertUsagePointWithPurposes(purposeReadings, USAGE_POINT_MRID, USAGE_POINT_NAME, BILLING_NAME, INFORMATION_NAME);
        MeterReading billingReadings = getReadingsByPurposeName(purposeReadings, BILLING_NAME);
        assertThat(billingReadings.getReadings()).isEmpty();
        assertRegularReadingTypeReferences(billingReadings.getIntervalBlocks(), DAILY_MRID);
        List<IntervalReading> dailyReadings = billingReadings.getIntervalBlocks().get(0).getIntervalReadings();
        assertThat(dailyReadings).hasSize(6);
        assertReading(dailyReadings.get(0), dailyReading2, suspect2, rule13Failed2);
        assertMissing(dailyReadings.get(1), mayDay(7), removed7);
        assertReading(dailyReadings.get(2), dailyReading9);
        assertReading(dailyReadings.get(3), dailyReading11);
        assertReading(dailyReadings.get(4), dailyReadingJune1);
        assertReading(dailyReadings.get(5), dailyReadingJuly1);
        MeterReading informationReadings = getReadingsByPurposeName(purposeReadings, INFORMATION_NAME);
        assertThat(informationReadings.getIntervalBlocks()).isEmpty();
        List<Reading> registerReadings = informationReadings.getReadings();
        assertThat(registerReadings).hasSize(4);
        assertReading(registerReadings.get(0), BULK_MRID, persistedReading3, inferred3);
        assertMissing(registerReadings.get(1), BULK_MRID, mayDay(7), removed7);
        assertReading(registerReadings.get(2), BULK_MRID, persistedReading8);
        assertReading(registerReadings.get(3), BULK_MRID, calculatedReading9, inferred9);
    }

    @Test
    public void testSuccessCaseWithUsagePointNameAndNoFiltering() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), null)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertReadingTypes(meterReadings.getReadingType(), dailyReadingType, registerReadingType, monthlyReadingType, min15ReadingType);
        List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> readingQualityTypes = meterReadings.getReadingQualityType();
        assertReadingQualityTypeCodes(readingQualityTypes, SUSPECT.getCode(), RULE_13_FAILED.getCode(), REMOVED.getCode(), ERROR_CODE.getCode(), INFERRED.getCode());
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, SUSPECT.getCode()),
                "MDM", "Reasonability", "Suspect");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, RULE_13_FAILED.getCode()),
                "MDM", "Validation", "Validated with specific rule");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, REMOVED.getCode()),
                "MDM", "Edited", "Manually rejected");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, ERROR_CODE.getCode()),
                "MDM", "Reasonability", "Error code");
        assertReadingQualityTypeDescription(getReadingQualityTypeWithCode(readingQualityTypes, INFERRED.getCode()),
                "MDM", "Derived", "Derived - inferred");

        List<MeterReading> purposeReadings = meterReadings.getMeterReading();
        assertUsagePointWithPurposes(purposeReadings, USAGE_POINT_MRID, USAGE_POINT_NAME, BILLING_NAME, INFORMATION_NAME, CHECK_NAME);
        MeterReading billingReadings = getReadingsByPurposeName(purposeReadings, BILLING_NAME);
        assertThat(billingReadings.getReadings()).isEmpty();
        List<IntervalBlock> billingReadingBlocks = billingReadings.getIntervalBlocks();
        assertRegularReadingTypeReferences(billingReadingBlocks, DAILY_MRID, MONTHLY_MRID);
        List<IntervalReading> dailyReadings = getReadingsByReadingTypeMRID(billingReadingBlocks, DAILY_MRID)
                .getIntervalReadings();
        assertThat(dailyReadings).hasSize(5);
        assertReading(dailyReadings.get(0), dailyReading2, suspect2, rule13Failed2);
        assertMissing(dailyReadings.get(1), mayDay(7), removed7);
        assertReading(dailyReadings.get(2), dailyReading9);
        assertReading(dailyReadings.get(3), dailyReading11);
        assertReading(dailyReadings.get(4), dailyReadingJune1);
        List<IntervalReading> monthlyReadings = getReadingsByReadingTypeMRID(billingReadingBlocks, MONTHLY_MRID)
                .getIntervalReadings();
        assertThat(monthlyReadings).hasSize(1);
        assertReading(monthlyReadings.get(0), monthlyReadingJune1);
        MeterReading informationReadings = getReadingsByPurposeName(purposeReadings, INFORMATION_NAME);
        assertRegularReadingTypeReferences(informationReadings.getIntervalBlocks(), MIN15_MRID);
        List<IntervalReading> min15Readings = informationReadings.getIntervalBlocks().get(0).getIntervalReadings();
        assertThat(min15Readings).hasSize(2);
        assertReading(min15Readings.get(0), min15Reading2_15, errorCode2_15, inferred2_15);
        assertReading(min15Readings.get(1), min15Reading9, inferred9);
        List<Reading> registerReadings = informationReadings.getReadings();
        assertThat(registerReadings).hasSize(4);
        assertReading(registerReadings.get(0), BULK_MRID, persistedReading3, inferred3);
        assertMissing(registerReadings.get(1), BULK_MRID, mayDay(7), removed7);
        assertReading(registerReadings.get(2), BULK_MRID, persistedReading8);
        assertReading(registerReadings.get(3), BULK_MRID, calculatedReading9, inferred9);
        MeterReading checkReadings = getReadingsByPurposeName(purposeReadings, CHECK_NAME);
        assertThat(checkReadings.getReadings()).isEmpty();
        assertRegularReadingTypeReferences(checkReadings.getIntervalBlocks(), MIN15_MRID);
        min15Readings = checkReadings.getIntervalBlocks().get(0).getIntervalReadings();
        assertThat(min15Readings).hasSize(1);
        assertReading(min15Readings.get(0), min15Reading10, inferred10);
    }

    @Test
    public void testSuccessCaseWithNoMatchingReadingsInUsagePoint() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();
        when(effectiveMC1.getChannelsContainer(information)).thenReturn(Optional.empty()); // only channel container for effectiveMC1 is billing
        when(billingContainer1.getChannels()).thenReturn(Collections.singletonList(monthlyChannel)); // only channel in billing is monthly

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(MAY_1ST.toInstant(), mayDay(9)) // only effectiveMC1 matches,
                // but its only channel has neither readings nor qualities in this period
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
    }

    @Test
    public void testSuccessCaseWithTimePeriodNotMatchingWithContainer() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();
        when(checkContainer.getChannels()).thenReturn(Collections.emptyList());

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(mayDay(9), mayDay(10)) // only matches with checkContainer that has no channels
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
    }

    @Test
    public void testSuccessCaseWithFilterThatFetchesNothing() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CHECK_NAME)
                .withTimePeriod(mayDay(1), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(DAILY_MRID, MONTHLY_MRID, BULK_MRID)
                .withReadingTypeFullAliasNames(DAILY_FULL_ALIAS_NAME, MONTHLY_FULL_ALIAS_NAME, BULK_FULL_ALIAS_NAME)
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = getInstance(ExecuteMeterReadingsEndpoint.class).getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
    }

    private static void assertRegularReadingTypeReferences(List<IntervalBlock> intervalBlocks, String... mRIDs) {
        List<String> readingTypeMRIDs = intervalBlocks.stream()
                .map(IntervalBlock::getReadingType)
                .peek(rt -> assertThat(rt).isNotNull())
                .map(ch.iec.tc57._2011.meterreadings.IntervalBlock.ReadingType::getRef)
                .collect(Collectors.toList());
        assertThat(readingTypeMRIDs).containsOnly(mRIDs);
    }

    private static IntervalBlock getReadingsByReadingTypeMRID(List<IntervalBlock> blocks, String mRID) {
        return blocks.stream()
                .filter(block -> mRID.equals(block.getReadingType().getRef()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private static void assertReadingQualityTypeCodes(List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> qualityTypes,
                                                      String... codes) {
        assertThat(qualityTypes.stream()
                .map(ch.iec.tc57._2011.meterreadings.ReadingQualityType::getMRID)
                .collect(Collectors.toList()))
                .containsOnly(codes);
    }

    private static ch.iec.tc57._2011.meterreadings.ReadingQualityType getReadingQualityTypeWithCode(
            List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> qualityTypes, String code) {
        return qualityTypes.stream()
                .filter(rqt -> code.equals(rqt.getMRID()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private static void assertReadingTypes(List<ch.iec.tc57._2011.meterreadings.ReadingType> actualReadingTypes,
                                           ReadingType... expectedReadingTypes) {
        Map<String, ReadingType> expectedReadingTypesByMRID = Arrays.stream(expectedReadingTypes)
                .collect(Collectors.toMap(ReadingType::getMRID, Function.identity()));
        assertThat(actualReadingTypes.stream()
                .map(ch.iec.tc57._2011.meterreadings.ReadingType::getMRID)
                .collect(Collectors.toList()))
                .containsOnly(expectedReadingTypesByMRID.keySet().stream().toArray(String[]::new));
        actualReadingTypes.forEach(actualReadingType -> {
            ReadingType expectedReadingType = expectedReadingTypesByMRID.get(actualReadingType.getMRID());
            assertReadingType(actualReadingType, expectedReadingType.getFullAliasName(), expectedReadingType.isRegular());
        });
    }

    private static void assertUsagePointWithPurposes(List<MeterReading> meterReadings, String usagePointMRID, String usagePointName,
                                                     String... purposeNames) {
        meterReadings.forEach(reading -> {
            ch.iec.tc57._2011.meterreadings.UsagePoint usagePoint = reading.getUsagePoint();
            assertThat(usagePoint).isNotNull();
            assertThat(usagePoint.getMRID()).isEqualTo(usagePointMRID);
            List<ch.iec.tc57._2011.meterreadings.Name> names = usagePoint.getNames();
            assertThat(names).hasSize(2);
            names.forEach(name -> {
                assertThat(name.getName()).isNotNull();
                assertThat(name.getNameType()).isNotNull();
            });
            assertThat(names.stream()
                    .filter(name -> UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType().equals(name.getNameType().getName()))
                    .findFirst()
                    .map(ch.iec.tc57._2011.meterreadings.Name::getName))
                    .contains(usagePointName);
        });
        assertThat(meterReadings.stream()
                .map(MeterReading::getUsagePoint)
                .map(ch.iec.tc57._2011.meterreadings.UsagePoint::getNames)
                .flatMap(List::stream)
                .filter(name -> UsagePointNameTypeEnum.PURPOSE.getNameType().equals(name.getNameType().getName()))
                .map(ch.iec.tc57._2011.meterreadings.Name::getName)
                .collect(Collectors.toList()))
                .containsOnly(purposeNames);
    }

    private static MeterReading getReadingsByPurposeName(List<MeterReading> meterReadings, String purposeName) {
        return meterReadings.stream()
                .filter(purpose -> purpose.getUsagePoint().getNames().stream()
                        .filter(name -> UsagePointNameTypeEnum.PURPOSE.getNameType().equals(name.getNameType().getName()))
                        .map(ch.iec.tc57._2011.meterreadings.Name::getName)
                        .anyMatch(purposeName::equals))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private static void assertReading(IntervalReading actualReading, BaseReadingRecord expectedReading,
                                      ReadingQualityRecord... expectedQualities) {
        assertThat(actualReading.getTimeStamp()).isEqualTo(expectedReading.getTimeStamp());
        assertThat(actualReading.getReportedDateTime()).isEqualTo(expectedReading.getReportedDateTime());
        assertThat(actualReading.getValue()).isEqualTo(expectedReading.getValue().toPlainString());
        assertReadingQualities(actualReading.getReadingQualities(), expectedQualities);
    }

    private static void assertMissing(IntervalReading actualReading, Instant timestamp,
                                      ReadingQualityRecord... expectedQualities) {
        assertThat(actualReading.getTimeStamp()).isEqualTo(timestamp);
        assertThat(actualReading.getReportedDateTime()).isNull();
        assertThat(actualReading.getValue()).isNull();
        assertReadingQualities(actualReading.getReadingQualities(), expectedQualities);
    }

    private static void assertReading(Reading actualReading, String readingTypeMRID,
                                      BaseReadingRecord expectedReading, ReadingQualityRecord... expectedQualities) {
        assertThat(actualReading.getTimeStamp()).isEqualTo(expectedReading.getTimeStamp());
        assertThat(actualReading.getReadingType()).isNotNull();
        assertThat(actualReading.getReadingType().getRef()).isEqualTo(readingTypeMRID);
        Optional<Range<Instant>> timePeriod = expectedReading.getTimePeriod();
        if (timePeriod.isPresent()) {
            assertThat(actualReading.getTimePeriod()).isNotNull();
            assertThat(actualReading.getTimePeriod().getStart()).isEqualTo(timePeriod.get().lowerEndpoint());
            assertThat(actualReading.getTimePeriod().getEnd()).isEqualTo(timePeriod.get().upperEndpoint());
        } else {
            assertThat(actualReading.getTimePeriod()).isNull();
        }
        assertThat(actualReading.getReportedDateTime()).isEqualTo(expectedReading.getReportedDateTime());
        assertThat(actualReading.getValue()).isEqualTo(expectedReading.getValue().toPlainString());
        assertReadingQualities(actualReading.getReadingQualities(), expectedQualities);
    }

    private static void assertMissing(Reading actualReading, String readingTypeMRID,
                                      Instant timestamp, ReadingQualityRecord... expectedQualities) {
        assertThat(actualReading.getTimeStamp()).isEqualTo(timestamp);
        assertThat(actualReading.getReadingType()).isNotNull();
        assertThat(actualReading.getReadingType().getRef()).isEqualTo(readingTypeMRID);
        assertThat(actualReading.getTimePeriod()).isNull();
        assertThat(actualReading.getReportedDateTime()).isNull();
        assertThat(actualReading.getValue()).isNull();
        assertReadingQualities(actualReading.getReadingQualities(), expectedQualities);
    }

    private static void assertReadingQualities(List<ReadingQuality> actualQualities, ReadingQualityRecord... expectedQualities) {
        actualQualities.forEach(quality -> assertThat(quality.getReadingQualityType()).isNotNull());
        Map<String, ReadingQualityRecord> expectedQualitiesByCodes = Arrays.stream(expectedQualities)
                .collect(Collectors.toMap(quality -> quality.getType().getCode(), Function.identity()));
        assertThat(actualQualities.stream()
                .map(ReadingQuality::getReadingQualityType)
                .map(ReadingQuality.ReadingQualityType::getRef)
                .collect(Collectors.toList()))
                .containsOnly(expectedQualitiesByCodes.keySet().stream().toArray(String[]::new));
        actualQualities.forEach(actualQuality -> {
            ReadingQualityRecord expected = expectedQualitiesByCodes.get(actualQuality.getReadingQualityType().getRef());
            assertThat(actualQuality.getTimeStamp()).isEqualTo(expected.getTimestamp());
            assertThat(actualQuality.getComment()).isEqualTo(expected.getComment());
        });
    }

    private static Instant mayDay(int n) {
        return MAY_1ST.withDayOfMonth(n).toInstant();
    }
}
