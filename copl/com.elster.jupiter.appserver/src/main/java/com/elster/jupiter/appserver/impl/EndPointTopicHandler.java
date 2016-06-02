package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Changed to and EndPointConfiguration are propagated to this handler through PubSub, and from this handler onwards
 * re-published using the MessageService.
 * Created by bvn on 5/6/16.
 */
@Component(name = "com.elster.jupiter.webservices.endpoint.eventhandler", service = TopicHandler.class, immediate = true)
public class EndPointTopicHandler implements TopicHandler {

    private MessageService messageService;
    private JsonService jsonService;

    public EndPointTopicHandler() {
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        messageService.getDestinationSpec(AppService.ALL_SERVERS)
                .ifPresent(destination -> destination.message(jsonService.serialize(new AppServerCommand(Command.CONFIG_CHANGED)))
                        .send());
    }

    @Override
    public String getTopicMatcher() {
        return EventType.WEB_SERVICE_CHANGED.topic();
    }
}
