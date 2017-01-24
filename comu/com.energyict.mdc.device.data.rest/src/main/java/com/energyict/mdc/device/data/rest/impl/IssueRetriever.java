package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.data.Device;
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
    private Map<String, List<OpenIssue>> deviceIssueCache;

    public IssueRetriever(IssueService issueService) {
        this(issueService, null);
    }

    public IssueRetriever(IssueService issueService, List<Device> devices) {
        this.issueService = issueService;
        if (devices != null) {
            deviceIssueCache = issueService.findOpenIssuesForDevices(devices.stream().map(Device::getName).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.groupingBy(openIssue -> openIssue.getDevice().getName()));
        } else {
            deviceIssueCache = null;
        }
        dataCollectionIssueType = issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
        dataValidationIssueType = issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    public boolean hasOpenDataCollectionIssues(Device device) {
        List<OpenIssue> openIssues = getOpenIssuesForDevice(device);
        return openIssues.stream().anyMatch(issue -> dataCollectionIssueType.equals(issue.getReason().getIssueType()));
    }

    private List<OpenIssue> getOpenIssuesForDevice(Device device) {
        if (deviceIssueCache != null) {
            return deviceIssueCache.containsKey(device.getName()) ? deviceIssueCache.get(device.getName()) : Collections.emptyList();
        } else {
            return issueService.findOpenIssuesForDevice(device.getName()).find();
        }
    }

    public boolean hasOpenDataValidationIssues(Device device) {
        List<OpenIssue> openIssues = getOpenIssuesForDevice(device);
        return openIssues.stream().anyMatch(issue -> dataValidationIssueType.equals(issue.getReason().getIssueType()));
    }

    public int numberOfDataCollectionIssues(Device device) {
        return (int) getOpenIssuesForDevice(device).stream().filter(issue -> dataCollectionIssueType.equals(issue.getReason().getIssueType())).count();
    }

    public Optional<OpenIssue> getOpenDataValidationIssue(Device device) {
        return getOpenIssuesForDevice(device).stream().filter(issue -> dataValidationIssueType.equals(issue.getReason().getIssueType())).findFirst();
    }
}
