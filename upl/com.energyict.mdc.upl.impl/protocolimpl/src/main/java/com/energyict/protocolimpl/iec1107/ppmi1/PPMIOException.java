package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.IOException;

/**
 * Need better Exception handling. Not sure if this "constant and reason" thing
 * is the way to go for exceptions. Time will tell.
 * 
 * @author fbo
 */
public class PPMIOException extends IOException {

	private String	reason	= null;

	/**
	 * Creates a new instance of PPMIOException
	 * 
	 * @param reason
	 */
	public PPMIOException(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return this.reason;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		if (reason == null) {
			return super.getMessage();
		} else {
			return this.reason;
		}
	}

}
