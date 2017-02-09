package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterDataUpdater;
import com.energyict.mdc.masterdata.RegisterType;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterDataResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private DataValidationStatus dataValidationStatus;
    @Mock
    private NumericalRegister register;
    @Mock
    private RegisterType registerType;
    @Mock
    private ReadingType readingType;
    @Mock
    NumericalRegisterSpec numericalRegisterSpec;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private ReadingRecord actualReading1, actualReading2, actualReading3;
    @Mock
    private Channel meteringChannel;
    @Mock
    private ChannelsContainer channelsContainer;

    private static final Instant BILLING_READING_INTERVAL_END = Instant.ofEpochMilli(1410786196000L);
    private static final Instant BILLING_READING_INTERVAL_START = Instant.ofEpochMilli(1409570229000L);
    private static final Instant READING_TIMESTAMP = Instant.ofEpochMilli(1409570229000L);

    private static final Instant NOW = ZonedDateTime.of(2014, 10, 01, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

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
        when(device.getRegisters()).thenReturn(Arrays.asList(register));
        when(numericalRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(numericalRegisterSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(register.getRegisterSpec()).thenReturn(numericalRegisterSpec);
        when(register.getReadingType()).thenReturn(readingType);
        when(register.getCalculatedReadingType(any())).thenReturn(Optional.empty());
        when(register.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(register.getLastReadingDate()).thenReturn(Optional.empty());

        NumericalReading numericalReading1 = mockNumericalReading(actualReading1);
        when(actualReading1.edited()).thenReturn(true);

        doReturn(Arrays.asList(mockReadingQuality("2.7.1"))).when(actualReading1).getReadingQualities();
        when(numericalReading1.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));

        NumericalReading numericalReading2 = mockNumericalReading(actualReading2);
        when(numericalReading2.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading2.edited()).thenReturn(true);
        NumericalReading numericalReadingConfirmed = mockNumericalReading(actualReading3);
        when(numericalReadingConfirmed.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading3.confirmed()).thenReturn(true);
        ReadingQualityRecord readingQualityEdited = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC).getCode());
        doReturn(Arrays.asList(readingQualityEdited)).when(actualReading2).getReadingQualities();
        ReadingQualityRecord readingQualityConfirmed = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED).getCode());
        doReturn(Arrays.asList(readingQualityConfirmed)).when(actualReading3).getReadingQualities();

        when(register.getReadings(any(Interval.class))).thenReturn(Arrays.asList(numericalReading1, numericalReading2, numericalReadingConfirmed));

        doReturn(Arrays.asList(channelsContainer)).when(meter).getChannelsContainers();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(meteringChannel));
        doReturn(Arrays.asList(readingType)).when(meteringChannel).getReadingTypes();
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
        when(numericalReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.empty());
        when(numericalReading.getActualReading()).thenReturn(actualReading);
        when(numericalReading.getRange()).thenReturn(Optional.empty());
        when(numericalReading.getCollectedValue()).thenReturn(Optional.empty());
        when(numericalReading.getDelta()).thenReturn(Optional.empty());
        when(numericalReading.getCalculatedValue()).thenReturn(Optional.empty());
        when(numericalReading.getEventDate()).thenReturn(Optional.empty());
        return numericalReading;
    }

    private BillingReading mockBillingReading(ReadingRecord actialReading) {
        BillingReading billingReading = mock(BillingReading.class);
        Quantity quantity = Quantity.create(BigDecimal.TEN, "M");
        when(billingReading.getQuantity()).thenReturn(quantity);
        when(billingReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        Range<Instant> interval = Ranges.openClosed(BILLING_READING_INTERVAL_START, BILLING_READING_INTERVAL_END);
        when(billingReading.getRange()).thenReturn(Optional.of(interval));
        when(billingReading.getValidationStatus()).thenReturn(Optional.empty());
        when(billingReading.getActualReading()).thenReturn(actualReading1);
        return billingReading;
    }

    @Test
    public void testGetRegisterData() throws Exception {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        when(topologyService.getDataLoggerRegisterTimeLine(any(Register.class), any(Range.class))).thenReturn(Collections.singletonList(Pair.of(register, Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant
                .ofEpochMilli(intervalEnd)))));
        String filter = ExtjsFilter.filter()
                .property("intervalStart", intervalStart)
                .property("intervalEnd", intervalEnd)
                .create();
        Map json = target("devices/1/registers/1/data")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(3);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[0].modificationFlag")).isEqualTo(ReadingModificationFlag.ADDED.name());
        assertThat(jsonModel.<String>get("$.data[0].editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[0].editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<Number>get("$.data[0].estimatedByRule.id")).isEqualTo(13);
        assertThat(jsonModel.<Number>get("$.data[0].estimatedByRule.ruleSetId")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[0].estimatedByRule.name")).isEqualTo("EstimationRule");
        assertThat(jsonModel.<List<?>>get("$.data[0].estimatedByRule.properties")).isEmpty();
        assertThat(jsonModel.<String>get("$.data[0].estimatedByRule.application.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[0].estimatedByRule.application.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<List<?>>get("$.data[0].readingQualities")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0].cimCode")).isEqualTo("2.7.1");
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0].indexName")).isEqualTo("Manually added");
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0].systemName")).isEqualTo("MDC");
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0].categoryName")).isEqualTo("Edited");

        assertThat(jsonModel.<String>get("$.data[1].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[1].modificationFlag")).isEqualTo(ReadingModificationFlag.EDITED.name());
        assertThat(jsonModel.<String>get("$.data[1].editedInApp.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[1].editedInApp.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<Number>get("$.data[1].estimatedByRule.id")).isEqualTo(13);
        assertThat(jsonModel.<Number>get("$.data[1].estimatedByRule.ruleSetId")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[1].estimatedByRule.name")).isEqualTo("EstimationRule");
        assertThat(jsonModel.<List<?>>get("$.data[1].readingQualities")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[1].readingQualities[0].cimCode")).isEqualTo("2.7.0");
        assertThat(jsonModel.<String>get("$.data[1].readingQualities[0].indexName")).isEqualTo("Manually edited - generic");
        assertThat(jsonModel.<String>get("$.data[1].readingQualities[0].systemName")).isEqualTo("MDC");
        assertThat(jsonModel.<String>get("$.data[1].readingQualities[0].categoryName")).isEqualTo("Edited");

        assertThat(jsonModel.<List<?>>get("$.data[1].estimatedByRule.properties")).isEmpty();
        assertThat(jsonModel.<String>get("$.data[1].estimatedByRule.application.id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[1].estimatedByRule.application.name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<Boolean>get("$.data[2].isConfirmed")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.data[2].confirmedInApps[0].id")).isEqualTo(QualityCodeSystem.MDC.name());
        assertThat(jsonModel.<String>get("$.data[2].confirmedInApps[0].name")).isEqualTo("MultiSense");
        assertThat(jsonModel.<List<?>>get("$.data[2].readingQualities")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[2].readingQualities[0].cimCode")).isEqualTo("2.10.1");
        assertThat(jsonModel.<String>get("$.data[2].readingQualities[0].indexName")).isEqualTo("Manually accepted");
        assertThat(jsonModel.<String>get("$.data[0].readingQualities[0].systemName")).isEqualTo("MDC");
        assertThat(jsonModel.<String>get("$.data[2].readingQualities[0].categoryName")).isEqualTo("Questionable");
    }

    @Test
    public void testGetRegisterDataSuspectsOnly() throws Exception {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        String filter = ExtjsFilter.filter()
                .property("intervalStart", intervalStart)
                .property("intervalEnd", intervalEnd)
                .property("suspect", "suspect")
                .create();
        Map json = target("devices/1/registers/1/data")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testPutRegisterData() {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(device.getId()).thenReturn(1L);
        when(readingType.getMRID()).thenReturn("mRID");
        RegisterDataUpdater registerDataUpdater = mock(RegisterDataUpdater.class);
        when(registerDataUpdater.removeReading(any(Instant.class))).thenReturn(registerDataUpdater);
        when(registerDataUpdater.editReading(any(BaseReading.class))).thenReturn(registerDataUpdater);
        when(register.startEditingData()).thenReturn(registerDataUpdater);

        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        numericalReadingInfo.value = BigDecimal.TEN;
        numericalReadingInfo.timeStamp = READING_TIMESTAMP;

        Response response = target("devices/1/registers/1/data/1").request().put(Entity.json(numericalReadingInfo));
        verify(registerDataUpdater).editReading(any());
        verify(registerDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPutRegisterDataExceedsOverflow() throws IOException {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getOverflowValue()).thenReturn(Optional.of(BigDecimal.ONE));
        when(device.getId()).thenReturn(1L);
        when(readingType.getMRID()).thenReturn("mRID");

        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        numericalReadingInfo.value = BigDecimal.TEN;
        numericalReadingInfo.timeStamp = READING_TIMESTAMP;

        Response response = target("devices/1/registers/1/data/1").request().put(Entity.json(numericalReadingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo("ValueMayNotExceedOverflowValue");
    }

    @Test
    public void testPutConfirmRegisterData() {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(device.getId()).thenReturn(1L);
        when(readingType.getMRID()).thenReturn("mRID");
        RegisterDataUpdater registerDataUpdater = mock(RegisterDataUpdater.class);
        when(registerDataUpdater.removeReading(any(Instant.class))).thenReturn(registerDataUpdater);
        when(registerDataUpdater.confirmReading(any(BaseReading.class))).thenReturn(registerDataUpdater);
        when(register.startEditingData()).thenReturn(registerDataUpdater);

        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        numericalReadingInfo.isConfirmed = true;
        numericalReadingInfo.timeStamp = READING_TIMESTAMP;

        Response response = target("devices/1/registers/1/data/1").request().put(Entity.json(numericalReadingInfo));
        verify(registerDataUpdater).confirmReading(any());
        verify(registerDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}