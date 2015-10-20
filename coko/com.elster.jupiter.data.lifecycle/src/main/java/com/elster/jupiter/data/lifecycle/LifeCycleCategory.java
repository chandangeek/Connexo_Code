package com.elster.jupiter.data.lifecycle;

import java.time.Period;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.HasTranslatableName;

@ProviderType
public interface LifeCycleCategory extends HasTranslatableName {
	
	LifeCycleCategoryKind getKind();
	Period getPartitionSize();
	Period getRetention();
	int getRetainedPartitionCount();
	void setRetentionDays(int days);
	long getVersion();
}
