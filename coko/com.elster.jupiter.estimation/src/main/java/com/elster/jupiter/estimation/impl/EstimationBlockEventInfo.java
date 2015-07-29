package com.elster.jupiter.estimation.impl;

import java.util.List;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;

public class EstimationBlockEventInfo {

    private long startTime;
    private long endTime;
    private long channelId;
    private String readingType;

    public static EstimationBlockEventInfo forFailure(EstimationBlock estimationBlock) {
        EstimationBlockEventInfo eventInfo = new EstimationBlockEventInfo();
        List<? extends Estimatable> estimatables = estimationBlock.estimatables();
        if (estimatables.isEmpty()) {
            throw new IllegalArgumentException();
        }
        eventInfo.setStartTime(estimatables.get(0).getTimestamp().toEpochMilli());
        eventInfo.setEndTime(estimatables.get(estimatables.size() - 1).getTimestamp().toEpochMilli());
        eventInfo.setChannelId(estimationBlock.getChannel().getId());
        eventInfo.setReadingType(estimationBlock.getReadingType().getMRID());
        return eventInfo;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getReadingType() {
        return readingType;
    }
}
