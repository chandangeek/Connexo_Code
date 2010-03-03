package com.energyict.protocolimpl.debug;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.energyict.protocolimpl.utils.ProtocolTools;

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
		String level = "[" + logRecord.getLevel().getName() + "]";
		sb.append(ProtocolTools.addPadding(level, ' ', 10, true));
		sb.append(logRecord.getMessage()).append("\r\n");
		return sb.toString();
	}

}
