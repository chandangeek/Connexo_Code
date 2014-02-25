package com.elster.jupiter.metering;

import com.elster.jupiter.nls.HasTranslatableName;

import java.util.Date;

public interface ServiceCategory extends HasTranslatableName {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Date start);
}
