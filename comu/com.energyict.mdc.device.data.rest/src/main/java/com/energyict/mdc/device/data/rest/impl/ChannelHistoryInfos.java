/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains a non-limited, non-paged list of the history of linked datalogger-slave channels
 */
@XmlRootElement
public class ChannelHistoryInfos {
    @XmlElement
    public List<ChannelHistoryInfo> channelHistory = new ArrayList<>();

    /**
     * Creates a new instance of this ChannelHistoryInfos with the proper ChannelHistoryInfo objects.
     * We assume that the order in which the dataLoggerChannelUsages are provided is correct, eg. descending based on the interval.start
     *
     * @param dataLoggerChannelUsages the dataLoggerChannelUsages
     * @return the proper info object
     */
    public static ChannelHistoryInfos from(List<DataLoggerChannelUsage> dataLoggerChannelUsages) {
        ChannelHistoryInfos channelHistoryInfos = new ChannelHistoryInfos();
        dataLoggerChannelUsages.stream().forEach(dataLoggerChannelUsage -> {
            ChannelHistoryInfo newHistoryInfo = ChannelHistoryInfo.from(dataLoggerChannelUsage);
            Optional<ChannelHistoryInfo> lastHistory = channelHistoryInfos.getLastHistory();
            if (!lastHistory.isPresent()) { // it's the first element
                createFirstElement(channelHistoryInfos, newHistoryInfo);
            } else {
                createHistoricalElement(channelHistoryInfos, newHistoryInfo, lastHistory);
            }
        });
        return channelHistoryInfos;
    }

    private static void createFirstElement(ChannelHistoryInfos channelHistoryInfos, ChannelHistoryInfo newHistoryInfo) {
        createInitialGapIfRequired(channelHistoryInfos, newHistoryInfo);
        channelHistoryInfos.channelHistory.add(newHistoryInfo);
    }

    private static void createHistoricalElement(ChannelHistoryInfos channelHistoryInfos, ChannelHistoryInfo newHistoryInfo, Optional<ChannelHistoryInfo> lastHistory) {
        if (lastHistory.get().startDate == null) {
            lastHistory.get().startDate = newHistoryInfo.endDate;
        } else {
            addGapIfRequired(channelHistoryInfos, newHistoryInfo, lastHistory);
        }
        channelHistoryInfos.channelHistory.add(newHistoryInfo);
    }

    private static void createInitialGapIfRequired(ChannelHistoryInfos channelHistoryInfos, ChannelHistoryInfo newHistoryInfo) {
        if (newHistoryInfo.endDate != null) {
            ChannelHistoryInfo firstHistoryInfo = new ChannelHistoryInfo();
            firstHistoryInfo.startDate = newHistoryInfo.endDate;
            channelHistoryInfos.channelHistory.add(firstHistoryInfo);
        }
    }

    private static void addGapIfRequired(ChannelHistoryInfos channelHistoryInfos, ChannelHistoryInfo newHistoryInfo, Optional<ChannelHistoryInfo> lastHistory) {
        if (!Objects.equals(lastHistory.get().startDate, newHistoryInfo.endDate)) {
            ChannelHistoryInfo emptyHistoryInfo = new ChannelHistoryInfo();
            emptyHistoryInfo.startDate = newHistoryInfo.endDate;
            emptyHistoryInfo.endDate = lastHistory.get().startDate;
            channelHistoryInfos.channelHistory.add(emptyHistoryInfo);
        }
    }

    private Optional<ChannelHistoryInfo> getLastHistory() {
        if (!channelHistory.isEmpty()) {
            return Optional.of(channelHistory.get(channelHistory.size() - 1));
        } else {
            return Optional.empty();
        }
    }
}
