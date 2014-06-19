package com.elster.jupiter.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.elster.jupiter.issue.datacollection.MeterReadingEventHandlerFactory", service = MessageHandlerFactory.class, property = {"subscriber=" + ModuleConstants.AQ_METER_READING_EVENT_SUBSC, "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class MeterReadingEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return new MeterReadingEventHandler(jsonService, issueService, issueCreationService, meteringService, thesaurus);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public final void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN);
    }
}
