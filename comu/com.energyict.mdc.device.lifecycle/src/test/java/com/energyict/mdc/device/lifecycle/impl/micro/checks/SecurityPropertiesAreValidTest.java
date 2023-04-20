/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SecurityPropertiesAreValid} component
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertiesAreValidTest {

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Device device;
    @Mock
    private State state;

    @Test
    public void validPropertiesWhenHavingKeyAccessorsForAllPropertySpecs() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        SecurityAccessorType ekSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);
        SecurityAccessor ekSecurityAccessor = mock(SecurityAccessor.class);
        when(ekSecurityAccessor.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(akSecurityAccessor, ekSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void validPropertiesWithoutKeyAccessorTypeForNonRequiredProperty() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(akKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(akSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void validPropertiesWithoutKeyAccessorForNonRequiredProperty() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        SecurityAccessorType ekSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(akSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void invalidPropertiesWhenMissingKeyAccessorsForRequiredPropertySpecs() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        SecurityAccessorType ekSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(true);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(akSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void invalidPropertiesWhenMissingKeyAccessorTypesForRequiredPropertySpecs() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(true);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(akKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(akSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void invalidPropertiesWhenHavingIncompleteKeyAccessor() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        SecurityAccessorType akSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        SecurityAccessorType ekSecurityAccessorType = mock(SecurityAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        SecurityAccessor akSecurityAccessor = mock(SecurityAccessor.class);
        when(akSecurityAccessor.getSecurityAccessorType()).thenReturn(akSecurityAccessorType);
        when(akSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);
        SecurityAccessor ekSecurityAccessor = mock(SecurityAccessor.class);
        when(ekSecurityAccessor.getSecurityAccessorType()).thenReturn(ekSecurityAccessorType);
        when(ekSecurityAccessor.getStatus()).thenReturn(KeyAccessorStatus.INCOMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(akSecurityAccessor, ekSecurityAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    private SecurityPropertiesAreValid getTestInstance() {
        SecurityPropertiesAreValid securityPropertiesAreValid = new SecurityPropertiesAreValid();
        securityPropertiesAreValid.setThesaurus(this.thesaurus);
        return securityPropertiesAreValid;
    }
}
