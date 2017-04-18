package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import javax.inject.Inject;
import java.time.Instant;
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

    public void addPurposeOutputsDataInfo(List<PurposeOutputsDataInfo> outputsDataInfos, Long outputId, ChannelReadingWithValidationStatus record){
        outputsDataInfos.forEach(purposeInfo -> {
            if(!purposeInfo.timeStamp.equals(record.getTimeStamp())){
                outputsDataInfos.add(createPurposeOutputsDataInfo(record.getTimeStamp()));
            }
            outputsDataInfos.forEach(info -> {
                if (info.timeStamp.equals(record.getTimeStamp())) {
                    info.outputsData.put(outputId, outputChannelDataInfoFactory.createChannelDataInfo(record));
                }
            });
        });
    }

    public PurposeOutputsDataInfo createPurposeOutputsDataInfo (Instant timestamp, Long channelId, OutputChannelDataInfo channelDataInfo){
        PurposeOutputsDataInfo info = new PurposeOutputsDataInfo();
        info.timeStamp = timestamp;
        info.outputsData.put(channelId, channelDataInfo);
        return info;
    }

    public PurposeOutputsDataInfo createPurposeOutputsDataInfo (Instant timestamp ){
        PurposeOutputsDataInfo info = new PurposeOutputsDataInfo();
        return info;
    }

    public void addValues (PurposeOutputsDataInfo info, Long channelId, OutputChannelDataInfo channelDataInfo){
        info.outputsData.put(channelId, channelDataInfo);
    }
}
