package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that a {@link com.energyict.mdc.tasks.ComTask} cannot
 * be scheduled twice on the same {@link com.energyict.mdc.device.data.Device}.
 * It does not matter if the ComTask is manually scheduled or
 * scheduled via a {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-05 (15:35)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueComTaskSchedulingValidator.class })
public @interface UniqueComTaskScheduling {

    String message() default "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}