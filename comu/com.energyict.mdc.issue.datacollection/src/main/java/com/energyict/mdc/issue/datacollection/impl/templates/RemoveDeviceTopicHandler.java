/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.issue.datacollection.RemoveDeviceTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveDeviceTopicHandler implements TopicHandler {
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private final Logger LOGGER = Logger.getLogger(RemoveDeviceTopicHandler.class.getName());


    public RemoveDeviceTopicHandler() {
        // for OSGI purpose
        LOGGER.log(Level.FINE, "Starting " + getClass().getSimpleName() + "...");
    }

    @Inject
    public RemoveDeviceTopicHandler(IssueService issueService, IssueDataCollectionService issueDataCollectionService) {
        setIssueService(issueService);
        setIssueDataCollectionService(issueDataCollectionService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Device device = (Device) localEvent.getSource();
        LOGGER.log(Level.FINE, "Removing data collection issues related to device '" + device.getName() + "'...");
        issueService.findStatus(IssueStatus.WONT_FIX).ifPresent(status -> wontFixOpenIssuesWithDevice(device, status));
        getHistoricalIssuesWithDevice(device).forEach(Issue::delete);
    }

    private void wontFixOpenIssuesWithDevice(Device device, IssueStatus status) {
        getOpenIssuesWithDevice(device).forEach(openIssue -> openIssue.close(status));
    }

    private List<OpenIssueDataCollection> getOpenIssuesWithDevice(Device device) {
        return issueDataCollectionService.query(OpenIssueDataCollection.class, ComTaskExecution.class, ConnectionTask.class)
                .select(where("comTask.device").isEqualTo(device).or(where("connectionTask.device").isEqualTo(device)));
    }

    private List<HistoricalIssueDataCollection> getHistoricalIssuesWithDevice(Device device) {
        return issueDataCollectionService.query(HistoricalIssueDataCollection.class, ComTaskExecution.class, ConnectionTask.class)
                .select(where("comTask.device").isEqualTo(device).or(where("connectionTask.device").isEqualTo(device)));
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }
}
