package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IntervalInfo;
import java.time.Instant;
import java.util.*;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public IntervalInfo interval;
    public Map<Long, String> channelData = new HashMap<>();
    public Map<Long, String> channelCollectedData = new HashMap<>();
    public Map<Long, ValidationInfo> channelValidationData = new HashMap<>();
    public Instant readingTime;
    public List<String> intervalFlags;
    public boolean validationActive;

}