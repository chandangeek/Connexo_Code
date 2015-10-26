package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityAttributesImporterFactoryTest {

    private DeviceDataImporterContext context;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Logger logger;

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceService);
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createSecurityAttributesImporter() {
        SecurityAttributesImportFactory factory = new SecurityAttributesImportFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatInfo(FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testMandatoryColumnsMissed() {
        String csv = "Device MRID;Security settings name\n" +
                ";";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createSecurityAttributesImporter();

        importer.process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.FILE_FORMAT_ERROR).format(2, 2, 0));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device MRID;Security settings name\n" +
                "VPB0001;MD5";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE).format(2, "VPB0001"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
    }

    @Test
    public void testNoSuchSecuritySettings() {
        String csv = "Device MRID;Security settings name\n" +
                "VPB0001;MD5";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mockDevice("VPB0001");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE).format(2, "MD5"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
    }

    @Test
    public void testSecuritySettingsNameUniquenessThroughFile() {
        String csv = "Device MRID;Security settings name\n" +
                "VPB0001;set1\n" +
                "VPB0002;set2\n" +
                "VPB0003;set3\n";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mockDevice("VPB0001");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        mockDevice("VPB0002");
        mockDevice("VPB0003");

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE).format(2, "set1"));
        verify(logger).severe(thesaurus.getFormat(MessageSeeds.SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE).format(3));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS).format(0, 1));
    }

    @Test
    public void testSetAttributesButMandatoryOnesMissed() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "VPB0001;MD5;;;";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        List<SecurityProperty> properties = Collections.singletonList(mockSecurityProperty("attr1", null));
        when(device.getSecurityProperties(securityPropertySet)).thenReturn(properties);

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger).info(thesaurus.getFormat(MessageSeeds.REQUIRED_SECURITY_ATTRIBUTES_MISSED).format(2, "attr1, attr2"));
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN).format(1, 1));
    }

    @Test
    public void testSetAttributesSuccessfully() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "VPB0001;MD5;string;100.25;true";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        List<SecurityProperty> properties = Arrays.asList(
                mockSecurityProperty("attr1", "value"),
                mockSecurityProperty("attr2", BigDecimal.valueOf(100.25)),
                mockSecurityProperty("attr3", true));
        when(device.getSecurityProperties(securityPropertySet)).thenReturn(properties);
        doAnswer(invocationOnMock -> {
            assertThat((SecurityPropertySet) invocationOnMock.getArguments()[0]).isEqualTo(securityPropertySet);
            TypedProperties typedProperties = (TypedProperties) invocationOnMock.getArguments()[1];
            assertThat(typedProperties.getProperty("attr1")).isEqualTo("string");
            assertThat(typedProperties.getProperty("attr2")).isEqualTo(BigDecimal.valueOf(100.25));
            assertThat(typedProperties.getProperty("attr3")).isEqualTo(true);
            return null;
        }).when(device).setSecurityProperties(any(), any());

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));

        verify(device).setSecurityProperties(any(), any());
    }

    @Test
    public void testUnableToParseAttributes() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "VPB0001;MD5;string;string;string";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(2, "attr2", "123456789.012"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
    }

    @Test
    public void testSecurityAttributeInvalidValue() throws Exception {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "VPB0001;MD5;string;string;string";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false)));
        InvalidValueException exception = mock(InvalidValueException.class);
        doThrow(exception).when(propertySpecs.iterator().next()).validateValue(any());
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.SECURITY_ATTRIBUTE_INVALID_VALUE).format(2, "string", "attr1"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
    }

    private Device mockDevice(String mRID) {
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn(mRID);
        when(deviceService.findByUniqueMrid(mRID)).thenReturn(Optional.of(device));
        return device;
    }

    private SecurityPropertySet mockSecurityPropertySet(String name, Set<PropertySpec> propertySpecs) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn(name);
        when(securityPropertySet.getPropertySpecs()).thenReturn(propertySpecs);
        return securityPropertySet;
    }

    private SecurityProperty mockSecurityProperty(String name, Object value) {
        SecurityProperty securityProperty = mock(SecurityProperty.class);
        when(securityProperty.getName()).thenReturn(name);
        when(securityProperty.getValue()).thenReturn(value);
        when(securityProperty.isComplete()).thenReturn(value != null);
        return securityProperty;
    }

    private PropertySpec mockPropertySpec(String key, ValueFactory valueFactory, boolean required) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(key);
        when(propertySpec.isRequired()).thenReturn(required);
        doReturn(valueFactory).when(propertySpec).getValueFactory();
        return propertySpec;
    }

}