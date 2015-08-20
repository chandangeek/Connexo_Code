package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a Device has valid security properties
 * for all {@link SecurityPropertySet}s that are used at the
 * configuration level when communication tasks are being enabled.
 * @see ComTaskEnablement#getSecurityPropertySet()
 * @see Device#securityPropertiesAreValid()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class SecurityPropertiesAreValid extends ConsolidatedServerMicroCheck {

    public SecurityPropertiesAreValid(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!device.securityPropertiesAreValid("")) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.SECURITY_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
    }

}