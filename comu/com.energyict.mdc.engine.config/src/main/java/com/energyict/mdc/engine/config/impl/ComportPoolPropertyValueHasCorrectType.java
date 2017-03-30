package com.energyict.mdc.engine.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ComportPoolPropertyValueValidator.class})
public @interface ComportPoolPropertyValueHasCorrectType {
    String message() default "{" + MessageSeeds.Keys.COM_PORT_PPOOL_PROPERTY_VALUE_OF_WRONG_TYPE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
