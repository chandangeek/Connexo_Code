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
 * Time: 15:11
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PartialScheduledConnectionTaskImpl.MinimumRescheduleDelayValidator.class})
public @interface CheckMinimumRescheduleDelay {

    String message() default '{' + MessageSeeds.Constants.UNDER_MINIMUM_RESCHEDULE_DELAY_KEY + '}';

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int minimumRescheduleDelayInSeconds() default 0;
}
