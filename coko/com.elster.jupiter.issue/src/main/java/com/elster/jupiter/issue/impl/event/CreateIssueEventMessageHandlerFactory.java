package com.elster.jupiter.issue.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.elster.jupiter.issue.event", service = MessageHandlerFactory.class, property = {"subscriber=" + EventConst.AQ_SUBSCRIBER_NAME, "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class CreateIssueEventMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueService issueService;
    private volatile EventService eventService;

    @Override
    public MessageHandler newMessageHandler() {
        return new CreateIssueEventMessageHandler(jsonService, issueService);
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMeteringService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private void createEventTypes() {
        for (IssueEventType eventType : IssueEventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
    }
}
