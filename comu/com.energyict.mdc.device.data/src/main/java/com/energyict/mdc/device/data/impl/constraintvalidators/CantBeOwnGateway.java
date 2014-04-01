package com.energyict.mdc.device.data.impl.constraintvalidators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint which indicates that a Device can not be his own gateway
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:56 PM
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {PhysicalGatewayNotSameAsOriginValidator.class, CommunicationGatewayNotSameAsOriginValidator.class})
public @interface CantBeOwnGateway {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
