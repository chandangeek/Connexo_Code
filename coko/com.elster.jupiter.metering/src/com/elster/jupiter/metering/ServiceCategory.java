package com.elster.jupiter.metering;

public interface ServiceCategory {	
	int getId();
	String getName();
	String getAliasName();
	String getDescription();
	ServiceKind getKind();
	UsagePoint newUsagePoint(String mRID);
}
