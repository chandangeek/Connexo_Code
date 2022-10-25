/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.upl.TypedProperties;

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
 * Test the {@link GeneralProtocolPropertiesAreValid} component
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneralProtocolPropertiesAreValidTest {

    public static final String OPTIONAL_PROPERTY_NAME = "optional";
    public static final String REQUIRED_PROPERTY_NAME = "required";
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
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
    @Mock
    private State state;

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
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void allRequiredPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OPTIONAL_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void allOptionalPropertiesAreMissing() {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REQUIRED_PROPERTY_NAME, "not missing");

        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void requiredPropertiesContainKeyAccessorTypes() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        SecurityAccessorType requiredKey = mock(SecurityAccessorType.class);
        SecurityAccessorType optionalKey = mock(SecurityAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getSecurityAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getSecurityAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithOptionalMissing() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        SecurityAccessorType requiredKey = mock(SecurityAccessorType.class);
        SecurityAccessorType optionalKey = mock(SecurityAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getSecurityAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getSecurityAccessor(optionalKey)).thenReturn(Optional.empty());
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithOptionalMissingActualValue() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        SecurityAccessorType requiredKey = mock(SecurityAccessorType.class);
        SecurityAccessorType optionalKey = mock(SecurityAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        SecurityAccessor optionalAccessor = mock(SecurityAccessor.class);
        when(optionalAccessor.getActualValue()).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(requiredKey)).thenReturn(Optional.of(accessor));
        when(device.getSecurityAccessor(optionalKey)).thenReturn(Optional.of(optionalAccessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithRequiredMissing() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        SecurityAccessorType requiredKey = mock(SecurityAccessorType.class);
        SecurityAccessorType optionalKey = mock(SecurityAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        when(device.getSecurityAccessor(requiredKey)).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(),state);

        // Asserts
        assertThat(violation).isPresent();
    }

    @Test
    public void propertiesContainKeyAccessorTypesWithRequiredMissingActualValue() throws Exception {
        GeneralProtocolPropertiesAreValid microCheck = this.getTestInstance();
        TypedProperties configurationProperties = TypedProperties.empty();
        SecurityAccessorType requiredKey = mock(SecurityAccessorType.class);
        SecurityAccessorType optionalKey = mock(SecurityAccessorType.class);
        configurationProperties.setProperty(REQUIRED_PROPERTY_NAME, requiredKey);
        TypedProperties properties = TypedProperties.inheritingFrom(configurationProperties);
        properties.setProperty(OPTIONAL_PROPERTY_NAME, optionalKey);
        SecurityAccessor accessor = mock(SecurityAccessor.class);
        SecurityValueWrapper securityValueWrapper = mock(SecurityValueWrapper.class);
        when(accessor.getActualValue()).thenReturn(Optional.of(securityValueWrapper));
        SecurityAccessor requiredAccessor = mock(SecurityAccessor.class);
        when(requiredAccessor.getActualValue()).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(requiredKey)).thenReturn(Optional.of(requiredAccessor));
        when(device.getSecurityAccessor(optionalKey)).thenReturn(Optional.of(accessor));
        when(this.device.getDeviceProtocolProperties()).thenReturn(properties);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        GeneralProtocolPropertiesAreValid generalProtocolPropertiesAreValid =
                new GeneralProtocolPropertiesAreValid();
        generalProtocolPropertiesAreValid.setThesaurus(this.thesaurus);
        return generalProtocolPropertiesAreValid;
    }
}
