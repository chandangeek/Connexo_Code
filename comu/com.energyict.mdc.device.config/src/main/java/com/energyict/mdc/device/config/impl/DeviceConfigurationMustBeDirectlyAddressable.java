package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that {@link PartialConnectionTaskImpl}s
 * cannot be created against a {@link DeviceConfigurationImpl}
 * if the latter is not directly addressable.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-13 (08:39)
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DeviceConfigurationMustBeDirectlyAddressableValidator.class})
public @interface DeviceConfigurationMustBeDirectlyAddressable {

    String message() default '{' + MessageSeeds.Keys.DEVICE_CONFIGURATION_IS_NOT_DIRECTLY_ADDRESSABLE + '}';

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}