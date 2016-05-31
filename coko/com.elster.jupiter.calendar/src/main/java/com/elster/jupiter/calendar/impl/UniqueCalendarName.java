package com.elster.jupiter.calendar.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueCalendarNameValidator.class, })
public @interface UniqueCalendarName {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {};
}
