package com.elster.insight.usagepoint.config;

import java.time.Instant;

import com.elster.jupiter.metering.UsagePoint;

public interface UsagePointMetrologyConfiguration {
	UsagePoint getUsagePoint();
	MetrologyConfiguration getMetrologyConfiguration();
	void setMetrologyConfiguration(MetrologyConfiguration mc);
	public void update();
	public void delete();

	long getId();
    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();
}
