package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraints that a {@link com.energyict.mdc.device.config.ComTaskEnablement}
 * cannot specify to use the default connection task and specify a
 * {@link com.energyict.mdc.device.config.PartialConnectionTask} at the same time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (09:49)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NoPartialConnectionTaskWhenDefaultIsUsedValidator.class })
public @interface NoPartialConnectionTaskWhenDefaultIsUsed {

    String message() default "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_CANNOT_USE_DEFAULT_AND_PARTIAL_CONNECTION_TASK + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}