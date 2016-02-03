package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

public interface MetrologyConfigurationCustomPropertySetUsage {

    MetrologyConfiguration getMetrologyConfiguration();

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();
}
