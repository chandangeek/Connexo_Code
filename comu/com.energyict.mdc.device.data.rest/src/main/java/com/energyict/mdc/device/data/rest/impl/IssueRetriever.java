/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bbl on 18/06/2016.
 */
public class IssueRetriever {

    private final IssueService issueService;
    private IssueType dataCollectionIssueType;
    private IssueType dataValidationIssueType;
    private Map<Long, List<String>> deviceIssueCache;

    public IssueRetriever(IssueService issueService) {
        this(issueService, null);
    }

    public IssueRetriever(IssueService issueService, List<Device> devices) {
        this.issueService = issueService;
        if (devices != null) {
            deviceIssueCache = issueService.findOpenIssuesPerIssueTypeForDevices(devices.stream().map(Device::getId).collect(Collectors.toList()));
        } else {
            deviceIssueCache = null;
        }
        dataCollectionIssueType = issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
        dataValidationIssueType = issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    public boolean hasOpenDataCollectionIssues(Device device) {
        List<String> issueTypes = getOpenIssuesForDevice(device);
        return issueTypes.stream().anyMatch(issueType -> issueType.equals(dataCollectionIssueType.getKey()));
    }

    public boolean hasOpenDataValidationIssues(Device device) {
        List<String> issueTypes = getOpenIssuesForDevice(device);
        return issueTypes.stream().anyMatch(issueType -> issueType.equals(dataValidationIssueType.getKey()));
    }

    public int numberOfDataCollectionIssues(Device device) {
        return (int) issueService.findOpenIssuesForDevice(device.getName()).find().stream().filter(issue -> dataCollectionIssueType.equals(issue.getReason().getIssueType())).count();
    }

    public Optional<OpenIssue> getOpenDataValidationIssue(Device device) {
        return issueService.findOpenIssuesForDevice(device.getName())
                .stream().filter(issue -> dataValidationIssueType.equals(issue.getReason().getIssueType())).findFirst();
    }

    private List<String> getOpenIssuesForDevice(Device device) {
        if (deviceIssueCache != null) {
            return deviceIssueCache.containsKey(device.getId()) ? deviceIssueCache.get(device.getId()) : Collections.emptyList();
        } else {
            return issueService.findOpenIssuesForDevice(device.getName()).find()
                    .stream()
                    .map(OpenIssue::getReason)
                    .map(IssueReason::getIssueType)
                    .map(IssueType::getKey)
                    .collect(Collectors.toList());
        }
    }
}

