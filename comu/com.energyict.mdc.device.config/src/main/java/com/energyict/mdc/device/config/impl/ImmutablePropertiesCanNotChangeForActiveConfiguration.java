package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

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
