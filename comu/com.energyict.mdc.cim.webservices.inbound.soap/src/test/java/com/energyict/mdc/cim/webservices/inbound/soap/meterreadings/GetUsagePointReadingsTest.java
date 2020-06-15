/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

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
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;

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
import com.google.common.collect.SetMultimap;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.DataSourceTypeName.LOAD_PROFILE;
import static com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.DataSourceTypeName.REGISTER_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetUsagePointReadingsTest extends AbstractMockActivator {
    private static final String USAGE_POINT_MRID = "Vâlâiom mă şel mardă";
    private static final String USAGE_POINT_NAME = "Biciadiom bravinta mă";
    private static final String ANOTHER_MRID = "Matână zabagandă";
    private static final String ANOTHER_NAME = "Io roma, io barvală";
    private static final String END_DEVICE1_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41ac";
    private static final String END_DEVICE1_NAME = "SPE01000001";
    private static final long END_DEVICE1_AMRID = 1;
    private static final String END_DEVICE2_MRID = "a74e77e1-c397-41c8-8c3c-6ddab969047c";
    private static final String END_DEVICE2_NAME = "SPE01000002";
    private static final long END_DEVICE2_AMRID = 2;
    private static final String COM_TASK_NAME = "com task name";
    private static final String LOAD_PROFILE_NAME = "load profile name";
    private static final String REGISTER_GROUP_NAME = "register group name";
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
    private static final String REPLY_ADDRESS = "some_url";
    private static final ZonedDateTime MAY_1ST = ZonedDateTime.of(2017, 5, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime JUNE_1ST = MAY_1ST.with(Month.JUNE);
    private static final ZonedDateTime JULY_1ST = MAY_1ST.with(Month.JULY);
    private static final ReadingQualityType SUSPECT = new ReadingQualityType("3.5.258");
    private static final ReadingQualityType RULE_13_FAILED = new ReadingQualityType("3.6.13");
    private static final ReadingQualityType REMOVED = new ReadingQualityType("3.7.3");
    private static final ReadingQualityType ERROR_CODE = new ReadingQualityType("3.5.257");
    private static final ReadingQualityType INFERRED = new ReadingQualityType("3.11.1");
    private final ObjectFactory getMeterReadingsMessageObjectFactory = new ObjectFactory();
    @Rule
    public TestRule snowy = Using.timeZoneOfMcMurdo();
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private com.elster.jupiter.metering.Meter meter1, meter2;
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
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private ReadingTypeDeliverable dailyDeliverable, monthlyDeliverable, min15Deliverable, registerDeliverable;
    @Mock
    private AggregatedChannel dailyChannel, monthlyChannel, min15Channel, registerChannel;
    @Mock
    private CimChannel dailyCimChannel, monthlyCimChannel, min15CimChannel, registerCimChannel;
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
    @Mock
    private Query<com.elster.jupiter.metering.Meter> meterQuery;
    @Mock
    private WebServicesService webServicesService;
    @Captor
    private ArgumentCaptor<Range<Instant>> rangeCaptor;
    @Mock
    private Device device1, device2;
    @Mock
    private WebServiceContext webServiceContext;
    @Mock
    private MessageContext messageContext;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private ComTaskExecution devMesComTaskExecution, regComTaskExecution, irregComTaskExecution;
    @Mock
    private ComTaskEnablement devMessageTaskEnablement, regCimTaskEnablement, irregComTaskEnablement;
    @Mock
    DeviceConfiguration deviceConfiguration, regDeviceConfiguration, irregMesDeviceConfiguration;
    @Mock
    ComTask devMesComTask, regComTask, irregComTask;
    @Mock
    MessagesTask messagesTask;
    @Mock
    LoadProfilesTask loadProfilesTask;
    @Mock
    RegistersTask registersTask;
    @Mock
    DeviceMessageCategory deviceMessageCategory;
    @Mock
    ConnectionTask connectionTask;
    @Mock
    LoadProfileType loadProfileType;
    @Mock
    LoadProfile loadProfile;
    @Mock
    RegisterGroup registerGroup;
    @Mock
    Device.DeviceMessageBuilder deviceMessageBuilder;
    @Mock
    DeviceMessage deviceMessage;


    private ExecuteMeterReadingsEndpoint executeMeterReadingsEndpoint;

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
        assertThat(rt.getMeasuringPeriod()).isEqualTo(regular ? "Not applicable" : TimeAttribute.HOUR24.getDescription());
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

    @SafeVarargs
    private static <T> Stream<T> filterInRange(Supplier<Range<Instant>> rangeSupplier,
                                               Function<? super T, Instant> instantRetriever,
                                               T... whatToFilter) {
        Range<Instant> range = rangeSupplier.get();
        return Arrays.stream(whatToFilter)
                .filter(item -> range.contains(instantRetriever.apply(item)));
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
                    .filter(name -> UsagePointNameType.USAGE_POINT_NAME.getNameType()
                            .equals(name.getNameType().getName()))
                    .findFirst()
                    .map(ch.iec.tc57._2011.meterreadings.Name::getName))
                    .contains(usagePointName);
        });
        assertThat(meterReadings.stream()
                .map(MeterReading::getUsagePoint)
                .map(ch.iec.tc57._2011.meterreadings.UsagePoint::getNames)
                .flatMap(List::stream)
                .filter(name -> UsagePointNameType.PURPOSE.getNameType().equals(name.getNameType().getName()))
                .map(ch.iec.tc57._2011.meterreadings.Name::getName)
                .collect(Collectors.toList()))
                .containsOnly(purposeNames);
    }

    private static MeterReading getReadingsByPurposeName(List<MeterReading> meterReadings, String purposeName) {
        return meterReadings.stream()
                .filter(purpose -> purpose.getUsagePoint().getNames().stream()
                        .filter(name -> UsagePointNameType.PURPOSE.getNameType()
                                .equals(name.getNameType().getName()))
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
            ReadingQualityRecord expected = expectedQualitiesByCodes.get(actualQuality.getReadingQualityType()
                    .getRef());
            assertThat(actualQuality.getTimeStamp()).isEqualTo(expected.getTimestamp());
            assertThat(actualQuality.getComment()).isEqualTo(expected.getComment());
        });
    }

    private static Instant mayDay(int n) {
        return MAY_1ST.withDayOfMonth(n).toInstant();
    }

    @Before
    public void setUp() throws Exception {
        executeMeterReadingsEndpoint = getInstance(ExecuteMeterReadingsEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeMeterReadingsEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1l);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeMeterReadingsEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeMeterReadingsEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeMeterReadingsEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(new Answer(){
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((ExceptionThrowingSupplier)invocationOnMock.getArguments()[0]).get();
            }
        });
        when(webServicesService.getOngoingOccurrence(1l)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));
        when(clock.instant()).thenReturn(JUNE_1ST.toInstant());
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        mockMetrologyConfigurations();
        mockUsagePoint();
        mockEndDevices();
        mockDevices();
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
        mockMetrologyContractChannelsContainers();
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

    private void mockFindReadingTypes(ReadingType... readingTypes) {
        Finder finder = mock(Finder.class);
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(finder);
        List<ReadingType> readings = new ArrayList<>();
        readings.addAll(Arrays.asList(readingTypes));
        when(finder.find()).thenReturn(readings);
        when(finder.stream()).thenReturn(readings.stream());
    }

    private void mockFindEndDevices(com.elster.jupiter.metering.Meter... endDevices) {
        List<com.elster.jupiter.metering.Meter> devices = new ArrayList<>();
        devices.addAll(Arrays.asList(endDevices));
        when(meteringService.getMeterQuery()).thenReturn(meterQuery);
        when(meteringService.getMeterQuery().select(anyObject())).thenReturn(devices);
    }

    private void mockFindEndPointConfigurations() {
        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration(REPLY_ADDRESS);
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
    }

    private void mockEndDevices() {
        mockEndDevice(meter1, END_DEVICE1_MRID, END_DEVICE1_NAME, String.valueOf(END_DEVICE1_AMRID));
        mockEndDevice(meter2, END_DEVICE2_MRID, END_DEVICE2_NAME, String.valueOf(END_DEVICE2_AMRID));
    }

    private void mockEndDevice(com.elster.jupiter.metering.EndDevice mock, String MRID, String name, String amrId) {
        when(mock.getMRID()).thenReturn(MRID);
        when(mock.getName()).thenReturn(name);
        when(mock.getAmrId()).thenReturn(amrId);
    }

    private void mockDevices() {
        mockDevice(device1, 1);
        mockDevice(device2, 2);
        when(deviceService.findDeviceById(END_DEVICE1_AMRID)).thenReturn(Optional.of(device1));
        when(deviceService.findDeviceById(END_DEVICE2_AMRID)).thenReturn(Optional.of(device2));
        when(deviceService.findAndLockDeviceById(END_DEVICE1_AMRID)).thenReturn(Optional.of(device1));
        when(deviceService.findAndLockDeviceById(END_DEVICE2_AMRID)).thenReturn(Optional.of(device2));
        Finder finder = mock(Finder.class);
        when(deviceService.findAllDevices(any())).thenReturn(finder);
        when(finder.stream())
                .thenReturn(Arrays.asList(device1, device2).stream());
        mockConnectionTask();
    }

    private void mockDevice(Device mock, long id) {
        when(mock.getComTaskExecutions()).thenReturn(Arrays.asList(devMesComTaskExecution, regComTaskExecution, irregComTaskExecution));
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn("deviceName");
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(devMessageTaskEnablement, regCimTaskEnablement, irregComTaskEnablement));
        mockComTaskEnablement(devMessageTaskEnablement, devMesComTask);
        mockComTaskEnablement(regCimTaskEnablement, regComTask);
        mockComTaskEnablement(irregComTaskEnablement, irregComTask);

        mockComTaskExecution(devMesComTaskExecution, mock, devMesComTask, messagesTask);
        mockComTaskExecution(regComTaskExecution, mock, regComTask, loadProfilesTask);
        mockComTaskExecution(irregComTaskExecution, mock, irregComTask, registersTask);
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Arrays.asList(deviceMessageCategory));
        when(deviceMessageCategory.getId()).thenReturn(16);
    }

    private void mockComTaskExecution(ComTaskExecution mock, Device device, ComTask comTask, ProtocolTask protocolTask) {
        when(mock.getDevice()).thenReturn(device);
        when(mock.getComTask()).thenReturn(comTask);
        mockComTask(comTask);
        when(mock.getProtocolTasks()).thenReturn(Arrays.asList(protocolTask));
        when(mock.getConnectionTask()).thenReturn(Optional.of(connectionTask));
    }

    private void mockConnectionTask() {
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(partialConnectionTask.getName()).thenReturn(COM_TASK_NAME);
    }

    private void mockComTaskEnablement(ComTaskEnablement mock, ComTask comTask) {
        when(mock.getComTask()).thenReturn(comTask);
    }

    private void mockComTask(ComTask mock){
        when(mock.getId()).thenReturn(1L);
        when(mock.isManualSystemTask()).thenReturn(true);
        when(mock.getName()).thenReturn("comTaskName");
    }


    private void mockLoadProfileType() {
        when(masterDataService.findAllLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        when(loadProfileType.getName()).thenReturn(LOAD_PROFILE_NAME);
    }

    private void mockLoadProfile() {
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(device2.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
    }

    private void mockDeviceMessage() {
        when(device1.newDeviceMessage(any())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setTrackingId(any())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(any())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);

        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessageSpec.getName()).thenReturn("device message name");
        when(deviceMessage.getId()).thenReturn(1L);
    }

    private void mockRegisterGroupType() {
        Finder finder = mock(Finder.class);
        when(masterDataService.findAllRegisterGroups()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(registerGroup));
        when(registerGroup.getName()).thenReturn(REGISTER_GROUP_NAME);
    }

    private void mockReadingTypesOnDevices() {
        when(meter1.getChannelsContainers()).thenReturn(Collections.singletonList(channelsContainer1));
        when(channelsContainer1.getChannel(min15ReadingType)).thenReturn(Optional.of(min15Channel));
        when(channelsContainer1.getChannel(dailyReadingType)).thenReturn(Optional.of(dailyChannel));
        when(meter2.getChannelsContainers()).thenReturn(Collections.singletonList(channelsContainer1));
        when(channelsContainer1.getChannel(min15ReadingType)).thenReturn(Optional.of(min15Channel));
        when(channelsContainer1.getChannel(dailyReadingType)).thenReturn(Optional.of(dailyChannel));
        doReturn(Collections.singletonList(min15ReadingType)).when(min15Channel).getReadingTypes();
        doReturn(Collections.singletonList(dailyReadingType)).when(dailyChannel).getReadingTypes();
    }

    private void mockChannelsContainers() {
        mockCimChannels();
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(Range.openClosed(MAY_1ST.minusMonths(1).toInstant(), mayDay(9))));
        when(channelsContainer1.getChannels()).thenReturn(Arrays.asList(min15Channel, registerChannel, dailyChannel, monthlyChannel));
        when(min15Channel.getLastDateTime()).thenReturn(MAY_1ST.toInstant());
        when(registerChannel.getLastDateTime()).thenReturn(MAY_1ST.toInstant());
        when(dailyChannel.getLastDateTime()).thenReturn(MAY_1ST.toInstant());
        when(monthlyChannel.getLastDateTime()).thenReturn(MAY_1ST.toInstant());

        when(min15Channel.truncateToIntervalLength(any())).thenReturn(MAY_1ST.toInstant());
        when(registerChannel.truncateToIntervalLength(any())).thenReturn(MAY_1ST.toInstant());
        when(dailyChannel.truncateToIntervalLength(any())).thenReturn(MAY_1ST.toInstant());
        when(monthlyChannel.truncateToIntervalLength(any())).thenReturn(MAY_1ST.toInstant());

        when(channelsContainer2.getInterval()).thenReturn(Interval.of(Range.greaterThan(mayDay(10))));
        when(channelsContainer2.getChannels()).thenReturn(Arrays.asList(min15Channel, registerChannel, dailyChannel, monthlyChannel));

        when(min15Channel.getCimChannel(any())).thenReturn(Optional.of(min15CimChannel));
        when(registerChannel.getCimChannel(any())).thenReturn(Optional.of(registerCimChannel));
        when(dailyChannel.getCimChannel(any())).thenReturn(Optional.of(dailyCimChannel));
        when(monthlyChannel.getCimChannel(any())).thenReturn(Optional.of(monthlyCimChannel));
    }

    private void mockCimChannels() {
        when(dailyCimChannel.isRegular()).thenReturn(true);
        when(monthlyCimChannel.isRegular()).thenReturn(true);
        when(min15CimChannel.isRegular()).thenReturn(true);
        when(registerCimChannel.isRegular()).thenReturn(false);

        mockIntervalReadings();
        when(dailyCimChannel.getIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        dailyReading1, dailyReading2, dailyReading9, dailyReading10, dailyReading11, dailyReadingJune1, dailyReadingJuly1)
                        .collect(Collectors.toList()));
        when(monthlyCimChannel.getIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        monthlyReading1, monthlyReadingJune1, monthlyReadingJuly1)
                        .collect(Collectors.toList()));
        when(min15CimChannel.getIntervalReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        min15Reading2, min15Reading2_15, min15Reading9, min15Reading10)
                        .collect(Collectors.toList()));

        mockRegisterReadings();
        when(registerCimChannel.getRegisterReadings(rangeCaptor.capture()))
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, BaseReadingRecord::getTimeStamp,
                        calculatedReading2, calculatedReading3, calculatedReading9)
                        .collect(Collectors.toList()));

        mockReadingQualities();
        when(dailyCimChannel.findReadingQualities()).thenReturn(dailyReadingQualityFetcher);
        when(dailyReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect1, suspect2, removed7, rule13Failed2));
        when(monthlyCimChannel.findReadingQualities()).thenAnswer(invocation -> FakeBuilder.initBuilderStub(Stream.empty(), ReadingQualityFetcher.class));
        when(min15CimChannel.findReadingQualities()).thenReturn(min15ReadingQualityFetcher);
        when(min15ReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect2, errorCode2_15));
        when(registerCimChannel.findReadingQualities()).thenReturn(registerReadingQualityFetcher);
        when(registerReadingQualityFetcher.actual()
                .inTimeInterval(rangeCaptor.capture())
                .stream())
                .thenAnswer(invocation -> filterInRange(rangeCaptor::getValue, ReadingQualityRecord::getReadingTimestamp,
                        suspect1, suspect2, removed7));
    }

    private void mockMetrologyContractChannelsContainers() {
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
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to get readings.");
            MeterReadingsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getDetails()).isEqualTo(expectedDetailedMessage);
            assertThat(error.getCode()).isEqualTo(expectedCode);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e.toString());
        }
    }

    @Test
    public void testNoRequest() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestType();
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings' is required.");
    }

    @Test
    public void testNoReplayAddress() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID,END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Header.ReplyAddress' is required.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoPublishedOutboundConfigurationFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(false);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL.getErrorCode(),
                "No published end point configuration is found by URL 'some_url'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoDevicesFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices();
        mockFindEndPointConfigurations();

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_END_DEVICES.getErrorCode(),
                "No devices have been found.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeDevicesWithMridNotFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .withEndDevice(END_DEVICE2_MRID, null)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6004")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find device(s) with MRID(s) 'a74e77e1-c397-41c8-8c3c-6ddab969047c'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeDevicesWithNameNotFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .withEndDevice(null, END_DEVICE2_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6005")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find device(s) with name(s) 'SPE01000002'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeDevicesWithMridAndNameNotFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .withEndDevice(null, END_DEVICE2_NAME)
                .withEndDevice(END_DEVICE2_MRID, null)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6006")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find device(s) with MRID(s) 'a74e77e1-c397-41c8-8c3c-6ddab969047c' and name(s) 'SPE01000002'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoReadingTypesFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_READING_TYPES.getErrorCode(),
                "No reading types have been found.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeReadingTypesWithMridNotFoundInSystemAndOnDevice() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6009")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Reading type(s) with MRID(s) '0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0' is(are) not found in the system.")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6012")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Reading type(s) is(are) not found on device 'SPE01000001': '11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeReadingTypesWithMridAndNamesNotFoundInSystem() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, null)
                .withReadingType(null, MONTHLY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6011")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails()
                        .equals("Reading type(s) with MRID(s) '0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0' and name(s) '[Monthly] Secondary Delta A+ (kWh)' is(are) not found in the system.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSomeReadingTypesWithNamesNotFoundInSystem() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(null, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6010")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails()
                        .equals("Reading type(s) with name(s) '[15-minute] Secondary Delta A+ (kWh)' is(are) not found in the system.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testIncorrectSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod("Something", JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[0].source' contains unsupported value 'Something'. Must be one of: 'System', 'Meter' or 'Hybrid'");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEmptySource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(null, JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Reading.source' is required.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testIncorrectSourceInSyncMode() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod("Something", JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[0].source' contains unsupported value 'Something'. Must be one of: System");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEndDevicesBulkOperationNotSupported() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .withEndDevice(END_DEVICE2_MRID, END_DEVICE2_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType);
        mockFindEndDevices(meter1);
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM0002")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Bulk operation is not supported on 'GetMeterReadings.EndDevice', only first element is processed")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEndDeviceWithoutMridAndName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        meterReadingsRequestType.getGetMeterReadings().getEndDevice().add(new EndDevice());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under 'GetMeterReadings.EndDevice[0]' for identification purpose.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testAsyncModeWrongScheduleStrategy() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withScheduleStrategy("Something wrong")
                        .get())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.SCHEDULE_STRATEGY_NOT_SUPPORTED.getErrorCode(),
                "Schedule strategy 'Something wrong' is not supported. The possible values are: 'Run now' and 'Use schedule'");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testAsyncModeWrongConnectionMethod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.METER.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod("something wrong")
                        .get())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6020")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("The required connection method 'something wrong' wasn't found for communication task 'comTaskName' of device 'deviceName'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }


    @Test
    public void testAsyncModeWrongDataSourceNameType() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(LOAD_PROFILE_NAME, "wrong load profile")
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.DATA_SOURCE_NAME_TYPE_NOT_FOUND.getErrorCode(),
                "Data source name type 'wrong load profile' is not found in the element 'GetMeterReadings.Reading[0]'. Possible values: Load Profile or Register Group.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testAsyncModeWrongLoadProfile() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource("wrong load profile name", LOAD_PROFILE.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockLoadProfileType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6024")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find load profile with name 'wrong load profile name' under element 'GetMeterReadings.Reading[0]'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testAsyncModeNoDataSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockLoadProfileType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6022")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("At least one correct 'GetMeterReadings.ReadingType' or 'GetMeterReadings.Reading.dataSource' must be specified in the request under element 'GetMeterReadings.Reading[0]'")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testAsyncModeWrongRegisterGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource("wrong register group name", REGISTER_GROUP.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockRegisterGroupType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6023")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find register group with name 'wrong register group name' under element 'GetMeterReadings.Reading[0]'.")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeCorrelationId() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeScheduleStrategy() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withScheduleStrategy("Run now")
                        .get())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeConnectionMethod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .get())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testSuccessCaseAsyncModeMeterSourceLoadProfile() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.METER.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(LOAD_PROFILE_NAME, LOAD_PROFILE.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockLoadProfileType();
        mockLoadProfile();
        mockDeviceMessage();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeLoadProfile() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(LOAD_PROFILE_NAME, LOAD_PROFILE.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockLoadProfileType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeSomeLoadProfileNotFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(LOAD_PROFILE_NAME, LOAD_PROFILE.getName())
                        .withDataSource("wrong load profile name", LOAD_PROFILE.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockLoadProfileType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6024")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find load profile with name 'wrong load profile name' under element 'GetMeterReadings.Reading[0]'.")));
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeRegisterGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(REGISTER_GROUP_NAME, REGISTER_GROUP.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockRegisterGroupType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeSomeRegisterGroupNotFound() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withReading(ReadingBuilder.createRequest()
                        .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                        .withConnectionMethod(COM_TASK_NAME)
                        .withDataSource(REGISTER_GROUP_NAME, REGISTER_GROUP.getName())
                        .withDataSource("wrong register group name", REGISTER_GROUP.getName())
                        .get())
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes();
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);
        mockRegisterGroupType();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM6023")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("Couldn't find register group with name 'wrong register group name' under element 'GetMeterReadings.Reading[0]'.")));
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testSuccessCaseAsyncModeEndDeviceMeterSourceEmptyStartEndDates() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.METER.getSource(), null, null)
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseSyncModeCorrelationId() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        headerType.setCorrelationID("hello");
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo("hello");
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseSyncModeEndDevice() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertReadingTypes(meterReadings.getReadingType(), dailyReadingType, min15ReadingType);
        List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> readingQualityTypes = meterReadings.getReadingQualityType();
        assertReadingQualityTypeCodes(readingQualityTypes, SUSPECT.getCode(), RULE_13_FAILED.getCode(),
                REMOVED.getCode(), INFERRED.getCode(), ERROR_CODE.getCode());
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

        List<MeterReading> meterReadingList = meterReadings.getMeterReading();
        assertThat(meterReadingList.size() == 1);
        MeterReading reading = meterReadingList.get(0);
        List<IntervalBlock> readingBlocks = reading.getIntervalBlocks();
        assertRegularReadingTypeReferences(readingBlocks, DAILY_MRID, MIN15_MRID);
        List<IntervalReading> dailyReadings = getReadingsByReadingTypeMRID(readingBlocks, DAILY_MRID)
                .getIntervalReadings();
        assertThat(dailyReadings).hasSize(3);
        assertReading(dailyReadings.get(0), dailyReading2, suspect2, rule13Failed2);
        assertMissing(dailyReadings.get(1), mayDay(7), removed7);
        assertReading(dailyReadings.get(2), dailyReading9);
        List<IntervalReading> min15Readings = getReadingsByReadingTypeMRID(readingBlocks, MIN15_MRID)
                .getIntervalReadings();
        assertThat(min15Readings).hasSize(3);
        assertReading(min15Readings.get(0), min15Reading2, suspect2, inferred2);
        assertReading(min15Readings.get(1), min15Reading2_15, inferred2_15, errorCode2_15);
        assertReading(min15Readings.get(2), min15Reading9, inferred9);

        assertThat(reading.getMeter().getMRID().equals(meter1.getMRID()));
        assertThat(reading.getMeter().getNames().get(0).equals(meter1.getName()));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseSyncModeTimeNotMatchingWithContainer() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(false);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseAsyncModeEndDevice() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testSuccessCaseAsyncModeEndDeviceMeterSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.METER.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testSuccessCaseAsyncModeEndDeviceHybridSourceReadingNotRequired() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.HYBRID.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    @Ignore //TODO: need to fix
    public void testSuccessCaseAsyncModeEndDeviceHybridSourceReadingRequired() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.HYBRID.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType(MIN15_MRID, MIN15_FULL_ALIAS_NAME)
                .withEndDevice(END_DEVICE1_MRID, END_DEVICE1_NAME)
                .get();
        HeaderType headerType = new HeaderType();
        headerType.setAsyncReplyFlag(true);
        headerType.setReplyAddress(REPLY_ADDRESS);
        getMeterReadingsRequestMessage.setHeader(headerType);
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);
        mockFindReadingTypes(dailyReadingType, min15ReadingType);
        mockFindEndDevices(meter1);
        mockReadingTypesOnDevices();
        mockChannelsContainers();
        when(min15Channel.getLastDateTime()).thenReturn(MAY_1ST.toInstant().minus(15, ChronoUnit.MINUTES));

        mockFindEndPointConfigurations();
        mockWebServices(true);

        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();
        // sync reply of async mode doesn't contain any readings
        assertThat(meterReadings).isNull();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEndDeviceGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest().get();
        meterReadingsRequestType.getGetMeterReadings().getEndDeviceGroup().add(new EndDeviceGroup());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_ELEMENT.getErrorCode(),
                "Element 'EndDeviceGroup' under 'GetMeterReadings' is not supported");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUsagePointGroup() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType meterReadingsRequestType = GetMeterReadingsRequestBuilder.createRequest().get();
        meterReadingsRequestType.getGetMeterReadings().getUsagePointGroup().add(new UsagePointGroup());
        getMeterReadingsRequestMessage.setRequest(meterReadingsRequestType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_ELEMENT.getErrorCode(),
                "Element 'UsagePointGroup' under 'GetMeterReadings' is not supported");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoUsagePoints() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'GetMeterReadings.UsagePoint' cannot be empty.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEmptyUsagePointMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(" \t \n \r ", USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.UsagePoint[0].mRID' is empty or contains only white spaces");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUsagePointIsNotFoundByMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(ANOTHER_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                "No usage point is found by MRID '" + ANOTHER_MRID + "'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEmptyUsagePointIdentifyingName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, " \r \n \t ")
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.UsagePoint[0].Names[?(@.NameType.name=='UsagePointName')].name' is empty or contains only white spaces");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSeveralUsagePointIdentifyingNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        Name another = GetMeterReadingsRequestBuilder.name(ANOTHER_NAME, UsagePointNameType.USAGE_POINT_NAME.getNameType()).orElse(null);
        request.getGetMeterReadings().getUsagePoint().get(0).getNames().add(another);
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_LIST_SIZE.getErrorCode(),
                "The list of 'GetMeterReadings.UsagePoint[0].Names[?(@.NameType.name=='UsagePointName')]' has unsupported size. Must be of size 1");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoMRIDAndNameInUsagePoint() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, null)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' with 'NameType.name' = 'UsagePointName' is required under 'GetMeterReadings.UsagePoint[0]' for identification purpose");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUsagePointIsNotFoundByName() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, ANOTHER_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                "No usage point is found by name '" + ANOTHER_NAME + "'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testEmptyReadingTypeMRID() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingType("\t\n \t\r", DAILY_FULL_ALIAS_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.ReadingType[1].mRID' is empty or contains only white spaces");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.ReadingType[2].Names[0].name' is empty or contains only white spaces");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSeveralReadingTypeFullAliasNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withReadingType(null, DAILY_FULL_ALIAS_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .get();
        Name oneMoreName = GetMeterReadingsRequestBuilder.name(DAILY_MRID).orElse(null);
        request.getGetMeterReadings().getReadingType().get(0).getNames().add(oneMoreName);
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_LIST_SIZE.getErrorCode(),
                "The list of 'GetMeterReadings.ReadingType[0].Names' has unsupported size. Must be of size 1");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under 'GetMeterReadings.ReadingType[3]' for identification purpose.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoReadings() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        getMeterReadingsRequestMessage.setRequest(GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .get());

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'GetMeterReadings.Reading' cannot be empty.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoTimePeriodInReading() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        request.getGetMeterReadings().getReading().get(0).setTimePeriod(null);
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);
        mockEffectiveMetrologyConfigurationsWithData();

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Reading[0].timePeriod' is required.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testNoTimePeriodStartInReading() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), null, JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'GetMeterReadings.Reading[1].timePeriod.start' is required.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUnsupportedHybridReadingSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.HYBRID.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[1].source' contains unsupported value 'Hybrid'. Must be one of: System");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUnsupportedMeterReadingSource() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.METER.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.UNSUPPORTED_VALUE.getErrorCode(),
                "Element 'GetMeterReadings.Reading[1].source' contains unsupported value 'Meter'. Must be one of: System");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testInvalidTimePeriod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JULY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);
        mockEffectiveMetrologyConfigurationsWithData();

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode(),
                "Can't construct a valid time period: provided start '2017-07-01T00:00:00+12:00' is after or coincides with the end '2017-06-01T00:00:00+12:00'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testInvalidEmptyTimePeriod() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), MAY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        mockEffectiveMetrologyConfigurationsWithData();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(min15ReadingType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode(),
                "Can't construct a valid time period: provided start '2017-05-01T00:00:00+12:00' is after or coincides with the end '2017-05-01T00:00:00+12:00'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testUnknownPurposeNames() throws Exception {
        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, "Yo", "Billing", "Brother", "C'mon", "Gimme", "Information")
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), JUNE_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        mockFindReadingTypes(dailyReadingType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                MessageSeeds.NO_PURPOSES_WITH_NAMES.getErrorCode(),
                "No metrology purposes are found for names: 'Brother', 'C'mon', 'Gimme', 'Yo'.");
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), null)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(DAILY_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                ERROR_CODE,
                ERROR);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), null)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .withReadingTypeMRIDs(BULK_MRID, DAILY_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);

        // Business method & assertions
        assertFaultMessage(() -> executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage),
                null,
                ERROR);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseWithUsagePointMRIDAndFullFiltering() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, ANOTHER_NAME, "Billing", "Information")
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), null)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), JUNE_1ST.toInstant(), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(BULK_MRID)
                .withReadingTypeFullAliasNames(DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);

        // Business method
        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

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
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseWithUsagePointNameAndNoFiltering() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(null, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), null)
                .withReadingTypeMRIDs(BULK_MRID)
                .withReadingTypeMRIDs(MONTHLY_MRID)
                .withReadingTypeMRIDs(DAILY_MRID)
                .withReadingTypeMRIDs(MIN15_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType, registerReadingType, monthlyReadingType, min15ReadingType);

        // Business method
        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

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
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
                .withReadingTypeMRIDs(BULK_MRID)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), MAY_1ST.toInstant(), mayDay(9)) // only effectiveMC1 matches,
                // but its only channel has neither readings nor qualities in this period
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(min15ReadingType);

        // Business method
        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseWithTimePeriodNotMatchingWithContainer() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();
        when(checkContainer.getChannels()).thenReturn(Collections.emptyList());
        mockFindReadingTypes(min15ReadingType);

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), mayDay(9), mayDay(10)) // only matches with checkContainer that has no channels
                .withReadingTypeMRIDs(BULK_MRID)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);

        // Business method
        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testSuccessCaseWithFilterThatFetchesNothing() throws Exception {
        mockEffectiveMetrologyConfigurationsWithData();

        // Prepare request
        GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage = getMeterReadingsMessageObjectFactory.createGetMeterReadingsRequestMessageType();
        GetMeterReadingsRequestType request = GetMeterReadingsRequestBuilder.createRequest()
                .withUsagePoint(USAGE_POINT_MRID, USAGE_POINT_NAME, CHECK_NAME)
                .withTimePeriod(ReadingSourceEnum.SYSTEM.getSource(), mayDay(1), JULY_1ST.toInstant())
                .withReadingTypeMRIDs(DAILY_MRID, MONTHLY_MRID, BULK_MRID)
                .withReadingTypeFullAliasNames(DAILY_FULL_ALIAS_NAME, MONTHLY_FULL_ALIAS_NAME, BULK_FULL_ALIAS_NAME)
                .withReadingType(DAILY_MRID, DAILY_FULL_ALIAS_NAME)
                .get();
        getMeterReadingsRequestMessage.setRequest(request);
        mockFindReadingTypes(dailyReadingType);

        // Business method
        MeterReadingsResponseMessageType response = executeMeterReadingsEndpoint.getMeterReadings(getMeterReadingsRequestMessage);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterReadings");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        MeterReadings meterReadings = response.getPayload().getMeterReadings();

        assertThat(meterReadings.getReadingType()).isEmpty();
        assertThat(meterReadings.getReadingQualityType()).isEmpty();
        assertThat(meterReadings.getMeterReading()).isEmpty();
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
