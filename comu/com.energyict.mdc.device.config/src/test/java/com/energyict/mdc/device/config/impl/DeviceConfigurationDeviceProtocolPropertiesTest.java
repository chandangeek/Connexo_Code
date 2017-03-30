/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyException;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test the general protocol properties on the {@link DeviceConfigurationImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (10:35)
 */
public class DeviceConfigurationDeviceProtocolPropertiesTest extends DeviceTypeProvidingPersistenceTest {

    private static final String NUMERIC_PROPERTY_SPEC_NAME = "numerical";
    private static final String STRING_PROPERTY_SPEC_NAME = "textual";
    private static final String DEFAULT_PROPERTY_SPEC_NAME = "withDefault";
    private static final String DEFAULT_VALUE = "Default for property: " + DEFAULT_PROPERTY_SPEC_NAME;
    private static final String NON_EXISTING_PROPERTY_SPEC_NAME = "notSupposedToExist";

    @Before
    public void initializeProtocolProperties() {
        PropertySpec bigDecimalPropertySpec = inMemoryPersistence.getPropertySpecService()
                .bigDecimalSpec()
                .named(NUMERIC_PROPERTY_SPEC_NAME, NUMERIC_PROPERTY_SPEC_NAME)
                .describedAs(NUMERIC_PROPERTY_SPEC_NAME)
                .markRequired()
                .finish();
        PropertySpec stringPropertySpec = inMemoryPersistence.getPropertySpecService()
                .stringSpec()
                .named(STRING_PROPERTY_SPEC_NAME, STRING_PROPERTY_SPEC_NAME)
                .describedAs(STRING_PROPERTY_SPEC_NAME)
                .markRequired()
                .finish();
        PropertySpec defaultPropertySpec = inMemoryPersistence.getPropertySpecService()
                .stringSpec()
                .named(DEFAULT_PROPERTY_SPEC_NAME, DEFAULT_PROPERTY_SPEC_NAME)
                .describedAs(DEFAULT_PROPERTY_SPEC_NAME)
                .markRequired()
                .setDefaultValue(DEFAULT_VALUE)
                .finish();
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(bigDecimalPropertySpec, stringPropertySpec, defaultPropertySpec));
    }

    @Test
    @Transactional
    public void testNewConfigurationInheritsPropertiesFromPluggableClass() {
        TypedProperties pluggableClassProperties = TypedProperties.empty();
        BigDecimal expectedNumericPropertyValue = BigDecimal.TEN;
        pluggableClassProperties.setProperty(NUMERIC_PROPERTY_SPEC_NAME, expectedNumericPropertyValue);
        String expectedStringPropertyValue = "testNewConfigurationInheritsPropertiesFromProtocol";
        pluggableClassProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);
        when(deviceProtocolPluggableClass.getProperties()).thenReturn(pluggableClassProperties);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationInheritsPropertiesFromProtocol");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        // Business method
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();

        // Asserts
        assertThat(protocolProperties).isNotNull();
        assertThat(protocolProperties.getDeviceConfiguration()).isEqualTo(deviceConfiguration);
        assertThat(protocolProperties.getTypedProperties()).isNotNull();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(protocolProperties.getTypedProperties().getProperty(NUMERIC_PROPERTY_SPEC_NAME)).isEqualTo(expectedNumericPropertyValue);
        assertThat(protocolProperties.getTypedProperties().hasLocalValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(protocolProperties.getTypedProperties().getProperty(STRING_PROPERTY_SPEC_NAME)).isEqualTo(expectedStringPropertyValue);
        assertThat(protocolProperties.getTypedProperties().hasLocalValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasLocalValueFor(DEFAULT_PROPERTY_SPEC_NAME)).isFalse();
    }

    @Test(expected = NoSuchPropertyException.class)
    @Transactional
    public void testAddNonExistingProperty() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();

        // Business method
        protocolProperties.setProperty(NON_EXISTING_PROPERTY_SPEC_NAME, "Does not really matter");

        // Asserts: see expected rule
    }

    @Test
    @Transactional
    public void testAddPropertyWithoutSaving() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();

        // Business method
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

    @Test
    @Transactional
    public void testSetPropertyTwice() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Will be overruled soon");

        // Business method
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(reloadedProtocolProperties.getTypedProperties().getProperty(STRING_PROPERTY_SPEC_NAME)).isEqualTo(expectedStringPropertyValue);
    }

    @Test
    @Transactional
    public void testAddProperty() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(reloadedProtocolProperties.getTypedProperties().getProperty(STRING_PROPERTY_SPEC_NAME)).isEqualTo(expectedStringPropertyValue);
    }

    @Test
    @Transactional
    public void testOverruleDefaultProperty() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(DEFAULT_PROPERTY_SPEC_NAME, expectedStringPropertyValue);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(DEFAULT_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(reloadedProtocolProperties.getTypedProperties().getProperty(DEFAULT_PROPERTY_SPEC_NAME)).isEqualTo(expectedStringPropertyValue);
    }

    @Test
    @Transactional
    public void testSetAndGetPropertyWithoutSaving() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        String expectedStringPropertyValue = "Actual string value";

        // Business method
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);

        // Asserts
        assertThat(protocolProperties.getProperty(STRING_PROPERTY_SPEC_NAME)).isEqualTo(expectedStringPropertyValue);
    }

    @Test
    @Transactional
    public void testAddAndRemoveSamePropertyWithOneSave() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        protocolProperties.removeProperty(STRING_PROPERTY_SPEC_NAME);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

    @Test
    @Transactional
    public void testRemoveProperty() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(STRING_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

    @Test
    @Transactional
    public void testRemovePropertyThatWasNotSet() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(NUMERIC_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isTrue();
    }

    @Test(expected = NoSuchPropertyException.class)
    @Transactional
    public void testRemovePropertyThatDoesNotExis() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(NON_EXISTING_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

    @Test
    @Transactional
    public void testRemovePropertyWithSetterMethod() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        DeviceProtocolConfigurationProperties protocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, null);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        DeviceProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getDeviceProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

}