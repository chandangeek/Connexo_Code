package com.energyict.mdc.device.lifecycle.config.impl.constraints;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that the maximum past effective time shift of a
 * {@link DeviceLifeCycle} should not exceed the maximum allowed.
 * @see DeviceLifeCycleConfigurationService#getMaximumPastEffectiveTimeShift()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-24 (09:54)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { MaximumPastEffectiveTimeShiftInRangeValidator.class })
public @interface MaximumPastEffectiveTimeShiftInRange {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}