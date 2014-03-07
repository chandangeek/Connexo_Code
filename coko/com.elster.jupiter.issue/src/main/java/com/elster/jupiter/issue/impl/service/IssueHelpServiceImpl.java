package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

// TODO delete when events will be defined by MDC
@Component(name = "com.elster.jupiter.issue.help", service = IssueHelpService.class)
public class IssueHelpServiceImpl implements IssueHelpService {
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile IssueMainService issueMainService;

    public IssueHelpServiceImpl(){}

    @Inject
    public IssueHelpServiceImpl(EventService eventService){
        setEventService(eventService);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }

    @Override
    public void setEventTopics() {
        for (IssueEventType eventType : IssueEventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
    }

    @Override
    public void getEvent() {
        eventService.postEvent(IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic(), new CreateIssueEvent());
    }

    public Optional<Issue> createTestIssue(String statusStr, String reasonStr, String deviceStr, long dueDate){
        IssueStatus status = new IssueStatus();
        status.setName(statusStr);
        status = issueMainService.searchFirst(status).get();

        IssueReason reason = new IssueReason();
        reason.setName(reasonStr);
        reason = issueMainService.searchFirst(reason).get();

        IssueAssignee assignee = new IssueAssignee();
        assignee.setType(IssueAssigneeType.ROLE);
        assignee.setRole(issueMainService.get(AssigneeRole.class, 1).get());

        Issue issue = new Issue();
        issue.setStatus(status);
        issue.setReason(reason);
        issue.setAssignee(assignee);
        issue.setDueDate(new UtcInstant(dueDate));

        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef =  amrSystemRef.get().findMeter(deviceStr);
            if(meterRef.isPresent()) {
                issue.setDevice(meterRef.get());
            }
        }
        return issueMainService.save(issue);
    }
}
