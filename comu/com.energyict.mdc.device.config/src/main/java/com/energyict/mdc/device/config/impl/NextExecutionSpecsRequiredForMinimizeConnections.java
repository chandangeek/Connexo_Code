package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 21/03/2014
 * Time: 10:48
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PartialScheduledConnectionTaskImpl.NextExecutionSpecValidator.class})
public @interface NextExecutionSpecsRequiredForMinimizeConnections {

    String message() default '{' + MessageSeeds.Constants.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS_KEY + '}';

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
