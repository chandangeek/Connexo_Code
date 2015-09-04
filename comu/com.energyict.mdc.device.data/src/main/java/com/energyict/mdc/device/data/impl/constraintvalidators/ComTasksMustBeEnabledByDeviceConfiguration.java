package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates the constraint that all {@link com.energyict.mdc.tasks.ComTask}s
 * of a {@link com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl}
 * should be enabled by the {@link com.energyict.mdc.device.data.Device}'s
 * {@link com.energyict.mdc.device.config.DeviceConfiguration configuration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-07 (09:51)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ComTasksMustBeEnabledByDeviceConfigurationValidator.class })
public @interface ComTasksMustBeEnabledByDeviceConfiguration {

    String message() default "{" + MessageSeeds.Keys.COMTASKS_MUST_BE_ENABLED_BY_CONFIGURATION + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
