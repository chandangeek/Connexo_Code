package com.elster.jupiter.metering;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.HasName;

import java.util.Date;

public interface ServiceCategory extends HasName {
	int getId();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Date start);
}
