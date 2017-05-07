/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.List;

public class PrevalidateChannelDataRequestInfo {

    public Instant validateUntil;

    public List<OutputChannelDataInfo> editedReadings;
}
