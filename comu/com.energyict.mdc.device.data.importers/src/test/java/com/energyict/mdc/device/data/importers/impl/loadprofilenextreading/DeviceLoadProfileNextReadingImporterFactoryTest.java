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
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.loadprofilenextreading.DeviceLoadProfileNextReadingImporterFactory;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.SupportedNumberFormatInfo;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
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
public class DeviceLoadProfileNextReadingImporterFactoryTest {

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

  private FileImporter createDeviceLoadProfileNextReadingImporter() {
        DeviceLoadProfileNextReadingImporterFactory factory = new DeviceLoadProfileNextReadingImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), timeZone.getID());
        return factory.createImporter(properties);
    }

    private FileImporterProperty mockFileImporterProperty(String key, Object value) {
        FileImporterProperty property = mock(FileImporterProperty.class);
        when(property.getName()).thenReturn(key);
        when(property.getValue()).thenReturn(value);
        return property;
    }
    private User mockUser(String mRID) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(mRID);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(userService.findUser(mRID)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device name;Load profile;Date Next Reading Block\n" +
                "SPE01000003;1.0.99.2.0.255;25/02/2019 13:00";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.empty());

        FileImporter importer = createDeviceLoadProfileNextReadingImporter();
        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE).format(2, "SPE01000003"));
        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
    }

//    @Test
//    public void testDeviceInDecommissionedState() {
//        String csv = "Device name;Load profile;Date Next Reading Block\n" +
//                "SPE01000003;1.0.99.2.0.255;25/02/2019 13:00";
//        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
//
//        mockDeviceInState("SPE01000003", DefaultState.DECOMMISSIONED);
//        User user = mockUser("batch executor");
//        when(threadPrincipalService.getPrincipal()).thenReturn(user);
//
//        FileImporter importer = createDeviceLoadProfileNextReadingImporter();
//        importer.process(importOccurrence);
//
//        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE).format(2, "SPE01000003"));
//        verifyNoMoreInteractions(logger);
//        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
//    }

