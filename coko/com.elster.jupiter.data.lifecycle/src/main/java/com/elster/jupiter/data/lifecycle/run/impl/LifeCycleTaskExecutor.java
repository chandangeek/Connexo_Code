package com.elster.jupiter.data.lifecycle.run.impl;

import java.time.Instant;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Handler;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;


public class LifeCycleTaskExecutor implements TaskExecutor {

	private final LifeCycleService lifeCycleService; 
	
	LifeCycleTaskExecutor(LifeCycleService service) {
		this.lifeCycleService = service;
		
	}
	public void execute(TaskOccurrence occurrence) {	
		Logger logger = Logger.getAnonymousLogger();
		logger.addHandler(new LogHandler(occurrence));
		lifeCycleService.execute(logger);
	}
	
	private static class LogHandler extends Handler {
		
		private final TaskOccurrence occurrence;
		
		LogHandler(TaskOccurrence occurrence) {
			this.occurrence = occurrence;
		}

		@Override
		public void publish(LogRecord record) {
			occurrence.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
		
	}

}
