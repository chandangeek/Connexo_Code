package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Models the validation constraints that a {@link com.energyict.mdc.tasks.ComTask}
 * can only be enabled once per {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (09:49)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ComTaskIsEnabledOnlyOncePerConfigurationValidator.class })
public @interface ComTaskIsEnabledOnlyOncePerConfiguration {

    String message() default "{" + MessageSeeds.Keys.COM_TASK_CAN_ONLY_BE_ENABLED_ONCE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}