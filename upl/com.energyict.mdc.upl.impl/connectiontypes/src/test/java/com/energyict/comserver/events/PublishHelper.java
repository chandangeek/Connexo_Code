package com.energyict.comserver.events;

import java.io.ByteArrayOutputStream;

import com.energyict.mdc.protocol.AbstractComChannel;

/**
 * This is a commserver piece of code that is sneakily called from mdw via reflection (doing it in a straightforward way would cause a cyclic dependency). This being said, it should have
 * been a plugin interface, and not called via reflection. This piece of code stubs it out, so the build log isn't spammed with e.printStackTraces.
 * 
 * @author alex
 *
 */
public final class PublishHelper {

	/** Prevent instantiation. */
	private PublishHelper() {
	}
	
	/**
	 * Does nothing except preventing the log to be spammed with NoClassDefs.
	 * 
	 * @param 		channel		The channel.
	 * @param 		data		The data.
	 */
	public static final void publishBytesWritten(final AbstractComChannel channel, final ByteArrayOutputStream data) {
	}
	
	/**
	 * Does nothing except preventing loads of stacktraces when running the tests.
	 * 
	 * @param 	channel			The channel.
	 * @param 	data			The data.
	 */
	public static final void publishBytesRead(final AbstractComChannel channel, final ByteArrayOutputStream data) {
	}
}
