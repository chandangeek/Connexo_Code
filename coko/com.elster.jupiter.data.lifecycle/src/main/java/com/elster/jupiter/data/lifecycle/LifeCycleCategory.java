/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Period;

@ProviderType
public interface LifeCycleCategory extends HasName {

	String getDisplayName();
	LifeCycleCategoryKind getKind();
	Period getPartitionSize();
	Period getRetention();
	int getRetainedPartitionCount();
	void setRetentionDays(int days);
	long getVersion();

}