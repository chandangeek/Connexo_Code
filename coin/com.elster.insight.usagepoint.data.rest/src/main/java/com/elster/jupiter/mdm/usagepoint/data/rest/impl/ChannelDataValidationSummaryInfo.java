/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.util.List;

public class ChannelDataValidationSummaryInfo {
    public long id;
    public String name;
    public int total;
    public List<ChannelDataValidationSummaryFlagInfo> statistics;

    ChannelDataValidationSummaryInfo(long id, String name, int total,
                                     List<ChannelDataValidationSummaryFlagInfo> statistics) {
        this.id = id;
        this.name = name;
        this.total = total;
        this.statistics = statistics;
    }
}
