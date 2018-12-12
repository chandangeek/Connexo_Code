/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;

public interface EventSetOnMetrologyConfiguration {

    MetrologyConfiguration getMetrologyConfiguration();

    EventSet getEventSet();

}
