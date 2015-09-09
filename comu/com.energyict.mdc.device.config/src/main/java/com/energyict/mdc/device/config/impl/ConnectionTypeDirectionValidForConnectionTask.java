package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 6/17/14
 * Time: 10:31 AM
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ConnectionTaskTypeDirectionValidator.class})
public @interface ConnectionTypeDirectionValidForConnectionTask {

    String message() default '{' + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + '}';

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ConnectionType.Direction direction();

}