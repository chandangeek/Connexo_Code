package com.elster.jupiter.metering;

import com.elster.jupiter.nls.HasTranslatableName;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.util.HasId;

import java.time.Instant;

public interface ServiceCategory extends HasTranslatableName, HasAuditInfo, HasId {
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePointBuilder newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);
}
