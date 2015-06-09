package com.elster.jupiter.orm.associations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target( { FIELD , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {IsPresentReferenceValidator.class, IsPresentOptionalValidator.class, IsPresentTemporalReferenceValidator.class})
@Documented
public @interface IsPresent {

    String message() default "{com.elster.jupiter.orm.associations.isPresent}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}