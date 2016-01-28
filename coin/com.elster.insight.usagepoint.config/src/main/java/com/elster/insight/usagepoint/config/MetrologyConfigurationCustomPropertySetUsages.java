package com.elster.insight.usagepoint.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

@ProviderType
public interface MetrologyConfigurationCustomPropertySetUsages {

    MetrologyConfiguration getMetrologyConfiguration();

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();
}
