package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.records.CreateIssueEventImpl;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

// TODO delete when events will be defined by MDC
/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
@Component(name = "com.elster.jupiter.issue.help", service = IssueHelpService.class)
public class IssueHelpServiceImpl implements IssueHelpService {
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile IssueService issueService;

    public IssueHelpServiceImpl(){}

    @Inject
    public IssueHelpServiceImpl(EventService eventService, MeteringService meteringService, IssueService issueService) {
        setEventService(eventService);
        setMeteringService(meteringService);
        setIssueService(issueService);
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void postEvent(String topic, String eventIdentifier){
        IssueEventType type = IssueEventType.getEventTypeByTopic(topic);
        if (type == null) {
            type = IssueEventType.DEVICE_CONNECTION_FAILURE;
        }
        eventService.postEvent(type.topic(), new CreateIssueEventImpl(type.topic(), eventIdentifier));
    }
}
