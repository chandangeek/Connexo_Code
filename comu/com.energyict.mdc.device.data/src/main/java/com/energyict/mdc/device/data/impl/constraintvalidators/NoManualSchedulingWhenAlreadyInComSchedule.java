package com.energyict.mdc.device.data.impl.constraintvalidators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that a {@link com.energyict.mdc.tasks.ComTask} cannot
 * be scheduled manually when it is already scheduled via a {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-02 (16:47)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { NoManualSchedulingWhenAlreadyInComScheduleValidator.class })
public @interface NoManualSchedulingWhenAlreadyInComSchedule {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}