package com.energyict.protocolimpl.messaging;

import java.lang.annotation.*;

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
