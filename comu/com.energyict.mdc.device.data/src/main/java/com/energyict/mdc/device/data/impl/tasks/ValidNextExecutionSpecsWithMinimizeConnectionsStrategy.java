package com.energyict.mdc.device.data.impl.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Models the contraint that a {@link ScheduledConnectionTaskImpl}
 * must have {@link com.energyict.mdc.scheduling.NextExecutionSpecs}
 * when the {@link com.energyict.mdc.device.config.ConnectionStrategy}
 * is {@link com.energyict.mdc.device.config.ConnectionStrategy#MINIMIZE_CONNECTIONS minimize connections}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (14:47)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NextExecutionSpecsWithMinimizeConnectionsStrategyValidator.class })
public @interface ValidNextExecutionSpecsWithMinimizeConnectionsStrategy {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}