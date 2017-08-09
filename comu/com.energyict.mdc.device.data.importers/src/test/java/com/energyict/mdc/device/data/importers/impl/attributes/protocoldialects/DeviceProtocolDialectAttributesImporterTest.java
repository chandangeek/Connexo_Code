/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.any;
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
public class DeviceProtocolDialectAttributesImporterTest {

    public final static String PROTOCOL_DIALECT_NAME = "SerialDlmsDialect";

    private DeviceDataImporterContext context;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Logger logger;

    @Before
    public void beforeTest() {
        reset(logger, deviceService);
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        when(context.getThesaurus()).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.empty());
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createProtocolDialectAttributesImporter() {
        ProtocolDialectAttributesImportFactory factory = new ProtocolDialectAttributesImportFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DeviceDataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DeviceDataImporterProperty.NUMBER_FORMAT.getPropertyKey(),
                new SupportedNumberFormat.SupportedNumberFormatInfo(SupportedNumberFormat.FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testMandatoryColumnsMissed() {
        String csv = "name;protocolDialect\n" +
                ";";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createProtocolDialectAttributesImporter();

        importer.process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "name;protocolDialect\n" +
                "VPB0001;SerialDlmsDialect";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.empty());

        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(contains("Can't process line 2: No device found: VPB0001."));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    public void testNoSuchProtocolDialectEvenOnConfiguration() {
        String csv = "name;protocolDialect;attr1\n" +
                "VPB0001;SerialDlmsDialect;attr1";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        when(device.getProtocolDialects()).thenReturn(Collections.emptyList());

        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(contains("Can't process line 2: Protocol dialect 'SerialDlmsDialect' is not supported on the device."));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }


    @Test
    public void testSetAttributesSuccessfully() {
        String csv = "name;protocolDialect;attr1;attr2;attr3\n" +
                "VPB0001;SerialDlmsDialect;string;100.25;false";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_NAME, propertySpecs);
        when(device.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));
        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess("Finished successfully. 1 device(s) processed successfully.");

        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr1", "string");
        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr2", new BigDecimal("100.25"));
        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr3", false);
        verify(device).save();
    }

    @Test
    public void testSetAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Protocol dialect;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;SerialDlmsDialect;string;100.25;false";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_NAME, propertySpecs);
        when(device.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));
        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess("Finished successfully. 1 device(s) processed successfully.");

        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr1", "string");
        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr2", new BigDecimal("100.25"));
        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr3", false);
        verify(device).save();
    }

    @Test
    public void testUnableToParseAttributes() {
        String csv = "Device MRID;Protocol dialect;attr1;attr2;attr3\n" +
                "VPB0001;SerialDlmsDialect;string;string;string";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_NAME, propertySpecs);
        when(device.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));
        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(contains("Format error for line 2: wrong value format for column 'attr2' (expected format = '123456789.012')"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());

        verify(device).setProtocolDialectProperty(PROTOCOL_DIALECT_NAME, "attr1", "string");
        verify(device, never()).save();
    }

    @Test
    public void testProtocolDialectAttributeInvalidValue() throws Exception {
        String csv = "Device name;Protocol dialect name;attr1;attr2;attr3\n" +
                "VPB0001;SerialDlmsDialect;string;string;string";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        InvalidValueException exception = mock(InvalidValueException.class);
        doThrow(exception).when(propertySpecs.get(0)).validateValue(any());
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_NAME, propertySpecs);
        when(device.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));

        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(contains("Can't process line 2: Protocol dialect value 'string' is invalid for attribute 'attr1'"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());

        verify(device, never()).save();
    }

    @Test
    public void testUnknownProtocolDialectAttribute() {
        String csv = "Device name;Protocol dialect name;attr1;attr2;typo\n" +
                "VPB0001;SerialDlmsDialect;string;100.25;1\n" +
                "VPB0002;SerialDlmsDialect;string;100.25;1\n";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device1 = mockDevice("VPB0001");
        Device device2 = mockDevice("VPB0002");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_NAME, propertySpecs);
        when(device1.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));
        when(device2.getProtocolDialects()).thenReturn(Collections.singletonList(protocolDialectConfigurationProperties));
        createProtocolDialectAttributesImporter().process(importOccurrence);

        verify(logger, times(2)).info(contains("Note for file: Protocol dialect 'SerialDlmsDialect' doesn't have following attribute(s): typo"));
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess("Finished successfully with (some) note. 2 devices processed successfully of which 2 devices contain a note.");
    }
    
    private Device mockDevice(String deviceName) {
        Device device = mock(Device.class);
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        return device;
    }

    private ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties(String name, List<PropertySpec> propertySpecs) {
        ProtocolDialectConfigurationProperties protocolDialectConfiguration = mock(ProtocolDialectConfigurationProperties.class);
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(protocolDialectConfiguration.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);
        when(protocolDialectConfiguration.getDeviceProtocolDialectName()).thenReturn(name);
        when(deviceProtocolDialect.getDeviceProtocolDialectName()).thenReturn(name);
        when(protocolDialectConfiguration.getPropertySpec(anyString())).thenReturn(Optional.empty());
        for (PropertySpec propertySpec : propertySpecs) {
            String key = propertySpec.getName();
            when(protocolDialectConfiguration.getPropertySpec(key)).thenReturn(Optional.of(propertySpec));
        }
        when(protocolDialectConfiguration.getPropertySpecs()).thenReturn(propertySpecs);
        return protocolDialectConfiguration;
    }

    private PropertySpec mockPropertySpec(String key, ValueFactory valueFactory, boolean required) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(key);
        when(propertySpec.isRequired()).thenReturn(required);
        doReturn(valueFactory).when(propertySpec).getValueFactory();
        return propertySpec;
    }
}
