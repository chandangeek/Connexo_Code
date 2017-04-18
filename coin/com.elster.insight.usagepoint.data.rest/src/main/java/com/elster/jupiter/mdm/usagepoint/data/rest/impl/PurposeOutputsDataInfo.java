package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aeryomin on 17.04.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurposeOutputsDataInfo {
    public Map<Long,OutputChannelDataInfo> outputsData = new HashMap<>();
    public Instant timeStamp;
}
