/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.ReferenceValueFactory;
import com.elster.jupiter.util.beans.BeanService;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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
public class SecurityAttributesImporterFactoryTest {
    private DeviceDataImporterContext context;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Logger logger;
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private BeanService beanService;
    @Mock
    private OrmService ormService;

    private KeyType symmetricKeyType;
    private KeyType passphraseKeyType;

    @Before
    public void beforeTest() {
        reset(logger, deviceService);
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        when(context.getThesaurus()).thenReturn(thesaurus);
        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.empty());

        symmetricKeyType = mock(KeyType.class);
        when(symmetricKeyType.getName()).thenReturn("key");
        when(symmetricKeyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);

        passphraseKeyType = mock(KeyType.class);
        when(passphraseKeyType.getName()).thenReturn("password");
        when(passphraseKeyType.getCryptographicType()).thenReturn(CryptographicType.Passphrase);

    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createSecurityAttributesImporter() {
        SecurityAttributesImportFactory factory = new SecurityAttributesImportFactory(context, securityManagementService);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatInfo(FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testMandatoryColumnsMissed() {
        String csv = "Device name;Security settings name\n" +
                ";";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createSecurityAttributesImporter();

        importer.process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device name;Security settings name\n" +
                "VPB0001;MD5";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.empty());

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE).format(2, "VPB0001"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    public void testNoSuchSecuritySettings() {
        String csv = "Device name;Security settings name\n" +
                "VPB0001;MD5";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mockDevice("VPB0001");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE).format(2, "MD5"));
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    public void testSecuritySettingsNameUniquenessThroughFile() {
        String csv = "Device name;Security settings name\n" +
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
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS)
                .format(0, 2));
    }

    @Test
    public void testSetSymmetricKeyAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr1 = mock(SecurityAccessorType.class);
        when(attr1.getName()).thenReturn("kat1");
        when(attr1.getKeyType()).thenReturn(symmetricKeyType);
        SecurityAccessor securityAccessorAttr1 = mock(SecurityAccessor.class);
        PlaintextSymmetricKey symmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityAccessorAttr1.getActualValue()).thenReturn(Optional.of(symmetricKeyWrapper));
        when(securityAccessorAttr1.getTempValue()).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(attr1)).thenReturn(Optional.of(securityAccessorAttr1));

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr1));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr1", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        ConfigurationSecurityProperty securityPropertyAttr1 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr1.getName()).thenReturn("attr1");
        when(securityPropertyAttr1.getSecurityAccessorType()).thenReturn(attr1);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(symmetricKeyWrapper).setProperties(mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getValue().get("key")).isEqualTo("MDEyMzQ1Njc4OTAxMjM0NQ==");
    }

    @Test
    public void testSetPassphraseAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr2 = mock(SecurityAccessorType.class);
        when(attr2.getName()).thenReturn("kat2");
        when(attr2.getKeyType()).thenReturn(passphraseKeyType);
        SecurityAccessor securityAccessorAttr2 = mock(SecurityAccessor.class);
        PlaintextPassphrase passphraseWrapper = mock(PlaintextPassphrase.class);
        when(securityAccessorAttr2.getActualValue()).thenReturn(Optional.of(passphraseWrapper));
        when(securityAccessorAttr2.getTempValue()).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(attr2)).thenReturn(Optional.ofNullable(securityAccessorAttr2));

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr2));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr2", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));

        ConfigurationSecurityProperty securityPropertyAttr2 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr2.getName()).thenReturn("attr2");
        when(securityPropertyAttr2.getSecurityAccessorType()).thenReturn(attr2);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr2));
        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(passphraseWrapper).setProperties(mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getValue().get("passphrase")).isEqualTo("SomePassword");
    }

    @Test
    public void testSetNewKeyAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr3 = mock(SecurityAccessorType.class);
        PlaintextSymmetricKey newSymmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityManagementService.newSymmetricKeyWrapper(attr3)).thenReturn(newSymmetricKeyWrapper);
        when(attr3.getName()).thenReturn("kat3");
        when(attr3.getKeyType()).thenReturn(symmetricKeyType);
        when(device.getSecurityAccessor(attr3)).thenReturn(Optional.empty());
        SecurityAccessor newSecurityAccessor = mock(SecurityAccessor.class);
        when(newSecurityAccessor.getActualValue()).thenReturn(Optional.empty(), Optional.of(newSymmetricKeyWrapper));
        when(newSecurityAccessor.getTempValue()).thenReturn(Optional.empty());
        when(device.newSecurityAccessor(attr3)).thenReturn(newSecurityAccessor);

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr3));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr3", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        ConfigurationSecurityProperty securityPropertyAttr3 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr3.getName()).thenReturn("attr3");
        when(securityPropertyAttr3.getSecurityAccessorType()).thenReturn(attr3);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr3));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));

        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(newSymmetricKeyWrapper).setProperties(mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getValue().get("key")).isEqualTo("MjIyMjIzMzMzMzQ0NDQ0NQ==");
    }

    @Test
    public void testSetNewActualKeyAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr3 = mock(SecurityAccessorType.class);
        PlaintextSymmetricKey newSymmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityManagementService.newSymmetricKeyWrapper(attr3)).thenReturn(newSymmetricKeyWrapper);
        when(attr3.getName()).thenReturn("kat3");
        when(attr3.getKeyType()).thenReturn(symmetricKeyType);
        SecurityAccessor securityAccessorWithoutActual = mock(SecurityAccessor.class);
        when(device.getSecurityAccessor(attr3)).thenReturn(Optional.ofNullable(securityAccessorWithoutActual));
        when(securityAccessorWithoutActual.getActualValue()).thenReturn(Optional.empty(), Optional.of(newSymmetricKeyWrapper));
        when(securityAccessorWithoutActual.getTempValue()).thenReturn(Optional.empty());
        when(device.newSecurityAccessor(attr3)).thenReturn(securityAccessorWithoutActual);

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr3));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr3", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        ConfigurationSecurityProperty securityPropertyAttr3 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr3.getName()).thenReturn("attr3");
        when(securityPropertyAttr3.getSecurityAccessorType()).thenReturn(attr3);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr3));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).warning(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));

        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(newSymmetricKeyWrapper).setProperties(mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getValue().get("key")).isEqualTo("MjIyMjIzMzMzMzQ0NDQ0NQ==");
    }

    @Test
    public void testIllegalActualKeyAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr3 = mock(SecurityAccessorType.class);
        PlaintextSymmetricKey newSymmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityManagementService.newSymmetricKeyWrapper(attr3)).thenReturn(newSymmetricKeyWrapper);
        when(attr3.getName()).thenReturn("kat3");
        when(attr3.getKeyType()).thenReturn(symmetricKeyType);
        SecurityAccessor securityAccessorWithoutActual = mock(SecurityAccessor.class);
        when(device.getSecurityAccessor(attr3)).thenReturn(Optional.ofNullable(securityAccessorWithoutActual));
        when(securityAccessorWithoutActual.getActualValue()).thenReturn(Optional.empty(), Optional.of(newSymmetricKeyWrapper));
        when(securityAccessorWithoutActual.getTempValue()).thenReturn(Optional.empty());
        when(device.newSecurityAccessor(attr3)).thenReturn(securityAccessorWithoutActual);
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        when(constraintViolation.getMessage()).thenReturn("Illegal key size");
        when(constraintViolation.getPropertyPath()).thenReturn(PathImpl.createPathFromString(""));
        doThrow(new ConstraintViolationException(Collections.singleton(constraintViolation))).when(newSymmetricKeyWrapper).setProperties(any(Map.class));

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr3));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr3", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        ConfigurationSecurityProperty securityPropertyAttr3 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr3.getName()).thenReturn("attr3");
        when(securityPropertyAttr3.getSecurityAccessorType()).thenReturn(attr3);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr3));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(anyString());
        verify(logger, times(1)).warning("Can't process line 2: The device can't be processed: property 'attr3': Illegal key size");
    }

    @Test
    public void testIllegalActualKeyPropertyAttributesForDeviceIdentifiedByMrid() {
        String csv = "Device MRID;Security settings name;attr1;attr2;attr3\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;MD5;MDEyMzQ1Njc4OTAxMjM0NQ==;SomePassword;MjIyMjIzMzMzMzQ0NDQ0NQ==";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);

        SecurityAccessorType attr3 = mock(SecurityAccessorType.class);
        PlaintextSymmetricKey newSymmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityManagementService.newSymmetricKeyWrapper(attr3)).thenReturn(newSymmetricKeyWrapper);
        when(attr3.getName()).thenReturn("kat3");
        when(attr3.getKeyType()).thenReturn(symmetricKeyType);
        SecurityAccessor securityAccessorWithoutActual = mock(SecurityAccessor.class);
        when(device.getSecurityAccessor(attr3)).thenReturn(Optional.ofNullable(securityAccessorWithoutActual));
        when(securityAccessorWithoutActual.getActualValue()).thenReturn(Optional.empty(), Optional.of(newSymmetricKeyWrapper));
        when(securityAccessorWithoutActual.getTempValue()).thenReturn(Optional.empty());
        when(device.newSecurityAccessor(attr3)).thenReturn(securityAccessorWithoutActual);
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        when(constraintViolation.getMessage()).thenReturn("Illegal key size");
        when(constraintViolation.getPropertyPath()).thenReturn(PathImpl.createPathFromString("key"));
        doThrow(new ConstraintViolationException(Collections.singleton(constraintViolation))).when(newSymmetricKeyWrapper).setProperties(any(Map.class));

        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(attr3));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = new LinkedHashSet<>(Arrays.asList(
                mockPropertySpec("attr3", new ReferenceValueFactory<SecurityAccessor>(ormService, beanService), false)));
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet("MD5", propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        ConfigurationSecurityProperty securityPropertyAttr3 = mock(ConfigurationSecurityProperty.class);
        when(securityPropertyAttr3.getName()).thenReturn("attr3");
        when(securityPropertyAttr3.getSecurityAccessorType()).thenReturn(attr3);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityPropertyAttr3));

        createSecurityAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(anyString());
        verify(logger, never()).severe(anyString());
        verify(importOccurrence).markFailure(anyString());
        verify(logger, times(1)).warning("Can't process line 2: The device can't be processed: property 'attr3': 'key': Illegal key size");
    }

    private Device mockDevice(String deviceName) {
        Device device = mock(Device.class);
        when(device.getName()).thenReturn(deviceName);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        return device;
    }

    private SecurityPropertySet mockSecurityPropertySet(String name, Set<PropertySpec> propertySpecs) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn(name);
        when(securityPropertySet.getPropertySpecs()).thenReturn(propertySpecs);
        return securityPropertySet;
    }

    private PropertySpec mockPropertySpec(String key, ValueFactory valueFactory, boolean required) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(key);
        when(propertySpec.isRequired()).thenReturn(required);
        doReturn(valueFactory).when(propertySpec).getValueFactory();
        return propertySpec;
    }

}
