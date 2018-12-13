/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IntervalInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public IntervalInfo interval;
    public Map<Long, String> channelData = new HashMap<>();
    public Map<Long, String> channelCollectedData = new HashMap<>();
    public Map<Long, MinimalVeeReadingInfo> channelValidationData = new HashMap<>();
    public Instant readingTime;
    public Map<Long, List<String>> readingQualities;

}