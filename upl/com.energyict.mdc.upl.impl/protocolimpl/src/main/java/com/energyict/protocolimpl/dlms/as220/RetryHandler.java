package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class RetryHandler {

	private static final int	DEFAULT_RETRIES	= 3;

	private int maxRetries;
	private int nrOfRetries;

	public RetryHandler(int maxRetries) {
		this.maxRetries = maxRetries;
		this.nrOfRetries = 0;
	}

	public RetryHandler() {
		this(DEFAULT_RETRIES);
	}

	public void reset() {
		reset(maxRetries);
	}

	public void reset(int maxRetries) {
		this.maxRetries = maxRetries;
		nrOfRetries = 0;
	}

	public void logFailure() throws IOException {
		nrOfRetries++;
		if (nrOfRetries >= maxRetries) {
			throw new IOException("Exceeded maximum number of retries: [" + nrOfRetries + "/" + maxRetries + "]");
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(nrOfRetries).append("/").append(maxRetries);
		sb.append("]");
		return sb.toString();
	}

}
