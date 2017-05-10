package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.rest.util.IntervalInfo;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aeryomin on 17.04.2017.
 */
public class PurposeOutputsDataInfoFactory {

    private final OutputChannelDataInfoFactory outputChannelDataInfoFactory;

    @Inject
    public PurposeOutputsDataInfoFactory( OutputChannelDataInfoFactory outputChannelDataInfoFactory){
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
    }

    public PurposeOutputsDataInfo createPurposeOutputsDataInfo (Long channelId, BigDecimal channelData, IntervalInfo intervalInfo){
        PurposeOutputsDataInfo info = new PurposeOutputsDataInfo();
        info.channelData.put(channelId, channelData);
        info.interval.put("end", intervalInfo.end);
        info.interval.put("start", intervalInfo.start);
        return info;
    }

    public PurposeOutputsDataInfo createPurposeOutputsDataInfo (Long intervalStart, Long intervalEnd){
        PurposeOutputsDataInfo info = new PurposeOutputsDataInfo();
        info.interval.put("end", intervalEnd);
        info.interval.put("start", intervalStart);
        return info;
    }

    public void addValues (PurposeOutputsDataInfo info, Long channelId, BigDecimal channelData){
        info.channelData.put(channelId, channelData);
    }
}
