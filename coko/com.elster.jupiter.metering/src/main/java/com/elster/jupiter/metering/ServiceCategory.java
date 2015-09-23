package com.elster.jupiter.metering;

import java.time.Instant;

import com.elster.jupiter.nls.HasTranslatableName;
import com.elster.jupiter.orm.HasAuditInfo;

public interface ServiceCategory extends HasTranslatableName, HasAuditInfo {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);

	UsagePointBuilder newUsagePointBuilder();
	
    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);
}
