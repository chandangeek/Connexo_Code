/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link GeneralProtocolPropertiesAreValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (14:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneralProtocolPropertiesAreValidTest {

    public static final String OPTIONAL_PROPERTY_NAME = "optional";
    public static final String REQUIRED_PROPERTY_NAME = "required";
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;

    @Before
    public void initializeMocks() {
        PropertySpec optional = mockedPropertySpec(OPTIONAL_PROPERTY_NAME, true);
        PropertySpec required = mockedPropertySpec(REQUIRED_PROPERTY_NAME, false);
        when(this.deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(optional, required));
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
    }

    @Test
    public void allPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        when(this.device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void allRequiredPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OPTIONAL_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void allOptionalPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REQUIRED_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void noPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OPTIONAL_PROPERTY_NAME, "not missing");
        properties.setProperty(REQUIRED_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void requiredPropertiesAreSpecifiedAtConfigurationLevel() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, "not missing");
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void requiredPropertiesContainKeyAccessorTypes() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        KeyAccessorType requiredKey = mock(KeyAccessorType.class);
        KeyAccessorType optionalKey = mock(KeyAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        KeyAccessor accessor = mock(KeyAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getKeyAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getKeyAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithOptionalMissing() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        KeyAccessorType requiredKey = mock(KeyAccessorType.class);
        KeyAccessorType optionalKey = mock(KeyAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        KeyAccessor accessor = mock(KeyAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getKeyAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getKeyAccessor(optionalKey)).thenReturn(Optional.empty());
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithOptionalMissingActualValue() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        KeyAccessorType requiredKey = mock(KeyAccessorType.class);
        KeyAccessorType optionalKey = mock(KeyAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        KeyAccessor accessor = mock(KeyAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        KeyAccessor optionalAccessor = mock(KeyAccessor.class);
        when(optionalAccessor.getActualValue()).thenReturn(Optional.empty());
        when(device.getKeyAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getKeyAccessor(optionalKey)).thenReturn(Optional.of(optionalAccessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithRequiredMissing() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        KeyAccessorType requiredKey = mock(KeyAccessorType.class);
        KeyAccessorType optionalKey = mock(KeyAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        KeyAccessor accessor = mock(KeyAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getKeyAccessor(requiredKey)).thenReturn(Optional.empty());
        when(device.getKeyAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithRequiredMissingActualValue() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        KeyAccessorType requiredKey = mock(KeyAccessorType.class);
        KeyAccessorType optionalKey = mock(KeyAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        KeyAccessor accessor = mock(KeyAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        KeyAccessor requiredAccessor = mock(KeyAccessor.class);
        when(requiredAccessor.getActualValue()).thenReturn(Optional.empty());
        when(device.getKeyAccessor(requiredKey)).thenReturn(Optional.of(requiredAccessor));
        when(device.getKeyAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
    }

    private PropertySpec mockedPropertySpec(String name, boolean optional) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.isReference()).thenReturn(false);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.isRequired()).thenReturn(!optional);
        return propertySpec;
    }

    private GeneralProtocolPropertiesAreValid getTestInstance() {
        return new GeneralProtocolPropertiesAreValid(this.thesaurus);
    }

}