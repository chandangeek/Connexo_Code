/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.common.tasks.TaskServiceKeys;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {UniqueComTaskNameValidator.class})
public @interface UniqueName {

    String message() default "{" + TaskServiceKeys.SIZE_TOO_LONG + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
