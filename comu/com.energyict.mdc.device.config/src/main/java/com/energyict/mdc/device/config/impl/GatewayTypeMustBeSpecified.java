package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { GatewayTypeMustBeSpecifiedValidator.class })
public @interface GatewayTypeMustBeSpecified {

    String message() default "{"+ MessageSeeds.Keys.INCORRECT_GATEWAY_TYPE+ "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };
}
