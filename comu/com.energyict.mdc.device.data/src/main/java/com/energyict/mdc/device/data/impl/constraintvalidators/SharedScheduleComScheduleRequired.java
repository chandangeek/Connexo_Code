package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Copyrights EnergyICT
 * Date: 18/11/16
 * Time: 13:16
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {SharedScheduleComScheduleRequiredValidator.class})
public @interface SharedScheduleComScheduleRequired {

    String message() default "{" + MessageSeeds.Keys.COMSCHEDULE_IS_REQUIRED + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}