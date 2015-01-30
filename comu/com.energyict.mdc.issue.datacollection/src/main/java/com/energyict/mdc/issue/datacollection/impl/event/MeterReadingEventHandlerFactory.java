package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.issue.datacollection.MeterReadingEventHandlerFactory", service = MessageHandlerFactory.class,
        property = {"subscriber=" + ModuleConstants.AQ_METER_READING_EVENT_SUBSC, "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class MeterReadingEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;

    @Override
    public MessageHandler newMessageHandler() {
        return new MeterReadingEventHandler(jsonService, issueCreationService, meteringService, clock);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

}