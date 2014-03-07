package com.elster.jupiter.issue.impl.event;

import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.event.EventConstants;

import java.util.Map;

public class CreateIssueEventMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final IssueService issueService;

    public CreateIssueEventMessageHandler(JsonService jsonService, IssueService issueService) {
        this.jsonService = jsonService;
        this.issueService = issueService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        Object topic = map.get(EventConstants.EVENT_TOPIC);
        IssueEventType eventType = IssueEventType.getEventTypeByTopic(topic);
        if (eventType != null) {
            issueService.createIssue(map);
        }
    }
}
