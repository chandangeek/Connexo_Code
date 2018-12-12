/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that all {@link com.energyict.mdc.device.data.tasks.ConnectionTaskProperty ConnectionTaskProperties}
 * that are defined against a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * are valid with the property specifications of the {@link com.energyict.mdc.protocol.api.ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (17:30)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasValidPropertiesValidator.class })
public @interface HasValidProperties {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}