package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that the {@link com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass}
 * of a {@link com.energyict.mdc.device.config.DeviceType} cannot change
 * when the DeviceType already has {@link com.energyict.mdc.device.config.DeviceConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-03 (13:54)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ProtocolCannotChangeWithExistingConfigurationsValidator.class })
public @interface ProtocolCannotChangeWithExistingConfigurations {

    String message() default "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}