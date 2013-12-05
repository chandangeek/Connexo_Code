package com.energyict.protocolimpl.messaging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a composite Rtu message description annotation. It allows the message definition to be used multiple times.
 *
 * @author alex
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RtuMessageDescriptions {

	/**
	 * Returns the message descriptions.
	 *
	 * @return		The message descriptions.
	 */
	public RtuMessageDescription[] value();
}
