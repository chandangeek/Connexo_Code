package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 21/03/2014
 * Time: 10:03
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PartialConnectionTaskImpl.ValueValidator.class})
public @interface PartialConnectionTaskPropertyValueHasCorrectType {

    String message() default "{" + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
