package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link com.energyict.mdc.tasks.ComTask} which are used by {@link com.energyict.mdc.scheduling.model.ComSchedule}
 * should be allowed for execution on {@link com.energyict.mdc.device.config.DeviceConfiguration}
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ComScheduleAllowedOnDeviceConfigurationValidator.class })
public @interface ComScheduleAllowedOnDeviceConfiguration {
    String message() default "{" + MessageSeeds.Keys.MISMATCH_COMTASK_SCHEDULE_WITH_DEVICE_CONFIGURATION + "}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}