//    @Test
//    public void testDeviceInInStockState() {
//        String csv = "Device name;Load profile;Date Next Reading Block\n" +
//                "SPE01000003;1.0.99.2.0.255;25/02/2019 13:00";
//        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
//
//        mockDeviceInState("SPE01000003", DefaultState.IN_STOCK);
//        User user = mockUser("batch executor");
//        when(threadPrincipalService.getPrincipal()).thenReturn(user);
//
//        FileImporter importer = createDeviceLoadProfileNextReadingImporter();
//        importer.process(importOccurrence);
//
//        verify(logger).warning(thesaurus.getFormat(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE).format(2, "SPE01000003"));
//        verifyNoMoreInteractions(logger);
//        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
//    }

    @Test
    public void testInvalidOBISCode() {
        String csv = "Device name;Load profile;Date Next Reading Block\n" +
                "SPE01000003;1.1.1.1.1.1;25/02/2019 13:00";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        Device device = mockDevice("SPE01000003");

        mockLoadProfile(device, "1.0.99.2.0.255", Arrays.asList());

        FileImporter importer = createDeviceLoadProfileNextReadingImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.INVALID_DEVICE_LOADPROFILE_OBIS_CODE).format(2, "1.1.1.1.1.1","SPE01000003"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
    }

    @Test
    public void testSuccessImport() {
        // 3 devices with channels
        ZonedDateTime readingDate1 = ZonedDateTime.of(2019, 3, 7, 13, 25, 0, 0, ZoneOffset.UTC);
        ZonedDateTime readingDate2 = ZonedDateTime.of(2019, 3, 8, 0, 0, 10, 0, ZoneOffset.UTC);
        ZonedDateTime readingDate3 = ZonedDateTime.of(2019, 3, 9, 0, 0, 10, 0, ZoneOffset.UTC);

        String csv = "Device name;Load profile;Date Next Reading Block\n"+
                    "SPE01000003;1.0.99.2.0.255;07/03/2019 13:25\n"+
                    "SPE01000004;1.0.99.2.0.255;\n"+
                    "SPE01000006;1.0.99.2.0.255;\n";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("SPE01000003");

        Channel channel1 = mockChannel( readingDate2.toInstant() );
        Channel channel2 = mockChannel( readingDate3.toInstant() );

        mockLoadProfile(device1,"1.0.99.2.0.255", Arrays.asList(channel1,channel2));

        Device device2 = mockDevice("SPE01000004");
        mockLoadProfile(device2,"1.0.99.2.0.255", Arrays.asList(channel1));

        Device device3 = mockDevice("SPE01000006");
        mockLoadProfile(device3,"1.0.99.2.0.255", Arrays.asList(channel1, channel2));

        FileImporter importer = createDeviceLoadProfileNextReadingImporter();
        importer.process(importOccurrence);

        verifyNoMoreInteractions(logger);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(3));


        LoadProfile.LoadProfileUpdater loadProfileUpdater = device1.getLoadProfileUpdaterFor(device1.getLoadProfiles().get(0));
        ArgumentCaptor<Instant> lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReading(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate1.toInstant());

        loadProfileUpdater = device2.getLoadProfileUpdaterFor(device2.getLoadProfiles().get(0));
        lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReading(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate2.toInstant());

        loadProfileUpdater = device3.getLoadProfileUpdaterFor(device3.getLoadProfiles().get(0));
        lastReadingArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadProfileUpdater).setLastReading(lastReadingArgumentCaptor.capture());
        assertThat(lastReadingArgumentCaptor.getValue()).isEqualTo(readingDate3.toInstant());
    }

    private Channel mockChannel(Instant lastReadTime) {
        Channel channel = mock(Channel.class);
        when(channel.getLastDateTime()).thenReturn(Optional.ofNullable(lastReadTime));

        return channel;
    }

    private LoadProfile mockLoadProfileWithDetails(Device device, String obisCode) {

        if (device.getName() == null && device == null)
        {
            return null;
        }

        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getDeviceObisCode()).thenReturn(ObisCode.fromString(obisCode));
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfiles()).thenReturn(Collections.singletonList(loadProfile));
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(loadProfileUpdater.setLastReadingIfLater(Matchers.any())).thenReturn(loadProfileUpdater);
        return loadProfile;
    }

    private LoadProfile mockLoadProfile(Device device, String loadProfileObisCode, List<Channel> channels){
        LoadProfile loadProfile =  mockLoadProfileWithDetails(device,loadProfileObisCode);
        when(loadProfile.getChannels()).thenReturn(channels);
        return loadProfile;
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

//    private ReadingType mockReadingType(String mRID, boolean isRegular) {
//        return mockReadingTypeWithDetails(mRID, null, isRegular);
//    }
//
//    private ReadingType mockReadingTypeWithDetails(String mRID, String fullAliasName, boolean isRegular) {
//        ReadingType readingType = mock(ReadingType.class);
//        when(readingType.getMRID()).thenReturn(mRID);
//        when(readingType.isRegular()).thenReturn(isRegular);
//        if (fullAliasName != null && !fullAliasName.isEmpty()) {
//            when(readingType.getFullAliasName()).thenReturn(fullAliasName);
//            when(meteringService.getReadingTypeByName(fullAliasName)).thenReturn(Optional.of(readingType));
//        }
//        if (isRegular) {
//            when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
//        }
//        when(meteringService.getReadingType(mRID)).thenReturn(Optional.of(readingType));
//        return readingType;
//    }

//    private MeterActivation mockMeterActivation(Range<Instant> range) {
//        MeterActivation meterActivation = mock(MeterActivation.class);
//        when(meterActivation.getInterval()).thenReturn(Interval.of(range));
//        if (range.hasLowerBound()) {
//            when(meterActivation.getStart()).thenReturn(range.lowerEndpoint());
//        }
//        if (range.hasUpperBound()) {
//            when(meterActivation.getEnd()).thenReturn(range.upperEndpoint());
//        }
//        when(meterActivation.getRange()).thenReturn(range);
//        return meterActivation;
//    }
}
