package com.elster.insight.usagepoint.config;

import java.time.Instant;

import com.elster.jupiter.metering.UsagePoint;

public interface UsagePointMetrologyConfiguration {
	UsagePoint getUsagePoint();
	MetrologyConfiguration getMetrologyConfiguration();
	public void update();
	public void delete();

    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();
}
