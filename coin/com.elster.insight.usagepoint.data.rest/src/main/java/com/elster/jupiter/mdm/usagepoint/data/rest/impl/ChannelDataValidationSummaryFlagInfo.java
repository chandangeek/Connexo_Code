/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.util.List;

public class ChannelDataValidationSummaryFlagInfo {
    public String key;
    public String displayName;
    public int count;
    public List<ChannelDataValidationSummaryFlagInfo> detail;

    ChannelDataValidationSummaryFlagInfo(String key, String displayName, int count) {
        this.key = key;
        this.displayName = displayName;
        this.count = count;
    }

    ChannelDataValidationSummaryFlagInfo(String key, String displayName, int count, List<ChannelDataValidationSummaryFlagInfo> detail) {
        this.key = key;
        this.displayName = displayName;
        this.count = count;
        this.detail = detail;
    }
}
