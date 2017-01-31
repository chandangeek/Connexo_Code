/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Properties;

/**
 * Changes to an EndPointConfiguration are propagated to this handler through PubSub, and from this handler onwards
 * re-published using the MessageService.
 * Created by bvn on 5/6/16.
 */
@Component(name = "com.elster.jupiter.webservices.endpoint.eventhandler", service = TopicHandler.class, immediate = true)
public class EndPointConfigurationTopicHandler implements TopicHandler {

    private MessageService messageService;
    private JsonService jsonService;

    public EndPointConfigurationTopicHandler() {
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
        EndPointConfiguration source = (EndPointConfiguration) localEvent.getSource();
        messageService.getDestinationSpec(AppService.ALL_SERVERS).ifPresent(dest -> sendMessage(dest, source));
    }

    private void sendMessage(DestinationSpec destinationSpec, EndPointConfiguration source) {
        Properties properties = new Properties();
        properties.setProperty("endpoint", source.getName());

        destinationSpec.message(jsonService.serialize(new AppServerCommand(Command.ENDPOINT_CHANGED, properties)))
                .send();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDPOINT_CONFIGURATION_CHANGED.topic();
    }
}
