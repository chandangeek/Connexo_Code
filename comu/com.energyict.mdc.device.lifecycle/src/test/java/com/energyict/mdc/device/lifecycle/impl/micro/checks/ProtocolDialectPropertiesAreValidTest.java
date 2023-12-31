/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.common.pluggable.PluggableClassUsageProperty;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
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
 * Tests the {@link ProtocolDialectPropertiesAreValid} component
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectPropertiesAreValidTest {

    private static final String DIALECT1_NAME = "One";
    private static final String DIALECT2_NAME = "Two";

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties1;
    @Mock
    private PartialConnectionTask partialConnectionTask1, partialConnectionTask2;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties1;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties2;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties2;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.protocolDialectConfigurationProperties1.getDeviceProtocolDialectName()).thenReturn(DIALECT1_NAME);
        when(this.protocolDialectConfigurationProperties2.getDeviceProtocolDialectName()).thenReturn(DIALECT2_NAME);
        when(this.partialConnectionTask1.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties1);
        when(this.partialConnectionTask2.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties2);
        when(this.deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
        when(this.protocolDialectProperties1.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties1);
        when(this.protocolDialectProperties1.getDeviceProtocolDialectName()).thenReturn(DIALECT1_NAME);
        when(this.protocolDialectProperties2.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties2);
        when(this.protocolDialectProperties2.getDeviceProtocolDialectName()).thenReturn(DIALECT2_NAME);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Arrays.asList(protocolDialectConfigurationProperties1, protocolDialectConfigurationProperties2));
    }

    @Test
    public void allDialectsAreValid() {
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void requiredPropertyIsNoFilledInTest() {
        String propName = "MyPropertyName";
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        PropertySpec requiredPropertySpec = mock(PropertySpec.class);
        when(requiredPropertySpec.getName()).thenReturn(propName);
        when(requiredPropertySpec.isRequired()).thenReturn(true);
        when(protocolDialectConfigurationProperties1.getPropertySpecs()).thenReturn(Collections.singletonList(requiredPropertySpec));
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void requiredPropertyIsFilledInOnDeviceLevelTest() {
        String propName = "MyPropertyName";
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        PropertySpec requiredPropertySpec = mock(PropertySpec.class);
        when(requiredPropertySpec.getName()).thenReturn(propName);
        when(requiredPropertySpec.isRequired()).thenReturn(true);
        when(protocolDialectConfigurationProperties1.getPropertySpecs()).thenReturn(Collections.singletonList(requiredPropertySpec));
        ProtocolDialectProperties dialectPropertyList = mock(ProtocolDialectProperties.class);
        PluggableClassUsageProperty propertyValue = mock(PluggableClassUsageProperty.class);
        when(dialectPropertyList.getProperty(propName)).thenReturn(propertyValue);
        when(device.getProtocolDialectPropertiesList()).thenReturn(Collections.singletonList(dialectPropertyList));

        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void requiredPropertyIsFilledInOnConfigLevelTest() {
        String propName = "MyPropertyName";
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        PropertySpec requiredPropertySpec = mock(PropertySpec.class);
        when(requiredPropertySpec.getName()).thenReturn(propName);
        when(requiredPropertySpec.isRequired()).thenReturn(true);
        when(protocolDialectConfigurationProperties1.getPropertySpecs()).thenReturn(Collections.singletonList(requiredPropertySpec));
        String myPropertyValue = "MyPropertyValue";
        when(protocolDialectConfigurationProperties1.getProperty(propName)).thenReturn(myPropertyValue);

        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    public ProtocolDialectPropertiesAreValid getTestInstance() {
        ProtocolDialectPropertiesAreValid protocolDialectPropertiesAreValid =
                new ProtocolDialectPropertiesAreValid();
        protocolDialectPropertiesAreValid.setThesaurus(this.thesaurus);
        return protocolDialectPropertiesAreValid;
    }
}
