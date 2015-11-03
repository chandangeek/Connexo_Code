package com.elster.insight.usagepoint.config;

import java.time.Instant;

import com.elster.jupiter.metering.UsagePoint;

public interface UsagePointMetrologyConfiguration {
	UsagePoint getUsagePoint();
	MetrologyConfiguration getMetrologyConfiguration();
	//TODO: JP-480 change to updateMetrologyConfiguration and make it persist.
	void setMetrologyConfiguration(MetrologyConfiguration mc);
	//TODO: Remove? JP-480
	public void update();
	public void delete();

	long getId();
    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();
}
