package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public abstract class DataValidationEvent implements IssueEvent {

    protected Long channelId;
    protected String readingType;
    protected Long deviceConfigurationId;

    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final IssueDataValidationService issueDataValidationService;
    private final IssueService issueService;

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
        getEndDevice().ifPresent(filter::setDevice);
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        Optional<? extends IssueDataValidation> foundIssue = issueDataValidationService.findAllDataValidationIssues(filter).find().stream().findFirst();//It is going to be only zero or one open issue per device
        if (foundIssue.isPresent()) {
            return Optional.of((OpenIssue)foundIssue.get());
        }
        return Optional.empty();
    }

    protected Optional<Channel> findChannel() {
        return meteringService.findChannel(channelId);
    }

    protected Optional<ReadingType> findReadingType() {
        return meteringService.getReadingType(readingType);
    }

    private Optional<Meter> findMeter() {
        return findChannel().flatMap(channel -> channel.getChannelsContainer().getMeter());
    }

    private Device getDevice() {
        return findMeter().map(Meter::getAmrId).map(Long::valueOf).flatMap(deviceService::findDeviceById).orElse(null);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
