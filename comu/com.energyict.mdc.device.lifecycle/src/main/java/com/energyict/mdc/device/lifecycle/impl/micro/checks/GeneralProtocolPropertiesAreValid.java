package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all the mandatory general protocol properties are set on the Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (09:39)
 */
public class GeneralProtocolPropertiesAreValid extends ConsolidatedServerMicroCheck {

    public GeneralProtocolPropertiesAreValid(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (anyMissingProperty(device).isPresent()) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<String> anyMissingProperty(Device device) {
        Set<String> requiredPropertyNames = requiredGeneralProtocolPropertyNames(device);
        Set<String> availablePropertyNames = device.getDeviceProtocolProperties().propertyNames();
        return requiredPropertyNames.stream().filter(each -> !availablePropertyNames.contains(each)).findAny();
    }

    private Set<String> requiredGeneralProtocolPropertyNames(Device device) {
        return device
                .getDeviceType()
                .getDeviceProtocolPluggableClass()
                .getDeviceProtocol()
                .getPropertySpecs()
                .stream()
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .collect(Collectors.toSet());
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }

}