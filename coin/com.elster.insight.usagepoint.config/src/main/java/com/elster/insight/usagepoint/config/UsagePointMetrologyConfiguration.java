package com.elster.insight.usagepoint.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;

public interface UsagePointMetrologyConfiguration extends Effectivity {
	long getId();
	UsagePoint getUsagePoint();
	MetrologyConfiguration getMetrologyConfiguration();
	boolean isCurrent();
	public void update();
	public void delete();
	
    /**
     * @param other
     * @return true if the argument defines a configuration which overlaps this instance's interval.
     */
	boolean conflictsWith(UsagePointMetrologyConfiguration other);

    long getVersion();
}
