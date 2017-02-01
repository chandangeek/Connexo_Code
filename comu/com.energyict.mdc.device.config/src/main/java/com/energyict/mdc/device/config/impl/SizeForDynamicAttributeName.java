/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Same as the {@link javax.validation.constraints.Size} constraint
 * but targetted specifically at {@link ProtocolDialectConfigurationPropertyImpl}
 * and will produce a validation error that picks up the attribute name from the property.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-20 (15:19)
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { SizeForDynamicAttributeNameValidator.class })
public @interface SizeForDynamicAttributeName {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return size the element must be lower or equal to
     */
    int max() default Integer.MAX_VALUE;

}