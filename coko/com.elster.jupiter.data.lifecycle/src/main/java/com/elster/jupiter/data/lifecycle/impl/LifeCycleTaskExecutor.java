/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import java.util.logging.Logger;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;


public class LifeCycleTaskExecutor implements TaskExecutor {

	private final LifeCycleServiceImpl lifeCycleService; 
	
	LifeCycleTaskExecutor(LifeCycleServiceImpl service) {
		this.lifeCycleService = service;
		
	}
	
	public void execute(TaskOccurrence occurrence) {	
		Logger logger = Logger.getAnonymousLogger();
		logger.addHandler(occurrence.createTaskLogHandler().asHandler());
		lifeCycleService.execute(logger);
	}
}
