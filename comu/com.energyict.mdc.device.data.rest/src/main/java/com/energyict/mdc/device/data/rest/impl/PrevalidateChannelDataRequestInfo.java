/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.List;

public class PrevalidateChannelDataRequestInfo {

    public Instant validateUntil;

    public List<ChannelDataInfo> editedReadings;
}