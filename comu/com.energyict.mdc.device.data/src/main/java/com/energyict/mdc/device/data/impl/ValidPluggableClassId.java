package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.HasValidPropertiesValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Models the constraint that the {@link ProtocolDialectProperties}
 * that are defined against a {@link com.energyict.mdc.device.data.Device}
 * are valid with the property specifications of the {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-07 (13:53)
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidPluggableClassIdValidator.class })
public @interface ValidPluggableClassId {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}