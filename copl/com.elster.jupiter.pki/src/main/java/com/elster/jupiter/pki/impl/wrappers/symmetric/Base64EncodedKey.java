/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.pki.impl.wrappers.Base64Validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint( validatedBy = {Base64Validator.class} )
public @interface Base64EncodedKey {

    String message() default "{com.elster.jupiter.validation.base64}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
