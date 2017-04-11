/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetrologyConfigurationHistoryInfo {

    public Instant start;
    public Instant end;
    public IdWithNameInfo metrologyConfiguration;
    public boolean current;
    public Map<String, List<ReadingTypeInfo>> purposesWithReadingTypes = new HashMap<>();
    public long ongoingProcessesNumber;
    public List<IdWithNameInfo> ongoingProcesses;

}
