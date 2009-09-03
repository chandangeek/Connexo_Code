/*
 * PPMIOException.java
 *
 * Created on 26 november 2004, 9:48
 */

package com.energyict.protocolimpl.iec1107.ppm;

import java.io.IOException;

/**
 * Need better Exception handling. Not sure if this "constant and reason" thingy
 * is the way to go for exceptions. Time will tell.
 * 
 * @author fbo
 */
public class PPMIOException extends IOException {

	private String reason = null;

	/** Creates a new instance of PPMIOException */
	public PPMIOException(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return this.reason;
	}

	public String getMessage() {
		if (this.reason == null) {
			return super.getMessage();
		} else {
			return this.reason;
		}
	}

}
