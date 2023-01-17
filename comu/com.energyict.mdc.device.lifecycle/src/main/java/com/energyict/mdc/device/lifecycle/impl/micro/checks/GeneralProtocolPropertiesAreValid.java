/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.upl.TypedProperties;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that all the mandatory general protocol properties are set on the Device.
 * In case a property is a KeyAccessorType, we also check the device has a value (KeyAccessor) for the KeyAccessorType
 * and the KeyAccessor has an actualValue
 */
public class GeneralProtocolPropertiesAreValid extends ConsolidatedServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return anyMissingProperty(device) ?
                fail(MicroCheckTranslations.Message.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID) :
                Optional.empty();
    }

    private boolean anyMissingProperty(Device device) {
        Set<String> requiredPropertyNames = requiredGeneralProtocolPropertyNames(device);
        TypedProperties deviceProtocolProperties = device.getDeviceProtocolProperties();
        Set<String> availablePropertyNames = deviceProtocolProperties.propertyNames();
        if (requiredPropertyNames.stream().anyMatch(each -> !availablePropertyNames.contains(each))) {
            return true;
        }
        return deviceProtocolProperties
                .stream()
                .filter(prop -> prop.getValue() instanceof SecurityAccessorType)
                .map(prop -> (SecurityAccessorType) prop.getValue())
                .map(device::getSecurityAccessor)
                .anyMatch(ka->!ka.isPresent() || !ka.get().getActualValue().isPresent());
    }

    private Set<String> requiredGeneralProtocolPropertyNames(Device device) {
        return device
                .getDeviceType()
                .getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass
                        .getDeviceProtocol()
                        .getPropertySpecs()
                        .stream()
                        .filter(PropertySpec::isRequired)
                        .map(PropertySpec::getName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
}
