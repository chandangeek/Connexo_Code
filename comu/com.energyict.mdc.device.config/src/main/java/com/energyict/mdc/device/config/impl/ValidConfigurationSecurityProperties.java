/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that all {@link ConfigurationSecurityProperty ConfigurationSecurityProperties}
 * that are defined against a {@link SecurityPropertySet} are valid.
 *
 * @author Stijn Vanhoorelbeke
 * @since 26.04.17 - 10:15
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidConfigurationSecurityPropertiesValidator.class })
public @interface ValidConfigurationSecurityProperties {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}