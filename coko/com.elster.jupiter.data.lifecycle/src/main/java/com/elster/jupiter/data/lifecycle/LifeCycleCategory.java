package com.elster.jupiter.data.lifecycle;

import java.time.Period;

public interface LifeCycleCategory {
	
	LifeCycleCategoryName getName();
	Period getPartitionSize();
	Period getRetention();
	int getRetainedPartitionCount();
}
