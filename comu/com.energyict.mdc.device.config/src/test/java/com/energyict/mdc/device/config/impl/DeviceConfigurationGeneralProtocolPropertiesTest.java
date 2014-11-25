package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyException;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test the general protocol properties on the {@link DeviceConfigurationImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (10:35)
 */
public class DeviceConfigurationGeneralProtocolPropertiesTest extends DeviceTypeProvidingPersistenceTest {

    private static final String NUMERIC_PROPERTY_SPEC_NAME = "numerical";
    private static final String STRING_PROPERTY_SPEC_NAME = "textual";
    private static final String DEFAULT_PROPERTY_SPEC_NAME = "withDefault";
    private static final String DEFAULT_VALUE = "Default for property: " + DEFAULT_PROPERTY_SPEC_NAME;
    private static final String NON_EXISTING_PROPERTY_SPEC_NAME = "notSupposedToExist";

    @Before
    public void initializeProtocolProperties() {
        PropertySpec<BigDecimal> bigDecimalPropertySpec = inMemoryPersistence.getPropertySpecService().basicPropertySpec(NUMERIC_PROPERTY_SPEC_NAME, true, new BigDecimalFactory());
        PropertySpec<String> stringPropertySpec = inMemoryPersistence.getPropertySpecService().basicPropertySpec(STRING_PROPERTY_SPEC_NAME, true, new StringFactory());
        PropertySpec<String> defaultPropertySpec = inMemoryPersistence.getPropertySpecService().stringPropertySpec(DEFAULT_PROPERTY_SPEC_NAME, true, DEFAULT_VALUE);
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(bigDecimalPropertySpec, stringPropertySpec, defaultPropertySpec));
    }

    @Test
    @Transactional
    public void testNewConfigurationHasEmptyProperties() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        // Business method
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();

        // Asserts
        assertThat(protocolProperties).isNotNull();
        assertThat(protocolProperties.getDeviceConfiguration()).isEqualTo(deviceConfiguration);
        assertThat(protocolProperties.getTypedProperties()).isNotNull();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasLocalValueFor(DEFAULT_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(DEFAULT_PROPERTY_SPEC_NAME)).isTrue();
        assertThat(protocolProperties.getTypedProperties().getProperty(DEFAULT_PROPERTY_SPEC_NAME)).isEqualTo(DEFAULT_VALUE);
    }

    @Test(expected = NoSuchPropertyException.class)
    @Transactional
    public void testAddNonExistingProperty() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();

        // Business method
        protocolProperties.setProperty(NON_EXISTING_PROPERTY_SPEC_NAME, "Does not really matter");

        // Asserts: see expected rule
    }

    @Test
    @Transactional
    public void testAddPropertyWithoutSaving() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("testNewConfigurationHasEmptyProperties");
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();

        // Business method
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Will be overruled soon");

        // Business method
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, expectedStringPropertyValue);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        String expectedStringPropertyValue = "Actual string value";
        protocolProperties.setProperty(DEFAULT_PROPERTY_SPEC_NAME, expectedStringPropertyValue);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        protocolProperties.removeProperty(STRING_PROPERTY_SPEC_NAME);

        // Business method
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(STRING_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(NUMERIC_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.removeProperty(NON_EXISTING_PROPERTY_SPEC_NAME);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
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
        ProtocolConfigurationProperties protocolProperties = deviceConfiguration.getProtocolProperties();
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, "Does not really matter");
        deviceConfiguration.save();

        // Business method
        protocolProperties.setProperty(STRING_PROPERTY_SPEC_NAME, null);
        deviceConfiguration.save();

        // Asserts
        DeviceConfiguration reloadedDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId()).get();
        ProtocolConfigurationProperties reloadedProtocolProperties = reloadedDeviceConfiguration.getProtocolProperties();
        assertThat(reloadedProtocolProperties).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties()).isNotNull();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(reloadedProtocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
    }

}