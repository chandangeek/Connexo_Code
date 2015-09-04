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
 * Time: 9:47
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PartialConnectionTaskImpl.HasSpecValidator.class})
public @interface PartialConnectionTaskPropertyMustHaveSpec {

    String message() default "{" + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
