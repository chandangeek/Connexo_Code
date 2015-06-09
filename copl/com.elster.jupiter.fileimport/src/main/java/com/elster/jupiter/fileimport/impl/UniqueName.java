package com.elster.jupiter.fileimport.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {UniqueImportScheduleNameValidator.class})
public @interface UniqueName {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}