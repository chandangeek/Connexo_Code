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
import java.util.Optional;

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
    private static final String NON_EXISTING_PROPERTY_SPEC_NAME = "notSupposedToExist";

    @Before
    public void initializeProtocolProperties() {
        PropertySpec<BigDecimal> bigDecimalPropertySpec = inMemoryPersistence.getPropertySpecService().basicPropertySpec(NUMERIC_PROPERTY_SPEC_NAME, true, new BigDecimalFactory());
        PropertySpec<String> stringPropertySpec = inMemoryPersistence.getPropertySpecService().basicPropertySpec(STRING_PROPERTY_SPEC_NAME, true, new StringFactory());
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(bigDecimalPropertySpec, stringPropertySpec));
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
        assertThat(protocolProperties.getTypedProperties()).isNotNull();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(NUMERIC_PROPERTY_SPEC_NAME)).isFalse();
        assertThat(protocolProperties.getTypedProperties().hasValueFor(STRING_PROPERTY_SPEC_NAME)).isFalse();
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

}