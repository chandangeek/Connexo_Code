/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.SupportedNumberFormatInfo;
import com.energyict.mdc.device.data.security.Privileges;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT1;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT2;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceReadingsImporterFactoryTest {

    private DeviceDataImporterContext context;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private BatchService batchService;
    @Mock
    private Logger logger;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private UserPreferencesService userPreferencesService;

    private TimeZone timeZone = TimeZone.getTimeZone("GMT");

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceConfigurationService, deviceService, batchService);
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setDeviceConfigurationService(deviceConfigurationService);
        context.setMeteringService(meteringService);
        context.setPropertySpecService(new PropertySpecServiceImpl());
        context.setThreadPrincipalService(threadPrincipalService);
        context.setUserService(userService);
        context.setClock(Clock.system(ZoneId.of("Europe/Athens"))); //CXO-7969
        when(context.getThesaurus()).thenReturn(thesaurus);
        when(userService.getUserPreferencesService()).thenReturn(userPreferencesService);
        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.empty());
        when(meteringService.getReadingTypeByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.empty());
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createDeviceReadingsImporter() {
        DeviceReadingsImporterFactory factory = new DeviceReadingsImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), timeZone.getID());
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testGetProperties() {
        User user = mockUser("admin");
        mockUserPreference(user, PreferenceType.DECIMAL_SEPARATOR, ",");
        mockUserPreference(user, PreferenceType.THOUSANDS_SEPARATOR, ".");

        DeviceReadingsImporterFactory factory = new DeviceReadingsImporterFactory(context);
        List<PropertySpec> propertySpecs = factory.getPropertySpecs();

        assertThat(propertySpecs).hasSize(4);
        //delimiter
        Optional<PropertySpec> delimiter = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(DELIMITER.getPropertyKey())).findFirst();
        assertThat(delimiter).isPresent();
        assertThat(delimiter.get().getPossibleValues().getAllValues()).containsExactly(";", ",");
        assertThat(delimiter.get().getPossibleValues().getDefault()).isEqualTo(";");

        //date format
        Optional<PropertySpec> dateFormat = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(DATE_FORMAT.getPropertyKey())).findFirst();
        assertThat(dateFormat).isPresent();
        assertThat(dateFormat.get().getPossibleValues().getDefault()).isEqualTo("dd/MM/yyyy HH:mm");

        //time zone
        Optional<PropertySpec> timeZone = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(TIME_ZONE.getPropertyKey())).findFirst();
        assertThat(timeZone).isPresent();
        assertThat(timeZone.get().getPossibleValues().getDefault()).isEqualTo("Europe/Athens"); //CXO-7969

        //number format
        Optional<PropertySpec> numberFormat = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(NUMBER_FORMAT.getPropertyKey())).findFirst();
        assertThat(numberFormat).isPresent();
        assertThat(((SupportedNumberFormatInfo) numberFormat.get().getPossibleValues().getDefault()).getFormat()).isEqualTo(FORMAT2);
    }

    @Test
    public void testGetDefaultNumberFormatProperties() {
        User user = mockUser("admin");
        mockUserPreference(user, PreferenceType.DECIMAL_SEPARATOR, ",");
        when(userPreferencesService.getPreferenceByKey(user, PreferenceType.THOUSANDS_SEPARATOR)).thenReturn(Optional.empty());

        DeviceReadingsImporterFactory factory = new DeviceReadingsImporterFactory(context);
        List<PropertySpec> propertySpecs = factory.getPropertySpecs();

        //number format
        Optional<PropertySpec> numberFormat = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(NUMBER_FORMAT.getPropertyKey())).findFirst();
        assertThat(numberFormat).isPresent();
        assertThat(((SupportedNumberFormatInfo) numberFormat.get().getPossibleValues().getDefault()).getFormat()).isEqualTo(FORMAT4);
    }

    private User mockUser(String mRID) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(mRID);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(userService.findUser(mRID)).thenReturn(Optional.of(user));
        return user;
    }

    private void mockUserPreference(User user, PreferenceType preferenceType, String value) {
        UserPreference userPreference = mock(UserPreference.class);
        when(userPreference.getDisplayFormat()).thenReturn(value);
        when(userPreferencesService.getPreferenceByKey(user, preferenceType)).thenReturn(Optional.of(userPreference));
    }

    @Test
    public void testNumberFormatIsIncompatibleWithDelimiter() {
        DeviceReadingsImporterFactory factory = new DeviceReadingsImporterFactory(context);
        List<FileImporterProperty> properties = new ArrayList<>();

        //Format 1 [123,456,789.012]
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ","));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT1)));
        assertExceptionThrown(factory, properties);

        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ";"));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT1)));
        factory.validateProperties(properties);

        //Format 2 [123.456.789,012]
        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ","));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT2)));
        assertExceptionThrown(factory, properties);

        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ";"));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT2)));
        factory.validateProperties(properties);

        //Format 3 [123456789.012]
        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ","));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT3)));
        factory.validateProperties(properties);

        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ";"));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT3)));
        factory.validateProperties(properties);

        //Format 4 [123456789,012]
        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ","));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT4)));
        assertExceptionThrown(factory, properties);

        properties.clear();
        properties.add(mockFileImporterProperty(DELIMITER.getPropertyKey(), ";"));
        properties.add(mockFileImporterProperty(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT4)));
        factory.validateProperties(properties);
    }

    private void assertExceptionThrown(DeviceReadingsImporterFactory factory, List<FileImporterProperty> properties) {
        try {
            factory.validateProperties(properties);
            fail("Exception expected but not thrown");
        } catch (LocalizedFieldValidationException e) {
            assertThat(e.getViolatingProperty()).isEqualTo("properties." + DeviceDataImporterProperty.NUMBER_FORMAT.getPropertyKey());
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER);
        }
    }

    private FileImporterProperty mockFileImporterProperty(String key, Object value) {
        FileImporterProperty property = mock(FileImporterProperty.class);
        when(property.getName()).thenReturn(key);
        when(property.getValue()).thenReturn(value);
        return property;
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1005003";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.empty());

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE).format(2, "VPB0001"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testDeviceInDecommissionedState() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1005003";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        mockDeviceInState("VPB0001", DefaultState.DECOMMISSIONED);
        User user = mockUser("batch executor");
        when(threadPrincipalService.getPrincipal()).thenReturn(user);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE).format(2, "VPB0001"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testDeviceInInStockState() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1005003";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        mockDeviceInState("VPB0001", DefaultState.IN_STOCK);
        User user = mockUser("batch executor");
        when(threadPrincipalService.getPrincipal()).thenReturn(user);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE).format(2, "VPB0001"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingDateIsNotCorrect() {
        ZonedDateTime firstReadingDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationStartDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationEndDate = ZonedDateTime.of(2015, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime lastReadingDate = ZonedDateTime.of(2015, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501\n" +
                "VPB0001;04/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100500";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        MeterActivation meterActivation = mockMeterActivation(Range.closedOpen(meterActivationStartDate.toInstant(), meterActivationEndDate.toInstant()));
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Arrays.asList(meterActivation));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION)
                .format(2, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(firstReadingDate)));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_AFTER_METER_ACTIVATION).format(3, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(lastReadingDate)));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingDateIsEqualsToMeterActivationDate() {
        ZonedDateTime readingDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationStartDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        MeterActivation meterActivation = mockMeterActivation(Range.atLeast(meterActivationStartDate.toInstant()));
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Arrays.asList(meterActivation));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION).format(2, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate)));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingDatesAreOnEdgesOfMeterActivation() {
        ZonedDateTime firstReadingDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationStartDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationEndDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime lastReadingDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100\n" +
                "VPB0001;02/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;101";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        MeterActivation meterActivation = mockMeterActivation(Range.closedOpen(meterActivationStartDate.toInstant(), meterActivationEndDate.toInstant()));
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Arrays.asList(meterActivation));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION)
                .format(2, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(firstReadingDate)));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(1, 1, 1, 1));

        ArgumentCaptor<MeterReading> readingArgumentCaptor = ArgumentCaptor.forClass(MeterReading.class);
        verify(device).store(readingArgumentCaptor.capture());
        List<IntervalBlock> intervalBlocks = readingArgumentCaptor.getValue().getIntervalBlocks();
        assertThat(intervalBlocks).hasSize(1);
        assertThat(intervalBlocks.get(0).getReadingTypeCode()).isEqualTo("11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        List<IntervalReading> intervals = intervalBlocks.get(0).getIntervals();
        assertThat(intervals).hasSize(1);
        assertThat(intervals.get(0).getTimeStamp()).isEqualTo(lastReadingDate.toInstant());
        assertThat(intervals.get(0).getValue()).isEqualTo(BigDecimal.valueOf(101L));
    }

    @Test
    public void testReadingTypeNotFound() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        mockDevice("VPB0001");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.empty());

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SUCH_READING_TYPE).format(2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingTypeIsNotSupported() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", false);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingDateIncorrectDailyReadingType() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value;\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;\n" +
                "VPB0001;02/08/2015 01:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        Channel channel = mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(channel.getReadingType().getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_DAILY_CHANNEL).format(3, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", timeZone.toZoneId()));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(1, 1, 1, 1));
    }

    @Test
    public void testReadingDateIncorrectMonthlyReadingType() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value;\n" +
                "VPB0001;01/08/2015 00:00;13.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;\n" +
                "VPB0001;02/08/2015 01:00;13.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        Channel channel = mockChannel(device, "13.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(channel.getReadingType().getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_MONTHLY_CHANNEL).format(3, "13.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", timeZone.toZoneId()));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(1, 1, 1, 1));
    }

    @Test
    public void testReadingDateIncorrectYearlyReadingType() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value;\n" +
                "VPB0001;01/08/2015 00:00;1001.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;\n" +
                "VPB0001;01/01/2016 00:00;1001.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        Channel channel = mockChannel(device, "1001.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(channel.getReadingType().getMacroPeriod()).thenReturn(MacroPeriod.YEARLY);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_YEARLY_CHANNEL).format(2, "1001.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", timeZone.toZoneId()));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(1, 1, 1, 1));
    }

    @Test
    public void testDeviceDoesNotSupportReadingType() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        mockDevice("VPB0001");
        mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", false);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.DEVICE_DOES_NOT_SUPPORT_READING_TYPE).format(2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testTextualReadingTypesNotSupported() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100502\n" +
                "VPB0001;02/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType1 = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", false);
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1")).thenReturn(Optional.of(readingType1));
        ReadingType readingType2 = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2", false);
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2")).thenReturn(Optional.of(readingType2));
        Register register1 = mock(Register.class, RETURNS_DEEP_STUBS);
        Register register2 = mock(Register.class, RETURNS_DEEP_STUBS);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));
        when(register1.getReadingType()).thenReturn(readingType1);
        when(register1.getRegisterSpec().isTextual()).thenReturn(true);
        when(register2.getReadingType()).thenReturn(readingType2);
        when(register2.getRegisterSpec().isTextual()).thenReturn(true);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NOT_SUPPORTED_READING_TYPE).format(2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NOT_SUPPORTED_READING_TYPE).format(3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format());
    }

    @Test
    public void testReadingValueDoesNotMatchRegisterConfiguration() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1000\n" +
                "VPB0001;03/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;998.984";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        mockRegister(device, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG).format(3, "998.98"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW).format(2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS).format(1, 1, 1, 1, 1));
    }

    @Test
    public void testReadingValueDoesNotMatchChannelConfiguration() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1000\n" +
                "VPB0001;03/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;998.984";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(3, "998.98"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW).format(2, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS).format(1, 1, 1, 1, 1));
    }

    @Test
    public void testSuccessImport() {
        ZonedDateTime readingDate1 = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime readingDate2 = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime readingDate3 = ZonedDateTime.of(2015, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device name;Reading date;Reading type MRID;Reading Value;;\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;107\n" +
                "VPB0002;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;107\n" +
                "VPB0003;03/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;107";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        mockRegister(device1, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        mockChannel(device1, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        Device device2 = mockDevice("VPB0002");
        mockRegister(device2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");
        mockChannel(device2, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");
        Device device3 = mockDevice("VPB0003");
        mockRegister(device3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2");
        mockChannel(device3, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS).format(6, 3));

        ArgumentCaptor<MeterReading> readingArgumentCaptor = ArgumentCaptor.forClass(MeterReading.class);
        verify(device1).store(readingArgumentCaptor.capture());

        List<Reading> readings = readingArgumentCaptor.getValue().getReadings();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getReadingTypeCode()).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(readingDate1.toInstant());
        assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(100L));

        List<IntervalBlock> intervalBlocks = readingArgumentCaptor.getValue().getIntervalBlocks();
        assertThat(intervalBlocks).hasSize(1);
        assertThat(intervalBlocks.get(0).getReadingTypeCode()).isEqualTo("11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        List<IntervalReading> intervals = intervalBlocks.get(0).getIntervals();
        assertThat(intervals.get(0).getTimeStamp()).isEqualTo(readingDate1.toInstant());
        assertThat(intervals.get(0).getValue()).isEqualTo(BigDecimal.valueOf(107L));

        LoadProfile.LoadProfileUpdater loadProfileUpdater = device1.getLoadProfileUpdaterFor(device1.getLoadProfiles().get(0));
        ArgumentCaptor<Instant> lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReadingIfLater(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate1.toInstant());

        loadProfileUpdater = device2.getLoadProfileUpdaterFor(device2.getLoadProfiles().get(0));
        lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReadingIfLater(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate2.toInstant());

        loadProfileUpdater = device3.getLoadProfileUpdaterFor(device3.getLoadProfiles().get(0));
        lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReadingIfLater(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate3.toInstant());
    }

    @Test
    public void testSuccessImportForDecommissionedDevice() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value;\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.DECOMMISSIONED);
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        User user = mockUser("batch executor");
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(user.hasPrivilege("MDC", Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)).thenReturn(true);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS).format(1, 1));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportReadingsLinesDifferentLength() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value;;\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100\n" +
                "VPB0001;02/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;;;;\n" +
                "VPB0001;03/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;;1001;;;;\n";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.LINE_MISSING_VALUE_ERROR).format(4, "#5"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS).format(3, 1));
    }

    @Test
    public void testImportResultFailedWithErrors() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100\n" +
                "VPB0002;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+;100\n" +
                "VPB0003;01/08/20153 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100\n" +
                "VPB0003;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100";
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+")).thenReturn(Optional.empty());
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        Device device2 = mockDevice("VPB0002");
        Device device3 = mockDevice("VPB0003");
        mockRegisterWithDetails(device1, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Alias name 1", "");
        mockRegisterWithDetails(device3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "Alias name 3", "");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SUCH_READING_TYPE).format(3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+"));
        verify(logger).severe(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(4, "Reading date", "dd/MM/yyyy HH:mm"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_ERRORS).format(1, 1, 1, 1));
        verify(device1).store(Matchers.any());
        verify(device2, never()).store(Matchers.any());
        verify(device3, never()).store(Matchers.any());
    }

    @Test
    public void testImportResultFailedWithErrorsAndWarnings() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100.156\n" +
                "VPB0002;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+;100\n" +
                "VPB0003;01/08/20153 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100\n" +
                "VPB0003;02/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100";
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+")).thenReturn(Optional.empty());
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        Device device2 = mockDevice("VPB0002");
        Device device3 = mockDevice("VPB0003");
        mockChannel(device1, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        mockChannel(device3, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(2, "100.15"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SUCH_READING_TYPE).format(3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+"));
        verify(logger).severe(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(4, "Reading date", "dd/MM/yyyy HH:mm"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS).format(1, 1, 1, 1, 1));
        verify(device1).store(Matchers.any());
        verify(device2, never()).store(Matchers.any());
        verify(device3, never()).store(Matchers.any());
    }

    @Test
    public void testImportResultFailedWithWarnings() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100.156\n" +
                "VPB0002;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100\n" +
                "VPB0003;01/08/20153 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100\n" +
                "VPB0003;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100";
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.+")).thenReturn(Optional.empty());
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        Device device2 = mockDevice("VPB0002");
        Device device3 = mockDevice("VPB0003");
        mockRegister(device1, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        mockRegister(device2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG).format(2, "100.15"));
        verify(logger).severe(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(4, "Reading date", "dd/MM/yyyy HH:mm"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_WARN).format(2, 2, 1));
        verify(device1).store(Matchers.any());
        verify(device2, never()).store(Matchers.any());
        verify(device3, never()).store(Matchers.any());
    }

    @Test
    public void testImportResultFailedNoReadingsProcessed() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/20153 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100\n" +
                "VPB0002;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.2;100";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        Device device2 = mockDevice("VPB0002");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).severe(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(2, "Reading date", "dd/MM/yyyy HH:mm"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format(2, 2, 1));
        verify(device1, never()).store(Matchers.any());
        verify(device2, never()).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessNoReadingsProcessed() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED).format(2, 2, 1));
    }

    @Test
    public void testImportResultSuccessWithWarning() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.134\n" +
                "VPB0001;02/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        mockChannel(device1, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN).format(2, 1, 2));
        verify(device1).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForDeviceByMrid() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;01/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.134\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;02/08/2015 00:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", "6a2632a4-6b73-4a13-bbcc-09c8bdd02308", DefaultState.ACTIVE);
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG).format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN).format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForChannelByMrid() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.134\n" +
                "VPB0001;02/08/2015 02:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockChannel(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForRegisterByMrid() {
        String csv = "Device name;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.134\n" +
                "VPB0001;02/08/2015 02:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockRegister(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForRegisterByAliasName() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;Alias Name;100.134\n" +
                "VPB0001;02/08/2015 02:00;Alias Name;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockRegisterWithDetails(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "Alias Name", "");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForChannelByAliasName() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;Alias Name;100.134\n" +
                "VPB0001;02/08/2015 02:00;Alias Name;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockChannelWithDetails(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "Alias Name", "");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForChannelByObisCode() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;1.0.1.8.0.251;100.134\n" +
                "VPB0001;02/08/2015 02:00;1.0.1.8.0.251;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockChannelWithDetails(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "", "1.0.1.8.0.251");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForRegisterByObisCode() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;1.0.1.8.0.251;100.134\n" +
                "VPB0001;02/08/2015 02:00;1.0.1.8.0.251;100.24546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDeviceInState("VPB0001", DefaultState.ACTIVE);
        mockRegisterWithDetails(device, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "", "1.0.1.8.0.251");

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(2, "100.13"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(3, "100.24"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(2, 1, 2));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultSuccessForChannelWithTimeperiod() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:00;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.146\n" +
                "VPB0001;01/08/2015 01:15;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.234\n" +
                "VPB0001;01/08/2015 01:30;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.334\n" +
                "VPB0001;01/08/2015 01:45;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.44546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", false);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);

        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.of(BigDecimal.valueOf(999L)));
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(2);

        Register register = mock(Register.class);
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getReadingType()).thenReturn(readingType);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getRegisterSpec().isTextual()).thenReturn(false);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(2, "100.14"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(3, "100.23"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(4, "100.33"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(5, "100.44"));
        verify(logger).info(thesaurus.getFormat(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG)
                .format(5, "100.44"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(4, 1, 4));
        verify(device).store(Matchers.any());
    }

    @Test
    public void testImportResultFailForChannelWithTimeperiod() {
        String csv = "Device name;Reading date;Reading type;Reading Value\n" +
                "VPB0001;01/08/2015 01:01;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.146\n" +
                "VPB0001;01/08/2015 01:18;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.234\n" +
                "VPB0001;01/08/2015 01:31;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.334\n" +
                "VPB0001;01/08/2015 01:40;11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1;100.44546";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", false);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);

        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.of(BigDecimal.valueOf(999L)));
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(2);

        Register register = mock(Register.class);
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getReadingType()).thenReturn(readingType);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getRegisterSpec().isTextual()).thenReturn(false);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL)
                .format(2, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "15", "GMT"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL)
                .format(3, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "15", "GMT"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL)
                .format(4, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "15", "GMT"));
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL)
                .format(5, "11.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1", "15", "GMT"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED)
                .format());
    }

    private Register mockRegister(Device device, String readingTypeMRID) {
        return mockRegisterWithDetails(device, readingTypeMRID, null, null);
    }

    private Register mockRegisterWithDetails(Device device, String readingTypeMRID, String fullAliasName, String obisCode) {
        ReadingType readingType;
        if (fullAliasName != null && !fullAliasName.isEmpty()) {
            readingType = mockReadingTypeWithDetails(readingTypeMRID, fullAliasName, false);
        } else {
            readingType = mockReadingType(readingTypeMRID, false);
        }
        Register register = mock(Register.class);
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        if (obisCode != null && !obisCode.isEmpty()) {
            when(device.getRegisters().get(0).getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        }
        when(register.getReadingType()).thenReturn(readingType);
        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.of(BigDecimal.valueOf(999L)));
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(2);
        return register;
    }

    private Channel mockChannelWithDetails(Device device, String readingTypeMRID, String fullAliasName, String obisCode) {
        ReadingType readingType;
        if (fullAliasName != null && !fullAliasName.isEmpty()) {
            readingType = mockReadingTypeWithDetails(readingTypeMRID, fullAliasName, true);
        } else {
            readingType = mockReadingType(readingTypeMRID, true);
        }
        LoadProfile loadProfile = mock(LoadProfile.class);
        Channel channel = mock(Channel.class);
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        if (obisCode != null && !obisCode.isEmpty()) {
            when(device.getChannels().get(0).getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        }
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getOverflow()).thenReturn(Optional.of(BigDecimal.valueOf(999L)));
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfiles()).thenReturn(Collections.singletonList(loadProfile));
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(loadProfileUpdater.setLastReadingIfLater(Matchers.any())).thenReturn(loadProfileUpdater);
        return channel;
    }

    private Channel mockChannel(Device device, String readingTypeMRID) {
        return mockChannelWithDetails(device, readingTypeMRID, null, null);
    }

    private Device mockDevice(String deviceName) {
        return mockDeviceInState(deviceName, DefaultState.ACTIVE);
    }

    private Device mockDeviceInState(String deviceName, DefaultState state) {
        return mockDeviceInState(deviceName, UUID.randomUUID().toString(), state);
    }

    private Device mockDeviceInState(String deviceName, String mRID, DefaultState state) {
        Device device = mock(Device.class);
        when(device.getName()).thenReturn(deviceName);
        when(device.getmRID()).thenReturn(mRID);
        State deviceState = mock(State.class);
        when(deviceState.getName()).thenReturn(state.getKey());
        when(device.getState()).thenReturn(deviceState);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByMrid(mRID)).thenReturn(Optional.of(device));
        return device;
    }

    private ReadingType mockReadingType(String mRID, boolean isRegular) {
        return mockReadingTypeWithDetails(mRID, null, isRegular);
    }

    private ReadingType mockReadingTypeWithDetails(String mRID, String fullAliasName, boolean isRegular) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        when(readingType.isRegular()).thenReturn(isRegular);
        if (fullAliasName != null && !fullAliasName.isEmpty()) {
            when(readingType.getFullAliasName()).thenReturn(fullAliasName);
            when(meteringService.getReadingTypeByName(fullAliasName)).thenReturn(Optional.of(readingType));
        }
        if (isRegular) {
            when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        }
        when(meteringService.getReadingType(mRID)).thenReturn(Optional.of(readingType));
        return readingType;
    }

    private MeterActivation mockMeterActivation(Range<Instant> range) {
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getInterval()).thenReturn(Interval.of(range));
        if (range.hasLowerBound()) {
            when(meterActivation.getStart()).thenReturn(range.lowerEndpoint());
        }
        if (range.hasUpperBound()) {
            when(meterActivation.getEnd()).thenReturn(range.upperEndpoint());
        }
        when(meterActivation.getRange()).thenReturn(range);
        return meterActivation;
    }
}
