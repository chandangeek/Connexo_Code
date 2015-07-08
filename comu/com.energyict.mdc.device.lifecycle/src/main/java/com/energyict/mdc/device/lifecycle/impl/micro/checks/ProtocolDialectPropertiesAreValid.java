package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Predicates;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 *The device communication protocol can define one or more protocol dialects on the device. Each dialect has one ore more attributes (key / value pair)
 *The check will verify that when an attribute is marked as mandatory, a value is foreseen. If no value foreseen on at least one mandatory attribute, the check will fail.
 *Only the protocol dialects that are used on one of the communication tasks of the device are checked
 */
public class ProtocolDialectPropertiesAreValid implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ProtocolDialectPropertiesAreValid(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        List<ProtocolDialectConfigurationProperties> protocolDialectPropsNotUsedByDevice = device
            .getDeviceConfiguration()
            .getComTaskEnablements()
            .stream()
            .map(ComTaskEnablement::getProtocolDialectConfigurationProperties)
            .filter(Predicates.not(pcp -> device.getProtocolDialectProperties(pcp.getDeviceProtocolDialectName()).isPresent()))
            .collect(Collectors.toList());
        if (!protocolDialectPropsNotUsedByDevice.isEmpty()) {
            if (protocolDialectPropsNotUsedByDevice.stream().anyMatch(Predicates.not(ProtocolDialectConfigurationProperties::isComplete))){
                return Optional.of(newViolation());
            }
        }
        return Optional.empty();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

}