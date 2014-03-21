package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 21/03/2014
 * Time: 14:29
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NextExecutionSpecsImpl.OffsetVsFrequencyValidator.class })
public @interface OffsetNotGreaterThanFrequency {

    String message() default "{" + MessageSeeds.Constants.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
