package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
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

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.FILE_FORMAT_ERROR.getTranslated(thesaurus, 2, 2, 0));
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device MRID;Security settings name\n" +
                "VPB0001;MD5";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_DEVICE.getTranslated(thesaurus, 2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
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

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE.getTranslated(thesaurus, 2, "MD5"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
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

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE.getTranslated(thesaurus, 2, "set1"));
        verify(logger).severe(MessageSeeds.SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE.getTranslated(thesaurus, 3));
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
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
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
        List<SecurityProperty> properties = Arrays.asList(mockSecurityProperty("attr1", null));
        when(device.getAllSecurityProperties(securityPropertySet)).thenReturn(properties);

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger).info(MessageSeeds.REQUIRED_SECURITY_ATTRIBUTES_MISSED.getTranslated(thesaurus, 2, "attr1, attr2"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));
    }

    @Test
    public void testSetAttributesSuccessfully() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "VPB0001;MD5;string;100.25;1";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
        List<SecurityProperty> properties = Arrays.asList(
                mockSecurityProperty("attr1", "value"),
                mockSecurityProperty("attr2", BigDecimal.valueOf(100.25)),
                mockSecurityProperty("attr3", true));
        when(device.getAllSecurityProperties(securityPropertySet)).thenReturn(properties);
        doAnswer(invocationOnMock -> {
            assertThat((SecurityPropertySet) invocationOnMock.getArguments()[0]).isEqualTo(securityPropertySet);
            TypedProperties typedProperties = (TypedProperties) invocationOnMock.getArguments()[1];
            assertThat(typedProperties.getProperty("attr1")).isEqualTo("string");
            assertThat(typedProperties.getProperty("attr2")).isEqualTo(BigDecimal.valueOf(100.25));
            assertThat(typedProperties.getProperty("attr3")).isEqualTo(true);
            return null;
        }).when(device).setSecurityProperties(any(), any());

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));

        verify(device).setSecurityProperties(Matchers.any(), Matchers.any());
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
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.LINE_FORMAT_ERROR.getTranslated(thesaurus, 2, "attr2", "123456789.012"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
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
