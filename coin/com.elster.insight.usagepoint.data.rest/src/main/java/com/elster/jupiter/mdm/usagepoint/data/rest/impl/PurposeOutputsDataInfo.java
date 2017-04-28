package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aeryomin on 17.04.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurposeOutputsDataInfo {
    public Map<Long,BigDecimal> channelData = new HashMap<>();
    public Map<String, Long> interval = new HashMap<>();
}
