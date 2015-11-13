package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the contraint that the target of this annotation is not obsolete.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:12)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ConnectionTaskIsNotObsoleteValidator.class })
public @interface NotObsolete {

    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}