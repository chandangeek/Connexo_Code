/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OffsetVsFrequencyValidator.class })
public @interface OffsetNotGreaterThanFrequency {

    String message() default "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
