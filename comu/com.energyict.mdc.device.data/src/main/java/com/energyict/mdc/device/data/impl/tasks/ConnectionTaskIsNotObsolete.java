package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

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
@Constraint(validatedBy = { ConnectionTypeIsNotObsoleteValidator.class })
public @interface ConnectionTaskIsNotObsolete {

    String message() default "{" + MessageSeeds.Keys.CONNECTION_TASK_IS_OBSOLETE_AND_CANNOT_UPDATE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}