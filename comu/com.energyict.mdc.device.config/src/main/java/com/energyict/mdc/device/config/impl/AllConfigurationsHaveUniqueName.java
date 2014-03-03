package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that all {@link com.energyict.mdc.device.config.DeviceConfiguration}s
 * in a {@link com.energyict.mdc.device.config.DeviceType} must have a unique name.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-28 (11:37)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { AllConfigurationsHaveUniqueNameValidator.class })
public @interface AllConfigurationsHaveUniqueName {

    String message() default "{" + MessageSeeds.Constants.DUPLICATE_DEVICE_CONFIGURATION_KEY + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}