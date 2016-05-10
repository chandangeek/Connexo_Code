package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.EventType;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

/**
 * Created by bvn on 5/6/16.
 */
@Component(name = "com.elster.jupiter.webservices.endpoint.eventhandler", service = TopicHandler.class, immediate = true)
public class EndPointTopicHandler implements TopicHandler {

    private final MessageService messageService;
    private final JsonService jsonService;

    @Inject
    public EndPointTopicHandler(MessageService messageService, JsonService jsonService) {
        this.messageService = messageService;
        this.jsonService = jsonService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndPointConfiguration source = (EndPointConfiguration) localEvent.getSource();

        messageService.getDestinationSpec(AppService.ALL_SERVERS)
                .ifPresent(destination -> destination.message(jsonService.serialize(source)).send());
    }

    @Override
    public String getTopicMatcher() {
        return EventType.WEB_SERVICE_CHANGED.topic();
    }
}
