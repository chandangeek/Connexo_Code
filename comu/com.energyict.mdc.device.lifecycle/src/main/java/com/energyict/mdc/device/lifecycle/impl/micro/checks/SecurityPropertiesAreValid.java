/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a Device has valid security properties
 * for all {@link SecurityPropertySet}s that are used at the
 * configuration level when communication tasks are being enabled.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class SecurityPropertiesAreValid extends ConsolidatedServerMicroCheck {

    private boolean valid = true;

    public SecurityPropertiesAreValid(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public Optional<EvaluableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<SecurityPropertySet> usedSecurityPropertySets = deviceConfiguration.getSecurityPropertySets()
                .stream()
                .filter(securityPropertySet -> isUsedInAnyComTaskEnablement(securityPropertySet, deviceConfiguration.getComTaskEnablements()))
                .collect(Collectors.toList());

        Map<PropertySpec, Optional<SecurityAccessorType>> propertySpecKeyAccessorTypeMapping = new HashMap<>();
        for (SecurityPropertySet usedSecurityPropertySet : usedSecurityPropertySets) {
            usedSecurityPropertySet.getPropertySpecs().forEach(propertySpec ->
                    propertySpecKeyAccessorTypeMapping.put(propertySpec, findCorrespondingKeyAccessorType(usedSecurityPropertySet, propertySpec))
            );
        }

        propertySpecKeyAccessorTypeMapping.forEach((propertySpec, keyAccessorTypeOptional) -> checkIfValid(propertySpec, keyAccessorTypeOptional, device));
        return this.valid ? Optional.empty() : Optional.of(newViolation());
    }

    private Optional<SecurityAccessorType> findCorrespondingKeyAccessorType(SecurityPropertySet securityPropertySet, PropertySpec propertySpec) {
        return securityPropertySet.getConfigurationSecurityProperties()
                .stream()
                .filter(property -> property.getName().equals(propertySpec.getName()))
                .findFirst()
                .map(ConfigurationSecurityProperty::getSecurityAccessorType);
    }

    private boolean isUsedInAnyComTaskEnablement(SecurityPropertySet securityPropertySet, List<ComTaskEnablement> comTaskEnablements) {
        return comTaskEnablements
                .stream()
                .anyMatch(enablement -> enablement.getSecurityPropertySet().equals(securityPropertySet));
    }

    private void checkIfValid(PropertySpec propertySpec, Optional<SecurityAccessorType> keyAccessorTypeOptional, Device device) {
        if (keyAccessorTypeOptional.isPresent()) {
            checkIfDeviceHasValidKeyAccessorCorrespondingTo(propertySpec, keyAccessorTypeOptional.get(), device);
        } else if (propertySpec.isRequired()) {
            this.valid = false; // A required property spec without corresponding KeyAccessor(type)
        }
    }

    private void checkIfDeviceHasValidKeyAccessorCorrespondingTo(PropertySpec propertySpec, SecurityAccessorType securityAccessorType, Device device) {
        Optional<SecurityAccessor> keyAccessorOptional = device.getSecurityAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().equals(securityAccessorType))
                .findFirst();
        if (keyAccessorOptional.isPresent()) {
            if (!keyAccessorOptional.get().getStatus().equals(KeyAccessorStatus.COMPLETE)) {
                this.valid = false; // KeyAccessor is not complete
            }
        } else if (propertySpec.isRequired()){
            this.valid = false; // Didn't found a matching security accessor for the a required security accessor type
        }
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.SECURITY_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
    }
}