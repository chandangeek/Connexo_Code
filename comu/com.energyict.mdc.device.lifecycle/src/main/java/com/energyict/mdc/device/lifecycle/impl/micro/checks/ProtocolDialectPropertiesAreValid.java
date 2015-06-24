package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Predicates;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a Device has valid properties
 * for all protocol dialects that are used at the configuration level
 * when communication tasks are being enabled.
 * @see ComTaskEnablement#getProtocolDialectConfigurationProperties()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class ProtocolDialectPropertiesAreValid implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ProtocolDialectPropertiesAreValid(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        long numberOfMissingDialects = device
            .getDeviceConfiguration()
            .getComTaskEnablements()
            .stream()
            .map(ComTaskEnablement::getProtocolDialectConfigurationProperties)
            .filter(Objects::nonNull)
            .map(pcp -> device.getProtocolDialectProperties(pcp.getDeviceProtocolDialectName()))
            .filter(Predicates.not(Optional::isPresent))
            .count();
        if (numberOfMissingDialects > 0) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

}