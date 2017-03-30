/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PartialScheduledConnectionTaskImpl.NextExecutionSpecVsComWindowValidator.class})
public @interface NextExecutionSpecsValidForComWindow {

    String message() default '{' + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW + '}';

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
