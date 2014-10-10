package com.elster.jupiter.metering;

import com.elster.jupiter.nls.HasTranslatableName;

import java.time.Instant;

public interface ServiceCategory extends HasTranslatableName {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);
}
