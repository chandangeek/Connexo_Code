/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class OverruledAttributesValidator implements ConstraintValidator<ValidOverruledAttributes, Device> {
    @Override
    public void initialize(ValidOverruledAttributes validOverruledAttributes) {

    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext context) {
        boolean valid = validateDuplicateRegisters(device, context);
        valid &= validateDuplicateChannels(device, context);
        valid &= validateRegisterOverflowIncrease(device, context);
        valid &= validateChannelOverflowIncrease(device, context);
        return valid;
    }

    private boolean validateChannelOverflowIncrease(Device device, ConstraintValidatorContext context) {
        Set<LoadProfile> overflowIncreased = new HashSet<>();
        device.getLoadProfiles().stream().forEach(loadProfile -> {
            loadProfile.getChannels().stream().forEach(channel -> {
                if (channel.getOverflow().isPresent() && channel.getChannelSpec().getOverflow().isPresent()) {
                    if (channel.getOverflow().get().compareTo(channel.getChannelSpec().getOverflow().get()) > 0) {
                        overflowIncreased.add(loadProfile);
                        context.disableDefaultConstraintViolation();
                        context.
                                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.OVERFLOW_INCREASED + "}").
                                addPropertyNode("overruledOverflowValue").
                                addConstraintViolation();
                    }
                }
            });
        });
        return overflowIncreased.size() == 0;
    }

    private boolean validateRegisterOverflowIncrease(Device device, ConstraintValidatorContext context) {
        Set<Register> overflowIncrease = new HashSet<>();
        device.getRegisters().stream().filter(register -> register instanceof NumericalRegister).map(register3 -> ((NumericalRegister) register3))
                .forEach(numericalRegister -> {
                    if (numericalRegister.getOverflow().isPresent() && numericalRegister.getRegisterSpec().getOverflowValue().isPresent()) {
                        if (numericalRegister.getOverflow().get().compareTo(numericalRegister.getRegisterSpec().getOverflowValue().get()) > 0) {
                            overflowIncrease.add(numericalRegister);
                            context.disableDefaultConstraintViolation();
                            context.
                                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.OVERFLOW_INCREASED + "}").
                                    addPropertyNode("overruledOverflow").
                                    addConstraintViolation();
                        }
                    }
                });
        return overflowIncrease.size() == 0;
    }


    private boolean validateDuplicateChannels(Device device, ConstraintValidatorContext context) {
        Set<LoadProfile> loadProfileWithDuplicates = new HashSet<>();
        device.getLoadProfiles().stream().forEach(loadProfile -> {
            Set<ObisCode> uniqueObisCodes = new HashSet<>();

            loadProfile.getChannels().stream().filter(channel -> !uniqueObisCodes.add(channel.getObisCode())).findAny()
                    .ifPresent(channel1 -> {
                        loadProfileWithDuplicates.add(loadProfile);
                        context.disableDefaultConstraintViolation();
                        context.
                                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DUPLICATE_CHANNEL_OBISCODE + "}").
                                addPropertyNode("overruledObisCode").
                                addConstraintViolation();
                    });
        });
        return loadProfileWithDuplicates.size() == 0;
    }

    private boolean validateDuplicateRegisters(Device device, ConstraintValidatorContext context) {
        Set<ObisCode> uniqueObisCodes = new HashSet<>();
        device.getRegisters().stream().filter(register1 -> !uniqueObisCodes.add(register1.getDeviceObisCode())).findAny()
                .ifPresent(register2 -> {
                    context.disableDefaultConstraintViolation();
                    context.
                            buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DUPLICATE_REGISTER_OBISCODE + "}").
                            addPropertyNode("overruledObisCode").
                            addConstraintViolation();
                });
        return uniqueObisCodes.size() == device.getRegisters().size();
    }
}
