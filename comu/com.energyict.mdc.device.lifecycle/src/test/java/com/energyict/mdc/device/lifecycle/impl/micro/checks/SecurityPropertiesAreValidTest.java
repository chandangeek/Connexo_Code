/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

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
 * Tests the {@link SecurityPropertiesAreValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (13:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertiesAreValidTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void validPropertiesWhenHavingKeyAccessorsForAllPropertySpecs() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        KeyAccessorType ekKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);
        KeyAccessor ekKeyAccessor = mock(KeyAccessor.class);
        when(ekKeyAccessor.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Arrays.asList(akKeyAccessor, ekKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

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

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(akKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(akKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

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

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        KeyAccessorType ekKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(akKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

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

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        KeyAccessorType ekKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(true);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(akKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
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

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(true);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(akKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(akKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void invalidPropertiesWhenHavingIncomleteKeyAccessor() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet notUsedSecurityPropertySet = mock(SecurityPropertySet.class);
        SecurityPropertySet usedSecurityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement enablement = mock(ComTaskEnablement.class);
        when(enablement.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(notUsedSecurityPropertySet, usedSecurityPropertySet));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(enablement));

        KeyAccessorType akKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty akKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(akKeySecurityProperty.getName()).thenReturn("AuthenticationKey");
        when(akKeySecurityProperty.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);
        KeyAccessorType ekKeyAccessorType = mock(KeyAccessorType.class);
        ConfigurationSecurityProperty ekKeySecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(ekKeySecurityProperty.getName()).thenReturn("EncryptionKey");
        when(ekKeySecurityProperty.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeySecurityProperty.getSecurityPropertySet()).thenReturn(usedSecurityPropertySet);

        PropertySpec akPropertySpec = mock(PropertySpec.class);
        when(akPropertySpec.getName()).thenReturn("AuthenticationKey");
        when(akPropertySpec.isRequired()).thenReturn(true);
        PropertySpec ekPropertySpec = mock(PropertySpec.class);
        when(ekPropertySpec.getName()).thenReturn("EncryptionKey");
        when(ekPropertySpec.isRequired()).thenReturn(false);

        when(usedSecurityPropertySet.getPropertySpecs()).thenReturn(new HashSet(Arrays.asList(akPropertySpec, ekPropertySpec)));
        when(usedSecurityPropertySet.getConfigurationSecurityProperties()).thenReturn(Arrays.asList(akKeySecurityProperty, ekKeySecurityProperty));

        KeyAccessor akKeyAccessor = mock(KeyAccessor.class);
        when(akKeyAccessor.getKeyAccessorType()).thenReturn(akKeyAccessorType);
        when(akKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.COMPLETE);
        KeyAccessor ekKeyAccessor = mock(KeyAccessor.class);
        when(ekKeyAccessor.getKeyAccessorType()).thenReturn(ekKeyAccessorType);
        when(ekKeyAccessor.getStatus()).thenReturn(KeyAccessorStatus.INCOMPLETE);

        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getKeyAccessors()).thenReturn(Arrays.asList(akKeyAccessor, ekKeyAccessor));

        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
    }

    private SecurityPropertiesAreValid getTestInstance() {
        return new SecurityPropertiesAreValid(this.thesaurus);
    }

}