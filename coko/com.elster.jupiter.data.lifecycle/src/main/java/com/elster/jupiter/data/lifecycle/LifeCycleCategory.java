package com.elster.jupiter.data.lifecycle;

import java.time.Period;

import com.elster.jupiter.nls.HasTranslatableName;

public interface LifeCycleCategory extends HasTranslatableName {
	
	LifeCycleCategoryKind getKind();
	Period getPartitionSize();
	Period getRetention();
	int getRetainedPartitionCount();
	void setRetentionDays(int days);
}
