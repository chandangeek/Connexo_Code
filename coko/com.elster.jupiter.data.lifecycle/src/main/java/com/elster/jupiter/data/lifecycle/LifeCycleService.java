package com.elster.jupiter.data.lifecycle;

import java.util.List;

import com.elster.jupiter.tasks.RecurrentTask;

public interface LifeCycleService {

	static String COMPONENTNAME = "LFC";
	
	List<LifeCycleCategory> getCategories();
	RecurrentTask getTask();
	void runNow();	
}
