package com.energyict.mdc.firmware.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that all DeviceMessageAttributes
 * that are defined against a DeviceMessage
 * are valid with the property specifications of the DeviceMessageSpec.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (17:30)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { FirmwareCampaignsAttributesValidator.class })
public @interface HasValidFirmwareCampaignAttributes {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
