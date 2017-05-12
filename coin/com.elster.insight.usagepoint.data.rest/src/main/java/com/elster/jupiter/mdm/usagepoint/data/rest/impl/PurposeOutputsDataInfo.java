/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aeryomin on 17.04.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurposeOutputsDataInfo {
    public Map<Long,BigDecimal> channelData = new HashMap<>();
    public IntervalInfo interval;
}
