/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IntervalInfo;

import javax.inject.Inject;
import java.math.BigDecimal;

/**
 * Created by aeryomin on 17.04.2017.
 */
public class PurposeOutputsDataInfoFactory {

    private final OutputChannelDataInfoFactory outputChannelDataInfoFactory;
    @Inject
    public PurposeOutputsDataInfoFactory( OutputChannelDataInfoFactory outputChannelDataInfoFactory){
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
    }

    public PurposeOutputsDataInfo createPurposeOutputsDataInfo (Long channelId, OutputChannelDataInfo outputChannelDataInfo, IntervalInfo intervalInfo){
        PurposeOutputsDataInfo info = new PurposeOutputsDataInfo();
        info.channelData.put(channelId, outputChannelDataInfo);
        info.interval = intervalInfo;
        return info;
    }

    public void addValues (PurposeOutputsDataInfo info, Long channelId, OutputChannelDataInfo outputChannelDataInfo){
        info.channelData.put(channelId, outputChannelDataInfo);
    }
}
