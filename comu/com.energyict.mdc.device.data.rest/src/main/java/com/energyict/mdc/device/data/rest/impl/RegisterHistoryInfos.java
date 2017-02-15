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
 * Contains a non-limited, non-paged list of the history of linked datalogger-slave registers
 */
@XmlRootElement
public class RegisterHistoryInfos {
    @XmlElement
    public List<RegisterHistoryInfo> registerHistory = new ArrayList<>();

    /**
     * Creates a new instance of this RegisterHistoryInfos with the proper RegisterHistoryInfo objects.
     * We assume that the order in which the dataLoggerChannelUsages are provided is correct, eg. descending based on the interval.start
     *
     * @param dataLoggerChannelUsages the dataLoggerChannelUsages
     * @return the proper info object
     */
    public static RegisterHistoryInfos from(List<DataLoggerChannelUsage> dataLoggerChannelUsages) {
        RegisterHistoryInfos registerHistoryInfos = new RegisterHistoryInfos();
        dataLoggerChannelUsages.stream().forEach(dataLoggerChannelUsage -> {
            RegisterHistoryInfo newHistoryInfo = RegisterHistoryInfo.from(dataLoggerChannelUsage);
            Optional<RegisterHistoryInfo> lastHistory = registerHistoryInfos.getLastHistory();
            if (!lastHistory.isPresent()) { // it's the first element
                createFirstElement(registerHistoryInfos, newHistoryInfo);
            } else {
                createHistoricalElement(registerHistoryInfos, newHistoryInfo, lastHistory);
            }
        });
        return registerHistoryInfos;
    }

    private static void createFirstElement(RegisterHistoryInfos registerHistoryInfos, RegisterHistoryInfo newHistoryInfo) {
        createInitialGapIfRequired(registerHistoryInfos, newHistoryInfo);
        registerHistoryInfos.registerHistory.add(newHistoryInfo);
    }

    private static void createInitialGapIfRequired(RegisterHistoryInfos registerHistoryInfos, RegisterHistoryInfo newHistoryInfo) {
        if (newHistoryInfo.endDate != null) {
            RegisterHistoryInfo firstHistoryInfo = new RegisterHistoryInfo();
            firstHistoryInfo.startDate = newHistoryInfo.endDate;
            registerHistoryInfos.registerHistory.add(firstHistoryInfo);
        }
    }

    private static void createHistoricalElement(RegisterHistoryInfos registerHistoryInfos, RegisterHistoryInfo newHistoryInfo, Optional<RegisterHistoryInfo> lastHistory) {
        if (lastHistory.get().startDate == null) {
            lastHistory.get().startDate = newHistoryInfo.endDate;
        } else {
            createGapIfRequired(registerHistoryInfos, newHistoryInfo, lastHistory);
        }
        registerHistoryInfos.registerHistory.add(newHistoryInfo);
    }

    private static void createGapIfRequired(RegisterHistoryInfos registerHistoryInfos, RegisterHistoryInfo newHistoryInfo, Optional<RegisterHistoryInfo> lastHistory) {
        if (!Objects.equals(lastHistory.get().startDate, newHistoryInfo.endDate)) {
            RegisterHistoryInfo emptyHistoryInfo = new RegisterHistoryInfo();
            emptyHistoryInfo.startDate = newHistoryInfo.endDate;
            emptyHistoryInfo.endDate = lastHistory.get().startDate;
            registerHistoryInfos.registerHistory.add(emptyHistoryInfo);
        }
    }


    private Optional<RegisterHistoryInfo> getLastHistory() {
        if (!registerHistory.isEmpty()) {
            return Optional.of(registerHistory.get(registerHistory.size() - 1));
        } else {
            return Optional.empty();
        }
    }
}
