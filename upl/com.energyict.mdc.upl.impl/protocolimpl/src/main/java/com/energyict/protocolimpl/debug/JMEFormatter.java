package com.energyict.protocolimpl.debug;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author jme
 *
 */
public class JMEFormatter extends Formatter {

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord logRecord) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(logRecord.getMillis()).append("] ");
		sb.append("[").append(logRecord.getLevel().getName()).append("] ");
		sb.append(logRecord.getMessage()).append("\r\n");
		return sb.toString();
	}

}
