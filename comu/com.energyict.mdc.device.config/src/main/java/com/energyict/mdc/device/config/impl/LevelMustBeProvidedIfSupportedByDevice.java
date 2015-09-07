package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 20/03/2014
 * Time: 13:47
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SecurityPropertySetImpl.LevelsAreSupportedValidator.class})
public @interface LevelMustBeProvidedIfSupportedByDevice {

    String message() default "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
