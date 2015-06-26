package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class DataValidationEvent implements IssueEvent {

    protected Long channelId;
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
    public EndDevice getEndDevice() {
        return findMeter().orElse(null);
    }

    public long getDeviceConfigurationId() {
        if (deviceConfigurationId == null) {
            deviceConfigurationId = getDevice().getDeviceConfiguration().getId();
        }
        return deviceConfigurationId;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setDevice(getEndDevice());
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        return issues.stream().findFirst();//It is going to be only zero or one open issue per device
    }

    protected Optional<Channel> findChannel() {
        return meteringService.findChannel(channelId);
    }

    private Optional<Meter> findMeter() {
        return findChannel().map(channel -> channel.getMeterActivation().getMeter()).orElse(Optional.empty());
    }

    private Device getDevice() {
        return findMeter().map(meter -> {
            return deviceService.findDeviceById(Long.valueOf(meter.getAmrId())).orElse(null);
        }).orElse(null);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }
}
