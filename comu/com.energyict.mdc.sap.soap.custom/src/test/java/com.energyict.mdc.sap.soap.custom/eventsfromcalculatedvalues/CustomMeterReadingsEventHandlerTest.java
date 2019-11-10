/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Unit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATT;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Unit.kW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomMeterReadingsEventHandlerTest {

    private final String DEVICE_TYPE_NAME = "deviceTypeName";
    private final String READING_TYPE_MRID_1 = "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private final String READING_TYPE_MRID_2 = "0.0.4.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private final Instant METER_CREATED_DATE_TIME = LocalDate.of(2019, Month.OCTOBER, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

    private CustomMeterReadingsEventHandler customMeterReadingsEventHandler;
    private String mRid = "1";
    private long meterId = 1;
    private CustomPropertySetValues values = CustomPropertySetValues.empty();
    private List<ReadingInfo> readings = new ArrayList<>();

    @Mock
    private EventService eventService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private NlsService nlsService;
    @Mock
    private CustomSAPDeviceEventHandler handler;
    @Mock
    protected Thesaurus thesaurus;
    @Mock
    private LocalEvent event;
    @Mock
    private Meter meter;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private ReadingInfo readingInfo1, readingInfo2;
    @Mock
    private Reading reading, reading2;
    @Mock
    private ReadingType readingType, readingType2;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private CustomPropertySet customPropertySet;


    @Before
    public void setup() {
        when(event.getSource()).thenReturn(readingStorer);
        when(readingStorer.getReadings()).thenReturn(readings);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);

        when(deviceService.findDeviceByMeterId(meterId)).thenReturn(Optional.of(device));
        when(device.getMeter()).thenReturn(meter);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(deviceType.getCustomPropertySets()).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(meter.getMRID()).thenReturn(mRid);
        when(meter.getId()).thenReturn(meterId);
        when(readingType.getMRID()).thenReturn(READING_TYPE_MRID_1);
        when(meteringService.getReadingType(READING_TYPE_MRID_1)).thenReturn(Optional.of(readingType));
        when(readingInfo1.getMeter()).thenReturn(Optional.ofNullable(meter));
        when(readingInfo1.getReading()).thenReturn(reading);
        when(readingInfo1.getReadingType()).thenReturn(readingType);
        when(reading.getTimeStamp()).thenReturn(METER_CREATED_DATE_TIME);
    }

    @Test
    public void topicMatcherDoesNotReturnEmpty() {
        assertThat(this.getInstance().getTopicMatcher()).isNotEmpty();
    }

    @Test
    public void testPowerFactorEventMissingReactiveReadingTypeFailure() {
        customMeterReadingsEventHandler = this.getInstance();
        setPowerFactorEventParameters();
        when(readingInfo2.getReading()).thenReturn(reading2);
        when(reading2.getValue()).thenReturn(BigDecimal.TEN);
        when(reading2.getTimeStamp()).thenReturn(LocalDate.of(2019, Month.OCTOBER, 2).atStartOfDay().toInstant(ZoneOffset.UTC));

        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_MISSING_READING);
    }

    @Test
    public void testPowerFactorEventNullValuesFailure() {
        customMeterReadingsEventHandler = this.getInstance();
        setPowerFactorEventParameters();
        readings.add(readingInfo2);
        when(reading.getValue()).thenReturn(BigDecimal.ZERO);

        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_VALUES_ARE_NULL);
    }

    @Test
    public void testPowerFactorNotEvent() {
        customMeterReadingsEventHandler = this.getInstance();
        setPowerFactorEventParameters();
        values.setProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName(), BigDecimal.ONE);
        values.setProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName(), new BigDecimal(30));
        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler, never()).handle(any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testPowerFactorEvent() {
        customMeterReadingsEventHandler = this.getInstance();
        setPowerFactorEventParameters();
        readings.add(readingInfo2);
        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler).handle(any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testMaxDemandEvent() {
        customMeterReadingsEventHandler = this.getInstance();
        setMaxDemandEventParameters();

        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler).handle(any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testMaxDemandNotEvent() {
        customMeterReadingsEventHandler = this.getInstance();
        setMaxDemandEventParameters();
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        values.setProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), Unit.MW);

        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler, never()).handle(any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testCtRatioEvent() {
        customMeterReadingsEventHandler = this.getInstance();
        when(customPropertySet.getId()).thenReturn(CTRatioCustomPropertySet.CPS_ID);
        when(reading.getValue()).thenReturn(BigDecimal.TEN);
        readings.add(readingInfo1);
        customMeterReadingsEventHandler.ctRatioEventReadingTypes.put(DEVICE_TYPE_NAME, READING_TYPE_MRID_1);
        values.setProperty(CTRatioDomainExtension.FieldNames.FLAG.javaName(), true);
        values.setProperty(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName(), BigDecimal.ONE);
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(values);

        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler).handle(any(CalculatedEventRecordImpl.class));
    }


    @Test
    public void testException() {
        customMeterReadingsEventHandler = this.getInstance();
        setPowerFactorEventParameters();
        when(reading.getValue()).thenReturn(null);
        // Business method
        customMeterReadingsEventHandler.handle(event);

        // Asserts
        verify(this.handler, never()).handle(any(CalculatedEventRecordImpl.class));
    }

    private void setPowerFactorEventParameters() {
        when(customPropertySet.getId()).thenReturn(PowerFactorCustomPropertySet.CPS_ID);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getUnit()).thenReturn(WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingInfo1.getReadingType()).thenReturn(readingType);

        when(readingInfo2.getMeter()).thenReturn(Optional.ofNullable(meter));
        when(readingInfo2.getReading()).thenReturn(reading);
        when(readingType2.isRegular()).thenReturn(true);
        when(readingType2.getMRID()).thenReturn(READING_TYPE_MRID_2);
        when(readingType2.getUnit()).thenReturn(VOLTAMPEREREACTIVEHOUR);
        when(readingType2.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(meteringService.getReadingType(READING_TYPE_MRID_2)).thenReturn(Optional.of(readingType2));
        when(readingInfo2.getReadingType()).thenReturn(readingType2);
        when(reading.getValue()).thenReturn(BigDecimal.TEN);

        readings.add(readingInfo1);
        customMeterReadingsEventHandler.powerFactorEventReadingTypes.put(DEVICE_TYPE_NAME, Pair.of(READING_TYPE_MRID_1, READING_TYPE_MRID_2));
        values.setProperty(PowerFactorDomainExtension.FieldNames.FLAG.javaName(), true);
        values.setProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName(), BigDecimal.TEN);
        values.setProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName(), BigDecimal.ONE);
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(values);
    }

    private void setMaxDemandEventParameters() {
        when(customPropertySet.getId()).thenReturn(MaxDemandCustomPropertySet.CPS_ID);
        when(readingType.getUnit()).thenReturn(WATT);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.MEGA);
        when(reading.getValue()).thenReturn(BigDecimal.TEN);
        readings.add(readingInfo1);
        customMeterReadingsEventHandler.maxDemandEventReadingTypes.put(DEVICE_TYPE_NAME, READING_TYPE_MRID_1);
        values.setProperty(MaxDemandDomainExtension.FieldNames.FLAG.javaName(), true);
        values.setProperty(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName(), BigDecimal.ONE);
        values.setProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), kW);
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(values);
    }

    private CustomMeterReadingsEventHandler getInstance() {
        return new CustomMeterReadingsEventHandler(eventService, meteringService, deviceService,
                customPropertySetService, nlsService, handler);
    }

    @After
    public void tearDown() {
        readings.clear();
    }
}
