package com.energyict.protocolimpl.messaging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:18 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RtuMessageAttribute {

    /**
     * This is the name of the attribute used to build the actual message. Normally this tag name should follow the
     * same rules as these for an xml attribute tag (no spaces, no special characters like '<', '>', '"', ...
     * This tag name is visible in EIServer as the attribute name, so it should be as clear as possible for the user what it does.
     *
     * @return The tag name
     */
    String tag();

    /**
     * Mark the attribute as required or optional
     * In case of a missing optional value, the {@link RtuMessageAttribute#defaultValue()} value is used
     *
     * @return True if required, false if optional
     */
    boolean required() default false;

    /**
     * If the field is not {@link RtuMessageAttribute#required()} and there was no value given by the user, this default value is used.
     *
     * @return The default value for optional fields ("" by default)
     */
    String defaultValue() default "";

}
