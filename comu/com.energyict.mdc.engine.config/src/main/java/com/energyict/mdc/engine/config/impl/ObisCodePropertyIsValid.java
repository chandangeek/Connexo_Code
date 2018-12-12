package com.energyict.mdc.engine.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ComportPoolPropertyObisCodeValid.class})
public @interface ObisCodePropertyIsValid {
    String message() default "{" + MessageSeeds.Keys.COM_PORT_POOL_PROPERTY_OBIS_INVALID + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
