package com.elster.jupiter.export.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { SandboxValidator.class })
public @interface Sandboxed {

    String message() default "{"+ MessageSeeds.Keys.PARENT_BREAKING_PATH +"}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
