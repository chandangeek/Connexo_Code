package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 8:39
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ProtocolDialectConfigurationPropertiesImpl.PrimaryKeyValidator .class })
public @interface ProtocolDialectConfigurationPropertiesCannotDuplicate {

    String message() default "{" + MessageSeeds.Keys.PROTOCOLDIALECT_CONF_PROPS_DUPLICATE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
