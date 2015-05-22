package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.masterdata.RegisterType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public class RegisterDataResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private DataValidationStatus dataValidationStatus;
    @Mock
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
    private ReadingRecord actualReading1, actualReading2;
    @Mock
    private Channel meteringChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private List list;

    public static final Instant BILLING_READING_INTERVAL_END = Instant.ofEpochMilli(1410786196000L);
    public static final Instant BILLING_READING_INTERVAL_START = Instant.ofEpochMilli(1409570229000L);
    public static final Instant READING_TIMESTAMP = Instant.ofEpochMilli(1409570229000L);

    public static final Instant NOW = ZonedDateTime.of(2014, 10, 01, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    public RegisterDataResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(device.getRegisters()).thenReturn(Arrays.asList(register));
        when(numericalRegisterSpec.getRegisterType()).thenReturn(registerType);
        when(register.getRegisterSpec()).thenReturn(numericalRegisterSpec);
        when(register.getReadingType()).thenReturn(readingType);
        when(numericalRegisterSpec.getUnit()).thenReturn(Unit.get("M"));
        BillingReading billingReading = mock(BillingReading.class);
        NumericalReading numericalReading = mock(NumericalReading.class);
        Quantity quantity = Quantity.create(BigDecimal.TEN, "M");
        when(billingReading.getQuantity()).thenReturn(quantity);
        when(numericalReading.getQuantity()).thenReturn(quantity);
        when(billingReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        when(numericalReading.getTimeStamp()).thenReturn(READING_TIMESTAMP);
        when(numericalReading.getValidationStatus()).thenReturn(Optional.empty());
        Range<Instant> interval = Ranges.openClosed(BILLING_READING_INTERVAL_START, BILLING_READING_INTERVAL_END);
        when(billingReading.getRange()).thenReturn(Optional.of(interval));
        when(billingReading.getValidationStatus()).thenReturn(Optional.empty());
        when(register.getReadings(any(Interval.class))).thenReturn(Arrays.asList(billingReading, numericalReading));
        when(billingReading.getActualReading()).thenReturn(actualReading1);
        when(actualReading1.edited()).thenReturn(true);
        when(actualReading2.wasAdded()).thenReturn(true);
        when(numericalReading.getActualReading()).thenReturn(actualReading2);
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(meteringChannel));
        doReturn(Arrays.asList(readingType)).when(meteringChannel).getReadingTypes();
        when(list.contains(readingType)).thenReturn(true);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(any(Register.class), any(Instant.class))).thenReturn(false);
        when(deviceValidation.getValidationStatus(any(Register.class), anyListOf(ReadingRecord.class), any(Range.class))).thenReturn(new ArrayList<>());
    }

    @Test
    public void testGetRegisterData() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        when(numericalRegisterSpec.getId()).thenReturn(1L);
        when(device.getId()).thenReturn(1L);

        Map json = target("devices/1/registers/1/data")
                .queryParam("intervalStart", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .queryParam("intervalEnd", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(2);
        assertThat(jsonModel.<String>get("$.data[0].type")).isEqualTo("billing");
        assertThat(jsonModel.<String>get("$.data[0].modificationFlag")).isEqualTo("EDITED");
        assertThat(jsonModel.<String>get("$.data[1].type")).isEqualTo("numerical");
        assertThat(jsonModel.<String>get("$.data[1].modificationFlag")).isEqualTo("ADDED");
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
        verify(registerDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}