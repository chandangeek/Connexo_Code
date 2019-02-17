/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.upl.TypedProperties;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all the mandatory general protocol properties are set on the Device.
 * In case a property is a KeyAccessorType, we also check the device has a value (KeyAccessor) for the KeyAccessorType
 * and the KeyAccessor has an actualValue
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (09:39)
 */
public class GeneralProtocolPropertiesAreValid extends ConsolidatedServerMicroCheck {

    public GeneralProtocolPropertiesAreValid(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public Optional<EvaluableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (anyMissingProperty(device)) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
    }

    private boolean anyMissingProperty(Device device) {
        Set<String> requiredPropertyNames = requiredGeneralProtocolPropertyNames(device);
        TypedProperties deviceProtocolProperties = device.getDeviceProtocolProperties();
        Set<String> availablePropertyNames = deviceProtocolProperties.propertyNames();
        if (requiredPropertyNames.stream().anyMatch(each -> !availablePropertyNames.contains(each))) {
            return true;
        }
        return deviceProtocolProperties.stream()
                .filter(prop->prop.getValue() instanceof SecurityAccessorType)
                .map(prop-> (SecurityAccessorType)prop.getValue())
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
                        .collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }

}