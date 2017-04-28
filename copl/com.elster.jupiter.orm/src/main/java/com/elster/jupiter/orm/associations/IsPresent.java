/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { FIELD , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {IsPresentReferenceValidator.class, IsPresentOptionalValidator.class, IsPresentTemporalReferenceValidator.class, IsPresentRefAnyValidator.class})
@Documented
public @interface IsPresent {

    String message() default "{com.elster.jupiter.orm.associations.isPresent}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}