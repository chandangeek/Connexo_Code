package com.elster.jupiter.data.lifecycle;

import java.time.Instant;
import java.util.List;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

public interface LifeCycleService {

	static String COMPONENTNAME = "LFC";
	
	List<LifeCycleCategory> getCategories();
	List<LifeCycleCategory> getCategoriesAsOf(Instant instant);
	RecurrentTask getTask();
	TaskOccurrence runNow();	
}
