package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all collected data in both load profiles and registers is valid.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles and registers against the last checked.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (11:24)
 */
public class AllDataValid implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public AllDataValid(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        return this.evaluateLoadProfiles(device).andRegisters(device);
    }

    private Continuation evaluateLoadProfiles(Device device) {
        if (anyLoadProfileWithUnvalidatedData(device).isPresent()) {
            return new ViolationFound(Optional.of(newViolation()));
        }
        return new EvaluateRegisters();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.ALL_DATA_VALID,
                MicroCheck.ALL_DATA_VALID);
    }

    private Optional<LoadProfile> anyLoadProfileWithUnvalidatedData(Device device) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(this::loadProfileContainsUnvalidatedData)
                .findAny();
    }

    private boolean loadProfileContainsUnvalidatedData(LoadProfile loadProfile) {
        if (loadProfile.getLastReading().isPresent()) {
            DeviceValidation deviceValidation = loadProfile.getDevice().forValidation();
            return loadProfile
                    .getChannels()
                    .stream()
                    .filter(channel -> !deviceValidation.allDataValidated(channel, loadProfile.getLastReading().get()))
                    .findAny()
                    .isPresent();
        }
        else {
            return false;
        }
    }

    private Optional<Register> anyRegisterWithUnvalidatedData(Device device) {
        return device
                .getRegisters()
                .stream()
                .filter(this::registerContainsUnvalidatedData)
                .findAny();
    }

    private boolean registerContainsUnvalidatedData(Register<? extends Reading> register) {
        if (register.getLastReading().isPresent()) {
            return !register
                    .getDevice()
                    .forValidation()
                    .allDataValidated(register, register.getLastReading().get().getTimeStamp());
        }
        else {
            return false;
        }
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
            if (anyRegisterWithUnvalidatedData(device).isPresent()) {
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