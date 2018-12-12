/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test.rules;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExpectedConstraintViolation {

    /**
     * Default empty exception
     */
    static class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }

    String messageId() default "";

    String property() default "";

    /** Are other violations allowed in this assert? strict implies no other violations should be thrown */
    boolean strict() default true;
}
