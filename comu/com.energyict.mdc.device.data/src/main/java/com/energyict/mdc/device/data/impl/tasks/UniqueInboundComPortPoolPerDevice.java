package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the contraint that there can be only 1
 * {@link com.energyict.mdc.engine.config.InboundComPortPool}
 * assigned to a Device at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (10:59)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { UniqueInboundComPortPoolPerDeviceValidator.class })
public @interface UniqueInboundComPortPoolPerDevice {

    String message() default "{" + MessageSeeds.Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}