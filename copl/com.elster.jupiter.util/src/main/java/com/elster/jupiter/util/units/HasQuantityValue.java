package com.elster.jupiter.util.units;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { HasQuantityValueValidator.class })
public @interface HasQuantityValue {

    long min();

    long max();

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {

    };

}
