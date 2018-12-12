/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;

public interface MetrologyConfigurationCustomPropertySetUsage {

    MetrologyConfiguration getMetrologyConfiguration();

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();

}