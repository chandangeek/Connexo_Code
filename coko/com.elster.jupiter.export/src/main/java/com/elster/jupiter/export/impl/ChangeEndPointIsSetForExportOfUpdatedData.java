/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ChangeEndPointIsSetForExportOfUpdatedDataValidator.class})
public @interface ChangeEndPointIsSetForExportOfUpdatedData {
    String message() default "{" + MessageSeeds.Keys.NO_CHANGE_ENDPOINT_FOR_UPDATED_DATA + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
