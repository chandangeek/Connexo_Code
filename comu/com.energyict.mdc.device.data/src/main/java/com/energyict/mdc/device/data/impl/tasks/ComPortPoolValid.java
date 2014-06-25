package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the contraint that the type of the {@link com.energyict.mdc.engine.model.ComPort}s
 * that are contained in the {@link com.energyict.mdc.engine.model.ComPortPool}
 * of a {@link ConnectionMethod} is supported by the ConnectionMethod's {@link com.energyict.mdc.protocol.api.ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:12)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ComPortPoolValidator.class })
public @interface ComPortPoolValid {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}