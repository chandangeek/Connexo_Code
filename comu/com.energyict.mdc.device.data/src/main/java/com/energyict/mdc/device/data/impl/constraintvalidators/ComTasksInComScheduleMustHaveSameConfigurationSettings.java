package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that all {@link com.energyict.mdc.tasks.ComTask}s
 * of a {@link com.energyict.mdc.scheduling.model.ComSchedule}
 * need to have the same configuration settings when scheduled
 * on a single device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-05 (16:56)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ComTasksInComScheduleMustHaveSameConfigurationSettingsValidator.class })
public @interface ComTasksInComScheduleMustHaveSameConfigurationSettings {

    String message() default "{" + MessageSeeds.Keys.COMTASK_CONFIGURATION_INCONSISTENT + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}