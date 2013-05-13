package com.elster.jupiter.pubsub.impl;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.service.log.LogService;

public class LogHandler extends Handler {

	private final LogService logService;
	
	LogHandler(LogService logService) {
		this.logService = logService;
	}
	
	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord logRecord) {
		int osgiLevel = mapLevel(logRecord.getLevel().intValue());
		logService.log(osgiLevel, logRecord.getMessage() , logRecord.getThrown());
	}

	private int mapLevel(int level) {
		if (level <= Level.FINE.intValue()) {
			return LogService.LOG_DEBUG;
		} else if (level <= Level.INFO.intValue()) {
			return LogService.LOG_INFO;
		} else if (level <= Level.WARNING.intValue()) {
			return LogService.LOG_WARNING;
		} else {
			return LogService.LOG_ERROR;		
		}
	}
}
