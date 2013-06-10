package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasName;

public interface ServiceCategory extends HasName {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);
}
