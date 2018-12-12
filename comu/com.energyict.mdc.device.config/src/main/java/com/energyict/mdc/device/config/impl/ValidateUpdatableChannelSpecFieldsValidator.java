/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class ValidateUpdatableChannelSpecFieldsValidator implements ConstraintValidator<ValidateUpdatableChannelSpecFields, ChannelSpecImpl> {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ValidateUpdatableChannelSpecFieldsValidator(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ValidateUpdatableChannelSpecFields validateUpdatableChannelSpecFields) {

    }

    @Override
    public boolean isValid(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (channelSpec.getDeviceConfiguration().isActive()) {
            Optional<ChannelSpec> oldChannelSpecOptional = deviceConfigurationService.findChannelSpec(channelSpec.getId());
            if (oldChannelSpecOptional.isPresent()) {
                ChannelSpec oldChannelSpec = oldChannelSpecOptional.get();
                if (validateSameUsageOfMultiplier(channelSpec, constraintValidatorContext, oldChannelSpec)) {
                    return false;
                }
                if (validateOverFlowAndFractionDigitsUpdate(channelSpec, constraintValidatorContext, oldChannelSpec)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateOverFlowAndFractionDigitsUpdate(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext, ChannelSpec oldChannelSpec) {
        if (oldChannelSpec.getOverflow().isPresent() && channelSpec.getOverflow().isPresent() &&
                oldChannelSpec.getOverflow().get().compareTo(channelSpec.getOverflow().get()) > 0) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CHANNEL_SPEC_OVERFLOW_DECREASED + "}").
                    addPropertyNode(ChannelSpecImpl.ChannelSpecFields.OVERFLOW_VALUE.fieldName()).
                    addConstraintViolation();
            return true;
        }
        if (oldChannelSpec.getNbrOfFractionDigits() > channelSpec.getNbrOfFractionDigits()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CHANNEL_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED + "}").
                    addPropertyNode(ChannelSpecImpl.ChannelSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).
                    addConstraintViolation();
            return true;
        }
        return false;
    }


    private boolean validateSameUsageOfMultiplier(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext, ChannelSpec oldChannelSpec) {
        if (oldChannelSpec.isUseMultiplier() == channelSpec.isUseMultiplier()) {
            if (validateSameCalculatedMultipliedReadingType(channelSpec, constraintValidatorContext, oldChannelSpec)) {
                return true;
            }
        } else {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG + "}")
                    .addPropertyNode(ChannelSpecImpl.ChannelSpecFields.USEMULTIPLIER.fieldName())
                    .addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean validateSameCalculatedMultipliedReadingType(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext, ChannelSpec oldChannelSpec) {
        if (channelSpec.isUseMultiplier()) {
            if (oldChannelSpec.getCalculatedReadingType().isPresent() && channelSpec.getCalculatedReadingType().isPresent()) { // just making sure both are there
                if (!oldChannelSpec.getCalculatedReadingType().get().equals(channelSpec.getCalculatedReadingType().get())) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG + "}")
                            .addPropertyNode(ChannelSpecImpl.ChannelSpecFields.CALCULATED_READINGTYPE.fieldName())
                            .addConstraintViolation();
                    return true;
                }
            }
        }
        return false;
    }
}
