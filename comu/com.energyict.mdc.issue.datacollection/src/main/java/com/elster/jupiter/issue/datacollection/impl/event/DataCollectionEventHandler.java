package com.elster.jupiter.issue.datacollection.impl.event;

import com.elster.jupiter.issue.datacollection.DataCollectionEvent;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonService;

import java.util.Map;

public class DataCollectionEventHandler implements MessageHandler {

    private final JsonService jsonService;
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final MeteringService meteringService;

    public DataCollectionEventHandler(JsonService jsonService, IssueService issueService, IssueCreationService issueCreationService, MeteringService meteringService) {
        this.jsonService = jsonService;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
        this.meteringService = meteringService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        DataCollectionEvent event = new DataCollectionEvent(issueService, meteringService, map);
        issueCreationService.checkCreationEvent(event);
    }
}
