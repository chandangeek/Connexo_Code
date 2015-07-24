package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.SingleComTaskComTaskExecution;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

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
public class ProtocolDialectPropertiesAreValid implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ProtocolDialectPropertiesAreValid(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        Set<PropertySpec> requiredPropertySpecs = device.getDeviceConfiguration().getComTaskEnablements().stream().map(ComTaskEnablement::getProtocolDialectConfigurationProperties)
                .flatMap(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getPropertySpecs().stream())
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toSet());

        requiredPropertySpecs.addAll(device.getComTaskExecutions().stream().filter(comTaskExecution -> comTaskExecution instanceof SingleComTaskComTaskExecution)
                .flatMap(comTaskExecution -> ((SingleComTaskComTaskExecution) comTaskExecution).getProtocolDialectConfigurationProperties().getPropertySpecs().stream())
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toSet()));

        Optional<PropertySpec> unsolvedRequiredProperty = requiredPropertySpecs.stream()
                .filter(findDialectProperty(device).negate())
                .findFirst();
        if (unsolvedRequiredProperty.isPresent()) {
            return Optional.of(newViolation());
        }
        return Optional.empty();
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
                .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getProperty(propertySpec.getName()) != null).findFirst();
    }

    private Optional<ProtocolDialectProperties> findDialectPropertyOnDeviceLevel(Device device, PropertySpec propertySpec) {
        return device.getProtocolDialectPropertiesList().stream()
                .filter(protocolDialectProperties -> protocolDialectProperties.getProperty(propertySpec.getName()) != null).findFirst();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

}