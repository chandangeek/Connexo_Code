package com.elster.jupiter.data.lifecycle;

import java.util.List;
import java.util.logging.Logger;

import com.elster.jupiter.tasks.RecurrentTask;

public interface LifeCycleService {

	public List<LifeCycleCategory> getCategories();
	
	public RecurrentTask getTask();
	
	public void execute(Logger logger);
	
}
