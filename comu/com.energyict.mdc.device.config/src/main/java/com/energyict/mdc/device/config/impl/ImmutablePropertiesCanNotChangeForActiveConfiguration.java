package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ImmutablePropertiesCanNotChangeForActiveConfigurationValidator.class })
public @interface ImmutablePropertiesCanNotChangeForActiveConfiguration {

	String message() default "{"+ MessageSeeds.Keys.DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE+ "}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default { };
}
