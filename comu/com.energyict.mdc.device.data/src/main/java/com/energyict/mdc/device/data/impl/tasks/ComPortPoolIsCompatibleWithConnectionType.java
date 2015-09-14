package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the contraint that the type of the {@link com.energyict.mdc.engine.config.ComPort}s
 * that are contained in the {@link com.energyict.mdc.engine.config.ComPortPool}
 * of a {@link com.energyict.mdc.device.data.tasks.ConnectionTask} is supported
 * by the ConnectionTask's {@link com.energyict.mdc.protocol.api.ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:12)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ComPortPoolIsCompatibleWithConnectionTypeValidator.class })
public @interface ComPortPoolIsCompatibleWithConnectionType {

    String message() default "{" + MessageSeeds.Keys.COMPORT_TYPE_NOT_SUPPORTED + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}