/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;

public class OutputChannelHistoryDataInfo extends OutputChannelDataInfo {

    public String userName;
    public Instant journalTime;
    public boolean isActive;
    public long version;
    public long commentId;
    public String commentValue;
}
