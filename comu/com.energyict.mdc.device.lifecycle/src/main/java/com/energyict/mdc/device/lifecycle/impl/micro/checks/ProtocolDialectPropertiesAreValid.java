/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The device communication protocol can define one or more protocol dialects on the device. Each dialect has one ore more attributes (key / value pair)
 * The check will verify that when an attribute is marked as mandatory, a value is foreseen. If no value foreseen on at least one mandatory attribute, the check will fail.
 * Only the protocol dialects that are used on one of the communication tasks of the device are checked
 */
public class ProtocolDialectPropertiesAreValid extends ConsolidatedServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        Set<PropertySpec> requiredPropertySpecs = device.getDeviceConfiguration().getPartialConnectionTasks().stream().map(PartialConnectionTask::getProtocolDialectConfigurationProperties)
                .flatMap(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getPropertySpecs().stream())
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toSet());
        requiredPropertySpecs.addAll(device.getConnectionTasks().stream()
                .flatMap(connectionTask -> connectionTask.getProtocolDialectConfigurationProperties().getPropertySpecs().stream())
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toSet()));
        Optional<PropertySpec> unsolvedRequiredProperty = requiredPropertySpecs.stream()
                .filter(findDialectProperty(device).negate())
                .findFirst();
        return unsolvedRequiredProperty.isPresent() ?
                fail(MicroCheckTranslations.Message.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID) :
                Optional.empty();
    }

    private Predicate<PropertySpec> findDialectProperty(Device device) {
        return propertySpec -> {
            Optional<ProtocolDialectProperties> dialectPropertiesOnDevice = findDialectPropertyOnDeviceLevel(device, propertySpec);
            Optional<ProtocolDialectConfigurationProperties> dialectConfigurationProperties = findDialectPropertyOnConfigurationLevel(device, propertySpec);
            return dialectPropertiesOnDevice.isPresent() || dialectConfigurationProperties.isPresent();
        };
    }

    private Optional<ProtocolDialectConfigurationProperties> findDialectPropertyOnConfigurationLevel(Device device, PropertySpec propertySpec) {
        return device.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList()
                .stream()
                .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getProperty(propertySpec.getName()) != null)
                .findFirst();
    }

    private Optional<ProtocolDialectProperties> findDialectPropertyOnDeviceLevel(Device device, PropertySpec propertySpec) {
        return device.getProtocolDialectPropertiesList().stream()
                .filter(protocolDialectProperties -> protocolDialectProperties.getProperty(propertySpec.getName()) != null)
                .findFirst();
    }
}
