/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

@ProviderType
public interface LifeCycleService {

	String COMPONENTNAME = "LFC";
	
	List<LifeCycleCategory> getCategories();
	
	List<LifeCycleCategory> getCategoriesAsOf(Instant instant);
	
	Optional<LifeCycleCategory> findAndLockCategoryByKeyAndVersion(LifeCycleCategoryKind key, long version);
	
	RecurrentTask getTask();
	
	TaskOccurrence runNow();	
}
