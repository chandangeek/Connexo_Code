package com.elster.insight.usagepoint.config;

import java.time.Instant;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.metering.UsagePoint;

@ProviderType
public interface UsagePointMetrologyConfiguration {
	UsagePoint getUsagePoint();
	MetrologyConfiguration getMetrologyConfiguration();
	void updateMetrologyConfiguration(MetrologyConfiguration mc);
	public void delete();

	long getId();
    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();
}
