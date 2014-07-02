package com.energyict.mdc.scheduling.model.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Copyrights EnergyICT
 * Date: 21/03/2014
 * Time: 14:29
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OffsetVsFrequencyValidator.class })
public @interface OffsetNotGreaterThanFrequency {

    String message() default "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
