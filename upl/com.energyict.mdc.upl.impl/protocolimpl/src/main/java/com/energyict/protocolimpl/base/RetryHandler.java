package com.energyict.protocolimpl.base;

import java.io.IOException;

import com.energyict.cbo.NestedIOException;

/**
 * @author jme
 *
 */
public class RetryHandler {

	private static final int	DEFAULT_RETRIES	= 3;

	private int maxRetries;
	private int nrOfRetries;

	public RetryHandler(int maxRetries) {
		setMaxRetries(maxRetries);
		this.nrOfRetries = 0;
	}

	public RetryHandler() {
		this(DEFAULT_RETRIES);
	}

	public void reset() {
		reset(maxRetries);
	}

	public void reset(int maxRetries) {
		setMaxRetries(maxRetries);
		nrOfRetries = 0;
	}

	private void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void logFailure(Exception e, String message) throws IOException {
		nrOfRetries++;
		if (nrOfRetries >= maxRetries) {
			StringBuffer sb = new StringBuffer();
			sb.append("Exceeded maximum number of retries: [");
			sb.append(nrOfRetries);
			sb.append("/");
			sb.append(maxRetries);
			sb.append("] ");
			sb.append(message == null ? "" : message);
			throw (e == null) ? new IOException(sb.toString()) : new NestedIOException(e, sb.toString());
		}
	}

	public void logFailure(String message) throws IOException {
		logFailure(null, message);
	}

	public void logFailure(Exception e) throws IOException {
		logFailure(e, null);
	}

	public void logFailure() throws IOException {
		logFailure(null, null);
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
