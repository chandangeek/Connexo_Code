package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.util.OsgiLogLevelMapper;
import org.osgi.service.log.LogService;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

class LogHandler extends Handler {

	private final LogService logService;
	
	LogHandler(LogService logService) {
		this.logService = logService;
	}
	
	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord logRecord) {
        int osgiLevel = OsgiLogLevelMapper.mapLevel(logRecord.getLevel());
		logService.log(osgiLevel, logRecord.getMessage() , logRecord.getThrown());
	}

}
