package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Predicates;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all data in both load profiles and registers is collected.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles and registers against the effective timestamp of the transition.
 * A missing last reading on a load profile or register
 * equals to a mismatch with the effective timestamp
 * and will therefore fail this check.
 *
 *
 * check bits: 8
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class AllDataCollected implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public AllDataCollected(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        return this.evaluateLoadProfiles(device, effectiveTimestamp).andRegisters(device, effectiveTimestamp);
    }

    private Continuation evaluateLoadProfiles(Device device, Instant effectiveTimestamp) {
        if (anyLoadProfileWithoutLastReading(device, effectiveTimestamp).isPresent()) {
            return new ViolationFound(Optional.of(newViolation()));
        }
        return new EvaluateRegisters();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.ALL_DATA_COLLECTED,
                MicroCheck.ALL_DATA_COLLECTED);
    }

    private Optional<LoadProfile> anyLoadProfileWithoutLastReading(Device device, Instant effectiveTimestamp) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(Predicates
                        .either(this::lastReadingInLoadProfileMissing)
                        .or(lp -> this.lastReadingInLoadProfileNotEqualTo(lp, effectiveTimestamp)))
                .findAny();
    }

    private boolean lastReadingInLoadProfileMissing(LoadProfile loadProfile) {
        return !loadProfile.getLastReading().isPresent();
    }

    private boolean lastReadingInLoadProfileNotEqualTo(LoadProfile loadProfile, Instant effectiveTimestamp) {
        return !loadProfile.getLastReading().get().equals(effectiveTimestamp);
    }

    private boolean lastReadingInRegisterMissing(Register register) {
        return !register.getLastReading().isPresent();
    }

    private boolean lastReadingInRegisterNotEqualTo(Register<? extends Reading> register, Instant effectiveTimestamp) {
        return !register.getLastReading().get().getTimeStamp().equals(effectiveTimestamp);
    }

    private Optional<Register> anyRegisterWithoutLastReading(Device device, Instant effectiveTimestamp) {
        return device
                .getRegisters()
                .stream()
                .filter(Predicates
                        .either(this::lastReadingInRegisterMissing)
                        .or(r -> this.lastReadingInRegisterNotEqualTo(r, effectiveTimestamp)))
                .findAny();
    }

    private interface Continuation {
        /**
         * Continues the evaluation process with the registers of the specified Device.
         *
         * @param device The Device
         * @param effectiveTimestamp The effective timestamp of the transition
         * @return A violation if at least one register does not have a last reading timestamp
         */
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device, Instant effectiveTimestamp);

    }
    private class EvaluateRegisters implements Continuation {
        @Override
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device, Instant effectiveTimestamp) {
            if (anyRegisterWithoutLastReading(device, effectiveTimestamp).isPresent()) {
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
        public Optional<DeviceLifeCycleActionViolation> andRegisters(Device device, Instant effectiveTimestamp) {
            return this.violation;
        }
    }

}