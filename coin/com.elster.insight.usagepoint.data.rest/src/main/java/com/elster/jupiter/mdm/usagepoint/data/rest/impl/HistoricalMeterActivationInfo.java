/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class HistoricalMeterActivationInfo implements Comparable<HistoricalMeterActivationInfo> {
    public long id;
    public Long start;
    public Long end;
    public String meter;
    public String url;
    public String meterRole;
    public boolean current;
    public List<IdWithNameInfo> ongoingProcesses;

    @Override
    public int compareTo(HistoricalMeterActivationInfo o) {
        return Long.compare(start, o.start);
    }
}
