package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.masterdata.RegisterType;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RegisterResourceReadingsTest extends DeviceDataRestApplicationJerseyTest {
    @Mock
    Device device;
    @Mock
    DeviceValidation deviceValidation;
    @Mock
    DataValidationStatus dataValidationStatus;
    @Mock
    NumericalRegister register;
    @Mock
    NumericalRegister billingRegister;
    @Mock
    RegisterType registerType;
    @Mock
    RegisterType billingRegisterType;
    @Mock
    ReadingType readingType;
    @Mock
    ReadingType billingReadingType;
    @Mock
    NumericalRegisterSpec numericalRegisterSpec;
    @Mock
    NumericalRegisterSpec billingRegisterSpec;
    @Mock
    AmrSystem amrSystem;
    @Mock
    Meter meter;
    @Mock
    ReadingRecord actualReading1, actualReading2, actualReading3, actualReading4;
    @Mock
    Channel meteringChannel;
    @Mock
    ChannelsContainer channelsContainer;

    static final Instant BILLING_READING_INTERVAL_END = Instant.ofEpochMilli(1410786196000L);
    static final Instant BILLING_READING_INTERVAL_START = Instant.ofEpochMilli(1409570229000L);
    static final Instant READING_TIMESTAMP = Instant.ofEpochMilli(1409570229000L);

    static final Instant NOW = ZonedDateTime.of(2014, 10, 01, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeIndex.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeIndex qualityCodeIndex = (QualityCodeIndex) invocationOnMock.getArguments()[0];
                    return qualityCodeIndex.getTranslationKey().getDefaultFormat();
                });
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeSystem.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeSystem qualityCodeSystem = (QualityCodeSystem) invocationOnMock.getArguments()[0];
                    return qualityCodeSystem.getTranslationKey().getDefaultFormat();
                });
        when(this.meteringTranslationService.getDisplayName(any(QualityCodeCategory.class)))
                .thenAnswer(invocationOnMock -> {
                    QualityCodeCategory qualityCodeCategory = (QualityCodeCategory) invocationOnMock.getArguments()[0];
                    return qualityCodeCategory.getTranslationKey().getDefaultFormat();
                });
    }

    @Before
    public void setUpStubs() {
        when(device.getRegisters()).thenReturn(Arrays.asList(register, billingRegister));
        when(numericalRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(numericalRegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(readingType.getFullAliasName()).thenReturn("NumericalReadingType");
        when(register.getRegisterSpec()).thenReturn(numericalRegisterSpec);
        when(register.getReadingType()).thenReturn(readingType);
        when(register.getCalculatedReadingType(any())).thenReturn(Optional.empty());
        when(register.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(register.getLastReadingDate()).thenReturn(Optional.empty());
        when(register.getRegisterSpecId()).thenReturn(1L);

        when(billingRegisterSpec.getRegisterType()).thenReturn(billingRegisterType);
        when(billingRegister.getRegisterSpec()).thenReturn(billingRegisterSpec);
        when(billingRegisterType.getReadingType()).thenReturn(billingReadingType);
        when(billingReadingType.getFullAliasName()).thenReturn("BillingReadingType");
        when(billingRegister.getReadingType()).thenReturn(billingReadingType);
        when(billingRegister.getCalculatedReadingType(any())).thenReturn(Optional.empty());
        when(billingRegister.getMultiplier(any())).thenReturn(Optional.empty());
        when(billingRegister.getLastReadingDate()).thenReturn(Optional.empty());
        when(billingRegister.getRegisterSpecId()).thenReturn(2L);


        BillingReading billingReading = mockBillingReading(BILLING_READING_INTERVAL_END);
        when(actualReading1.edited()).thenReturn(true);
        doReturn(Collections.singletonList(mockReadingQuality("2.7.1"))).when(actualReading1).getReadingQualities();
        when(billingReading.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));

        BillingReading billingReading2 = mockBillingReading(BILLING_READING_INTERVAL_END.plusSeconds(63113851));
        when(actualReading4.edited()).thenReturn(false);
        doReturn(Collections.singletonList(mockReadingQuality("2.7.1"))).when(actualReading4).getReadingQualities();
        when(billingReading2.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));

        NumericalReading numericalReading = mockNumericalReading(actualReading2);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading2.edited()).thenReturn(true);
        NumericalReading numericalReadingConfirmed = mockNumericalReading(actualReading3);
        when(numericalReadingConfirmed.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading3.confirmed()).thenReturn(true);
        ReadingQualityRecord readingQualityEdited = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC).getCode());
        doReturn(Collections.singletonList(readingQualityEdited)).when(actualReading2).getReadingQualities();
        ReadingQualityRecord readingQualityConfirmed = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED).getCode());
        doReturn(Collections.singletonList(readingQualityConfirmed)).when(actualReading3).getReadingQualities();

        when(register.getReadings(any(Interval.class))).thenReturn(Arrays.asList(numericalReading, numericalReadingConfirmed));
        when(billingRegister.getReadings(any())).thenReturn(Arrays.asList(billingReading, billingReading2));

        doReturn(Collections.singletonList(channelsContainer)).when(meter).getChannelsContainers();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(meteringChannel));
        doReturn(Collections.singletonList(readingType)).when(meteringChannel).getReadingTypes();
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(any(Register.class), any(Instant.class))).thenReturn(false);

        EstimationRule estimationRule = mock(EstimationRule.class);
        ReadingQualityType readingQualityType = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, (int) estimationRule.getId());
        ReadingQualityRecord readingQualityEstimated = mockReadingQuality(readingQualityType.getCode());
        when(readingQualityEstimated.hasEstimatedCategory()).thenReturn(true);
        when(estimationRule.getId()).thenReturn(13L);
        when(estimationRule.getName()).thenReturn("EstimationRule");
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        when(estimationRuleSet.getId()).thenReturn(15L);
        when(estimationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(readingQualityEstimated.hasEstimatedCategory()).thenReturn(true);
        doReturn(Optional.of(estimationRule)).when(estimationService).findEstimationRuleByQualityType(readingQualityType);
        doReturn(Arrays.asList(readingQualityEstimated, readingQualityConfirmed)).when(dataValidationStatus).getReadingQualities();
        when(topologyService.getSlaveChannel(any(com.energyict.mdc.device.data.Channel.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveRegister(any(Register.class), any(Instant.class))).thenReturn(Optional.empty());
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        return readingQuality;
    }

    private NumericalReading mockNumericalReading(ReadingRecord actualReading) {
        NumericalReading numericalReading = mock(NumericalReading.class);
        Quantity quantity = Quantity.create(BigDecimal.TEN, "M");
        when(numericalReading.getQuantity()).thenReturn(quantity);
        when(numericalReading.getCollectedValue()).thenReturn(Optional.of(quantity));
        when(numericalReading.getCalculatedValue()).thenReturn(Optional.empty());
        when(numericalReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.empty());
        when(numericalReading.getActualReading()).thenReturn(actualReading);
        when(numericalReading.getRange()).thenReturn(Optional.empty());
        when(numericalReading.getDelta()).thenReturn(Optional.empty());
        when(numericalReading.getEventDate()).thenReturn(Optional.empty());

        return numericalReading;
    }

    private BillingReading mockBillingReading(Instant intervalEndTimestamp) {
        BillingReading billingReading = mock(BillingReading.class);
        Quantity quantity = Quantity.create(BigDecimal.TEN, "M");
        when(billingReading.getQuantity()).thenReturn(quantity);
        when(billingReading.getCollectedValue()).thenReturn(Optional.of(quantity));
        when(billingReading.getCalculatedValue()).thenReturn(Optional.empty());
        when(billingReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        Range<Instant> interval = Ranges.openClosed(BILLING_READING_INTERVAL_START, intervalEndTimestamp);
        when(billingReading.getRange()).thenReturn(Optional.of(interval));
        when(billingReading.getValidationStatus()).thenReturn(Optional.empty());
        when(billingReading.getActualReading()).thenReturn(actualReading1);
        when(billingReading.getDelta()).thenReturn(Optional.empty());
        when(billingReading.getEventDate()).thenReturn(Optional.empty());
        return billingReading;
    }

    @Test
    public void testGetRegisterData() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(eq(register), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        when(topologyService.getDataLoggerRegisterTimeLine(eq(billingRegister), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(billingRegister, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("measurementTimeStart", intervalStart)
                .property("measurementTimeEnd", intervalEnd)
                .create();
        Map json = target("devices/1/registers/registerreadings")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(4);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[1].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[2].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[3].type")).isEqualTo("numerical");
    }

    @Test
    public void testGetRegisterDataWithToTime() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(eq(register), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        when(topologyService.getDataLoggerRegisterTimeLine(eq(billingRegister), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(billingRegister, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("toTimeStart", intervalStart)
                .property("toTimeEnd", intervalEnd)
                .create();
        Map json = target("devices/1/registers/registerreadings")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("numerical");
    }

    @Test
    public void testGetRegisterFilterRegisters() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(eq(register), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        when(topologyService.getDataLoggerRegisterTimeLine(eq(billingRegister), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(billingRegister, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("registers", Collections.singletonList(1))
                .create();
        Map json = target("devices/1/registers/registerreadings")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(2);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("numerical");
    }

    @Test
    public void testGetRegisterDataWithRegisterAndToTimeFilter() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(eq(register), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        when(topologyService.getDataLoggerRegisterTimeLine(eq(billingRegister), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(billingRegister, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("toTimeStart", intervalStart)
                .property("toTimeEnd", intervalEnd)
                .property("registers", Collections.singletonList(2))
                .create();
        Map json = target("devices/1/registers/registerreadings")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("numerical");
    }

    @Test
    public void testGetRegisterDataWithRegisterAndToTimeFilter2() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(eq(register), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        when(topologyService.getDataLoggerRegisterTimeLine(eq(billingRegister), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(billingRegister, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("toTimeStart", intervalStart)              // <<< criterion asking for Billing only registers
                .property("toTimeEnd", intervalEnd)                  // <<< criterion asking for Billing only registers
                .property("registers", Collections.singletonList(1)) // <<< criterion asking for Numerical registers
                .create();
        Map json = target("devices/1/registers/registerreadings")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(0);
    }
}
