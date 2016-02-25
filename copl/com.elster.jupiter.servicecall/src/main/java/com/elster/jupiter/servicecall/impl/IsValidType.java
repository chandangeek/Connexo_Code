package com.elster.jupiter.servicecall.impl;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by bvn on 2/25/16.
 */
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = TypeValidator.class)
@Documented
public @interface IsValidType {
    String message() default "{com.elster.jupiter.servicecall.invalidType}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
