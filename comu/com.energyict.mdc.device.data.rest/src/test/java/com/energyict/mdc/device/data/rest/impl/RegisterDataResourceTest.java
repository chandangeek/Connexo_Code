package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.masterdata.RegisterType;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Mock(extraInterfaces = NumericalRegister.class)
    private Register register;
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
    private MeterActivation meterActivation;

    public static final Instant BILLING_READING_INTERVAL_END = Instant.ofEpochMilli(1410786196000L);
    public static final Instant BILLING_READING_INTERVAL_START = Instant.ofEpochMilli(1409570229000L);
    public static final Instant READING_TIMESTAMP = Instant.ofEpochMilli(1409570229000L);

    public static final Instant NOW = ZonedDateTime.of(2014, 10, 01, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private ReadingQualityType readingQualityTypeConfirmed = new ReadingQualityType("3.10.1");

    public RegisterDataResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(device.getRegisters()).thenReturn(Arrays.asList(register));
        when(numericalRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(register.getRegisterSpec()).thenReturn(numericalRegisterSpec);
        when(register.getReadingType()).thenReturn(readingType);

        BillingReading billingReading = mockBillingReading(actualReading1);
        when(actualReading1.edited()).thenReturn(true);
        ReadingQuality quality = mock(ReadingQuality.class);
        when(quality.getType()).thenReturn(new ReadingQualityType("3.7.1"));
        List qualities = new ArrayList<>();
        qualities.add(quality);
        when(actualReading1.getReadingQualities()).thenReturn(qualities);
        when(billingReading.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));

        NumericalReading numericalReading = mockNumericalReading(actualReading2);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading2.edited()).thenReturn(true);
        NumericalReading numericalReadingConfirmed = mockNumericalReading(actualReading3);
        when(numericalReadingConfirmed.getValidationStatus()).thenReturn(Optional.of(dataValidationStatus));
        when(actualReading3.confirmed()).thenReturn(true);
        ReadingQualityRecord readingQualityEdited = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC));
        doReturn(Arrays.asList(readingQualityEdited)).when(actualReading2).getReadingQualities();
        ReadingQualityRecord readingQualityConfirmed = mockReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED));
        doReturn(Arrays.asList(readingQualityConfirmed)).when(actualReading3).getReadingQualities();

        when(register.getReadings(any(Interval.class))).thenReturn(Arrays.asList(billingReading, numericalReading, numericalReadingConfirmed));

        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(meteringChannel));
        doReturn(Arrays.asList(readingType)).when(meteringChannel).getReadingTypes();
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(any(Register.class), any(Instant.class))).thenReturn(false);

        EstimationRule estimationRule = mock(EstimationRule.class);
        ReadingQualityType readingQualityType = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, (int) estimationRule.getId());
        ReadingQualityRecord readingQualityEstimated = mockReadingQuality(readingQualityType);
        when(readingQualityEstimated.hasEstimatedCategory()).thenReturn(true);
        when(estimationRule.getId()).thenReturn(13L);
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        when(estimationRule.getRuleSet()).thenReturn(estimationRuleSet);
        when(estimationRuleSet.getId()).thenReturn(15L);
        when(estimationRule.getName()).thenReturn("EstimationRule");
        doReturn(Optional.of(estimationRule)).when(estimationService).findEstimationRuleByQualityType(readingQualityType);
        doReturn(Arrays.asList(readingQualityEstimated, readingQualityConfirmed)).when(dataValidationStatus).getReadingQualities();
    }

    private ReadingQualityRecord mockReadingQuality(ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        return readingQualityRecord;
    }

    private NumericalReading mockNumericalReading(ReadingRecord actualReading) {
        NumericalReading numericalReading = mock(NumericalReading.class);
        Quantity quantity = Quantity.create(BigDecimal.TEN, "M");
        when(numericalReading.getQuantity()).thenReturn(quantity);
        when(numericalReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.empty());
        when(numericalReading.getActualReading()).thenReturn(actualReading);
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
    public void testGetRegisterData() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(numericalRegisterSpec.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device.getId()).thenReturn(1L);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);

        long intervalStart = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        long intervalEnd = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli();
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":" + intervalStart + "},{\"property\":\"intervalEnd\",\"value\":"+ intervalEnd + "}]");
        Map json = target("devices/1/registers/1/data")
                .queryParam("filter", filter)
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(3);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("billing");
        assertThat(jsonModel.<String>get("$.data[0].modificationFlag")).isEqualTo("ADDED");
        assertThat(jsonModel.<Number>get("$.data[0].estimatedByRule.id")).isEqualTo(13);
        assertThat(jsonModel.<Number>get("$.data[0].estimatedByRule.ruleSetId")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[0].estimatedByRule.name")).isEqualTo("EstimationRule");
        assertThat(jsonModel.<List<?>>get("$.data[0].estimatedByRule.properties")).isEmpty();
        assertThat(jsonModel.<String>get("$.data[1].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[1].modificationFlag")).isEqualTo("EDITED");
        assertThat(jsonModel.<Number>get("$.data[1].estimatedByRule.id")).isEqualTo(13);
        assertThat(jsonModel.<Number>get("$.data[1].estimatedByRule.ruleSetId")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[1].estimatedByRule.name")).isEqualTo("EstimationRule");
        assertThat(jsonModel.<List<?>>get("$.data[1].estimatedByRule.properties")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.data[2].isConfirmed")).isEqualTo(true);
    }

    @Test
    public void testPutRegisterData() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
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
    public void testPutConfirmRegisterData() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
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