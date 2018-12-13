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
@Constraint(validatedBy = {SecurityPropertySetImpl.ClientValidator.class})
public @interface ValidClient {

    String message() default "{" + MessageSeeds.Keys.INVALID_VALUE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
