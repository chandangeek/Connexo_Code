package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.*;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    private DeviceImportService deviceImportService;
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

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceConfigurationService, deviceService, deviceImportService);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            for (TranslationKey translation : TranslationKeys.values()) {
                if (translation.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return translation.getDefaultFormat();
                }
            }
            return invocationOnMock.getArguments()[1];
        });
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setDeviceConfigurationService(deviceConfigurationService);
        context.setDeviceImportService(deviceImportService);
        context.setMeteringService(meteringService);
        context.setPropertySpecService(new PropertySpecServiceImpl());
        context.setThreadPrincipalService(threadPrincipalService);
        context.setUserService(userService);
        when(context.getThesaurus()).thenReturn(thesaurus);
        when(userService.getUserPreferencesService()).thenReturn(userPreferencesService);
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
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormatInfo(FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testGetProperties() {
        User user = mockUser("admin");
        mockUserPreference(user, FormatKey.DECIMAL_SEPARATOR, ",");
        mockUserPreference(user, FormatKey.THOUSANDS_SEPARATOR, ".");

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
        assertThat(timeZone.get().getPossibleValues().getDefault()).isEqualTo(TimeZonePropertySpec.DEFAULT);

        //number format
        Optional<PropertySpec> numberFormat = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(NUMBER_FORMAT.getPropertyKey())).findFirst();
        assertThat(numberFormat).isPresent();
        assertThat(((SupportedNumberFormatInfo) numberFormat.get().getPossibleValues().getDefault()).getFormat()).isEqualTo(FORMAT2);
    }

    @Test
    public void testGetDefaultNumberFormatProperties() {
        User user = mockUser("admin");
        mockUserPreference(user, FormatKey.DECIMAL_SEPARATOR, ",");
        mockUserPreference(user, FormatKey.THOUSANDS_SEPARATOR, "");

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

    private void mockUserPreference(User user, FormatKey formatKey, String value) {
        UserPreference userPreference = mock(UserPreference.class);
        when(userPreference.getFormatFE()).thenReturn(value);
        when(userPreferencesService.getPreferenceByKey(user, formatKey)).thenReturn(Optional.of(userPreference));
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
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_DEVICE.getTranslated(thesaurus, 2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testReadingDateIsNotCorrect() {
        ZonedDateTime firstReadingDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationStartDate = ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime meterActivationEndDate = ZonedDateTime.of(2015, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime lastReadingDate = ZonedDateTime.of(2015, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501\n" +
                "VPB0001;04/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100500";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Arrays.asList(meterActivation));
        when(meterActivation.getRange()).thenReturn(Range.closed(meterActivationStartDate.toInstant(), meterActivationEndDate.toInstant()));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION.getTranslated(thesaurus, 2, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(firstReadingDate)));
        verify(logger).warning(MessageSeeds.READING_DATE_AFTER_METER_ACTIVATION.getTranslated(thesaurus, 3, DefaultDateTimeFormatters.shortDate().withShortTime().build().format(lastReadingDate)));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 2));
    }

    @Test
    public void testReadingTypeNotFound() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        mockDevice("VPB0001");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.empty());

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_SUCH_READING_TYPE.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testReadingTypeIsNotSupported() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NOT_SUPPORTED_READING_TYPE.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testDeviceDoesNotSupportReadingType() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.DEVICE_DOES_NOT_SUPPORT_READING_TYPE.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testTextualReadingTypesNotSupported() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502\n" +
                "VPB0001;02/08/2015 00:30;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100501;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100502";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));
        Register register = mock(Register.class, RETURNS_DEEP_STUBS);
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getReadingType()).thenReturn(readingType);
        when(register.getRegisterSpec().isTextual()).thenReturn(true);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NOT_SUPPORTED_READING_TYPE.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0"));
        verify(logger).warning(MessageSeeds.NOT_SUPPORTED_READING_TYPE.getTranslated(thesaurus, 3, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 2));
    }

    @Test
    public void testReadingValueDoesNotMatchRegisterConfiguration() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1000\n" +
                "VPB0001;03/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;998.984";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));
        Register register = mock(Register.class);
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getReadingType()).thenReturn(readingType);
        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(registerSpec.getOverflowValue()).thenReturn(BigDecimal.valueOf(999L));
        when(registerSpec.getNumberOfDigits()).thenReturn(3);
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(2);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG.getTranslated(thesaurus, 3, "998.98"));
        verify(logger).warning(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS.getTranslated(thesaurus, 1, 1, 1));
    }

    @Test
    public void testReadingValueDoesNotMatchChannelConfiguration() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;1000\n" +
                "VPB0001;03/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;998.984";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));
        LoadProfile loadProfile = mock(LoadProfile.class);
        Channel channel = mock(Channel.class);
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getOverflow()).thenReturn(BigDecimal.valueOf(999L));
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(loadProfileUpdater.setLastReadingIfLater(any())).thenReturn(loadProfileUpdater);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger).info(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG.getTranslated(thesaurus, 3, "998.98"));
        verify(logger).warning(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW.getTranslated(thesaurus, 2, "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS.getTranslated(thesaurus, 1, 1, 1));
    }

    @Test
    public void testSuccessImport() {
        ZonedDateTime readingDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value;;\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));
        LoadProfile loadProfile = mock(LoadProfile.class);
        Channel channel = mock(Channel.class);
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getOverflow()).thenReturn(BigDecimal.valueOf(999L));
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(loadProfileUpdater.setLastReadingIfLater(readingDate.toInstant())).thenReturn(loadProfileUpdater);

        doAnswer(invocationOnMock -> {
            MeterReading reading = (MeterReading) invocationOnMock.getArguments()[0];
            assertThat(reading.getReadings()).hasSize(1);
            assertThat(reading.getIntervalBlocks()).isEmpty();
            assertThat(reading.getReadings().get(0).getTimeStamp()).isEqualTo(readingDate.toInstant());
            assertThat(reading.getReadings().get(0).getReadingTypeCode()).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
            assertThat(reading.getReadings().get(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            return null;
        }).when(device).store(Matchers.any());

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
        verify(device, times(2)).store(Matchers.any());
    }

    @Test
    public void testImportReadingsLinesDifferentLength() {
        ZonedDateTime readingDate = ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value;;\n" +
                "VPB0001;01/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100\n" +
                "VPB0001;02/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;;;;\n" +
                "VPB0001;03/08/2015 00:00;0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0;100;;1001;;;;\n";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(meteringService.getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(readingType));
        LoadProfile loadProfile = mock(LoadProfile.class);
        Channel channel = mock(Channel.class);
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getOverflow()).thenReturn(BigDecimal.valueOf(999L));
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfileUpdaterFor(loadProfile)).thenReturn(loadProfileUpdater);
        when(loadProfileUpdater.setLastReadingIfLater(Matchers.any())).thenReturn(loadProfileUpdater);

        FileImporter importer = createDeviceReadingsImporter();
        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 4, "#5"));
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL.getTranslated(thesaurus, 2));
        verify(device, times(3)).store(Matchers.any());
    }

    private Device mockDevice(String mRID) {
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn(mRID);
        when(deviceService.findByUniqueMrid(mRID)).thenReturn(Optional.of(device));
        return device;
    }

    private ReadingType mockReadingType(String mRID) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        return readingType;
    }
}
