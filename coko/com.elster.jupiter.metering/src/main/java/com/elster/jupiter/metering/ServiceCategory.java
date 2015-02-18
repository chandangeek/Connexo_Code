package com.elster.jupiter.metering;

import com.elster.jupiter.nls.HasTranslatableName;
import com.elster.jupiter.orm.HasAuditInfo;

import java.time.Instant;

public interface ServiceCategory extends HasTranslatableName, HasAuditInfo {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);
}
