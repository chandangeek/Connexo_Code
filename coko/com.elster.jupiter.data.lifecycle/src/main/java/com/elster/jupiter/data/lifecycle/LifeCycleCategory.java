package com.elster.jupiter.data.lifecycle;

import java.time.Period;

public interface LifeCycleCategory {
	
	LifeCycleCategoryKind getKind();
	Period getPartitionSize();
	Period getRetention();
	int getRetainedPartitionCount();
	void setRetentionDays(int days);
}
