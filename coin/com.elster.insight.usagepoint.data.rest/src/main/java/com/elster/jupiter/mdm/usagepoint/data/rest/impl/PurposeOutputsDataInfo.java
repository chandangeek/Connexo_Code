package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.*;

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
    public com.elster.jupiter.rest.util.IntervalInfo interval;
}
