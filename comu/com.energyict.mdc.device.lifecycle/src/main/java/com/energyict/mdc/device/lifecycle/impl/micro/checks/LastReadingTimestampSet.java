package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a last reading timestamp is set on all load profiles and registers of a Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class LastReadingTimestampSet implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public LastReadingTimestampSet(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device) {
        return this.evaluateLoadProfiles(device).andRegisters(device);
    }

    private Continuation evaluateLoadProfiles(Device device) {
        if (anyLoadProfileWithoutLastReading(device).isPresent()) {
            return new ViolationFound(Optional.of(newViolation()));
        }
        return new EvaluateRegisters();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.LAST_READING_TIMESTAMP_SET,
                MicroCheck.LAST_READING_TIMESTAMP_SET);
    }

    private Optional<LoadProfile> anyLoadProfileWithoutLastReading(Device device) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(each -> !each.getLastReading().isPresent())
                .findAny();
    }

    private Optional<Register> anyRegisterWithoutLastReading(Device device) {
        return device
                .getRegisters()
                .stream()
                .filter(each -> !each.getLastReading().isPresent())
                .findAny();
    }

    private interface Continuation {
        /**
         * Continues the evaluation process with the registers of the specified Device.
         *
         * @param device The Device
         * @return A violation if at least one register does not have a last reading timestamp
         */
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device);

    }
    private class EvaluateRegisters implements Continuation {
        @Override
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device) {
            if (anyRegisterWithoutLastReading(device).isPresent()) {
                return Optional.of(newViolation());
            }
            else {
                return Optional.empty();
            }
        }

    }

    /**
     * Provides an implementation for the Continuation interface
     * that will return the Violation that was already found earlier
     * and will therefore shortcut the remainder of the
     * evaluation process.
     */
    private class ViolationFound implements Continuation {
        private final Optional<DeviceLifeCycleActionViolation> violation;

        private ViolationFound(Optional<DeviceLifeCycleActionViolation> violation) {
            super();
            this.violation = violation;
        }

        @Override
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device) {
            return this.violation;
        }
    }

}