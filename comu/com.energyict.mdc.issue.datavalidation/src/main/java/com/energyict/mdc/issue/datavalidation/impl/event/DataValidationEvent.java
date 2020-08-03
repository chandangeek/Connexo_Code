/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;

import com.google.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class DataValidationEvent implements IssueEvent {

    protected Long channelId;
    protected String readingType;
    protected Long deviceConfigurationId;
    private int ruleId;

    protected final Thesaurus thesaurus;
    protected final MeteringService meteringService;
    protected final DeviceService deviceService;
    protected final IssueDataValidationService issueDataValidationService;
    protected final IssueService issueService;

    @Inject
    public DataValidationEvent(Thesaurus thesaurus, MeteringService meteringService, DeviceService deviceService, IssueDataValidationService issueDataValidationService, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.issueDataValidationService = issueDataValidationService;
        this.issueService = issueService;
    }

    abstract void init(Map<?, ?> jsonPayload);

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
        return findMeter().map(EndDevice.class::cast);
    }

    public long getDeviceConfigurationId() {
        if (deviceConfigurationId == null) {
            deviceConfigurationId = getDevice().getDeviceConfiguration().getId();
        }
        return deviceConfigurationId;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        Optional<CreationRule> rule = issueService.getIssueCreationService().findCreationRuleById(ruleId);
        if (rule.isPresent()) {
            filter.addRule(rule.get());
            Stream.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS, IssueStatus.SNOOZED)
                    .map(issueService::findStatus)
                    .map(Optional::get)
                    .forEach(filter::addStatus);
            getEndDevice().ifPresent(filter::setDevice);
            return issueDataValidationService.findAllDataValidationIssues(filter).paged(0, 0).stream()
                    .findAny()
                    .map(OpenIssueDataValidation.class::cast);
        }
        return Optional.empty();
    }

    /**
     * used by issue creation rule
     */
    public void setCreationRule(int ruleId) {
        this.ruleId = ruleId;
    }

    protected Optional<ChannelsContainer> findChannelsContainer() {
        return findChannel().map(Channel::getChannelsContainer);
    }

    protected Optional<Channel> findChannel() {
        return meteringService.findChannel(channelId);
    }

    protected Optional<ReadingType> findReadingType() {
        return meteringService.getReadingType(readingType);
    }

    private Optional<Meter> findMeter() {
        return findChannelsContainer().flatMap(ChannelsContainer::getMeter);
    }

    protected Device getDevice() {
        return findMeter().map(Meter::getAmrId).map(Long::valueOf).flatMap(deviceService::findDeviceById).orElse(null);
    }

    protected List<LoadProfile> getLoadProfiles() {
        return getDevice().getLoadProfiles();
    }
}
