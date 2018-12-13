/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.pki.impl.wrappers.KeySizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint( validatedBy = {KeySizeValidator.class} )
public @interface KeySize {

    String message() default "{com.elster.jupiter.validation.keysize}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
