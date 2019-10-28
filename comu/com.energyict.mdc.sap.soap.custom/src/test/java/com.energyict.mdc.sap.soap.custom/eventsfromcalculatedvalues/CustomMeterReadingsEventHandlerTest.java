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
import com.elster.jupiter.metering.EventType;
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
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CustomPropertySets;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Units;
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

import static com.elster.jupiter.cbo.ReadingTypeUnit.TESLA;
import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATT;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.cbo.TimeAttribute.MINUTE10;
import static com.elster.jupiter.cbo.TimeAttribute.MINUTE15;
import static com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Units.kW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
        customMeterReadingsEventHandler = new CustomMeterReadingsEventHandler();
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
        setPowerFactorEventParameters();
        when(readingInfo2.getReading()).thenReturn(reading2);
        when(reading2.getValue()).thenReturn(BigDecimal.TEN);
        when(reading2.getTimeStamp()).thenReturn(LocalDate.of(2019, Month.OCTOBER, 2).atStartOfDay().toInstant(ZoneOffset.UTC));

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_MISSING_READING);
    }

    @Test
    public void testPowerFactorEventNullValuesFailure() {
        setPowerFactorEventParameters();
        when(reading.getValue()).thenReturn(BigDecimal.ZERO);

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_VALUES_ARE_NULL);
    }

    @Test
    public void testPowerFactorEventReadingTypeFailure() {
        setPowerFactorEventParameters();
        when(meteringService.getReadingType(READING_TYPE_MRID_1)).thenReturn(Optional.empty());
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.READING_TYPE_NOT_FOUND);
    }

    @Test
    public void testPowerFactorEventUnitFailure() {
        setPowerFactorEventParameters();
        when(readingType.getUnit()).thenReturn(TESLA);
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.UNEXPECTED_UNIT_ON_READING_TYPE);
    }

    @Test
    public void testPowerFactorEventInvalidReadingTypeFailure() {
        setPowerFactorEventParameters();
        when(readingType.isRegular()).thenReturn(false);
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_INVALID_READING_TYPE);
    }

    @Test
    public void testPowerFactorEventInvalidReadingTypeNotTheSameIntervalFailure() {
        setPowerFactorEventParameters();
        when(readingType.getMeasuringPeriod()).thenReturn(MINUTE15);
        when(readingType2.getMeasuringPeriod()).thenReturn(MINUTE10);
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.POWER_FACTOR_READING_TYPES_MUST_HAVE_THE_SAME_INTERVAL);
    }

    @Test
    public void testPowerFactorNotEvent() {
        setPowerFactorEventParameters();
        values.setProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName(), BigDecimal.ONE);
        values.setProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName(), new BigDecimal(30));
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService, times(0)).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testPowerFactorEvent() {
        setPowerFactorEventParameters();
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testMaxDemandEventReadingTypeFailure() {
        setMaxDemandEventParameters();
        when(meteringService.getReadingType(READING_TYPE_MRID_1)).thenReturn(Optional.empty());

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.READING_TYPE_NOT_FOUND);
    }

    @Test
    public void testMaxDemandEventUnitFailure() {
        setMaxDemandEventParameters();
        when(readingType.getUnit()).thenReturn(TESLA);


        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.UNEXPECTED_UNIT_ON_READING_TYPE);
    }

    @Test
    public void testMaxDemandEvent() {
        setMaxDemandEventParameters();

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testMaxDemandNotEvent() {
        setMaxDemandEventParameters();
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        values.setProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), Units.MW.getValue());

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService, times(0)).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
    }

    @Test
    public void testCtRatioEvent() {
        when(customPropertySet.getId()).thenReturn(CTRatioCustomPropertySet.CPS_ID);
        when(reading.getValue()).thenReturn(BigDecimal.TEN);
        readings.add(readingInfo1);
        CustomPropertySets.getCTRatioEventReadingTypes().put(DEVICE_TYPE_NAME, READING_TYPE_MRID_1);
        values.setProperty(CTRatioDomainExtension.FieldNames.FLAG.javaName(), true);
        values.setProperty(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName(), BigDecimal.ONE);
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(values);

        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
    }


    @Test
    public void testException() {
        setPowerFactorEventParameters();
        when(reading.getValue()).thenReturn(null);
        // Business method
        this.getInstance().handle(event);

        // Asserts
        verify(this.eventService, times(0)).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), any(CalculatedEventRecordImpl.class));
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
        readings.add(readingInfo2);
        CustomPropertySets.getPowerFactorEventReadingTypes().put(DEVICE_TYPE_NAME, Pair.of(READING_TYPE_MRID_1, READING_TYPE_MRID_2));
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
        CustomPropertySets.getMaxDemandEventReadingTypes().put(DEVICE_TYPE_NAME, READING_TYPE_MRID_1);
        values.setProperty(MaxDemandDomainExtension.FieldNames.FLAG.javaName(), true);
        values.setProperty(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName(), BigDecimal.ONE);
        values.setProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), kW.getValue());
        when(customPropertySetService.getUniqueValuesFor(customPropertySet, device)).thenReturn(values);
    }

    private CustomMeterReadingsEventHandler getInstance() {
        return new CustomMeterReadingsEventHandler(eventService, meteringService, deviceService,
                customPropertySetService, nlsService);
    }

    @After
    public void tearDown() {
        readings.clear();
        CustomPropertySets.getPowerFactorEventReadingTypes().entrySet().clear();
        CustomPropertySets.getMaxDemandEventReadingTypes().entrySet().clear();
        CustomPropertySets.getCTRatioEventReadingTypes().entrySet().clear();
    }
}
