package com.elster.jupiter.issue.datacollection.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.google.common.base.Optional;
import org.osgi.service.event.EventConstants;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class AbstractEvent implements IssueEvent{
    private static final Logger LOG = Logger.getLogger(AbstractEvent.class.getName());

    private IssueService issueService;
    private MeteringService meteringService;
    private EndDevice endDevice;
    private IssueStatus status;

    private IssueEventType eventType;

    public AbstractEvent(IssueService issueService, MeteringService meteringService, Map<?, ?> rawEvent) {
        this.issueService = issueService;
        this.meteringService = meteringService;
        init(rawEvent);
    }

    protected void init(Map<?, ?> rawEvent){
        String topic = String.class.cast(rawEvent.get(EventConstants.EVENT_TOPIC));
        this.eventType = IssueEventType.getEventTypeByTopic(topic);

        Query<IssueStatus> statusQuery = issueService.query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("isFinal").isEqualTo(Boolean.FALSE));
        if (statusList.isEmpty()){
            LOG.severe("Issue creation failed, because no not-final statuses was found");
        } else {
            this.status = statusList.get(0);
        }

        String amrId = String.class.cast(rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER));
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(ModuleConstants.MDC_AMR_SYSTEM_ID);
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(amrId);
            if (meterRef.isPresent()) {
                this.endDevice = meterRef.get();
            }
        }
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    @Override
    public String getEventType() {
        return this.eventType.topic();
    }

    @Override
    public IssueStatus getStatus() {
        return this.status;
    }

    @Override
    public EndDevice getDevice() {
        return this.endDevice;
    }
}
