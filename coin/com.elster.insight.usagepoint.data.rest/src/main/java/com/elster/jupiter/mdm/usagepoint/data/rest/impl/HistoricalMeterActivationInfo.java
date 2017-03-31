/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class HistoricalMeterActivationInfo {
    public long id;
    public Instant start;
    public Instant end;
    public String meter;
    public String url;
    public String meterRole;
    public boolean current;
    public List<IdWithNameInfo> ongoingProcesses;
}
