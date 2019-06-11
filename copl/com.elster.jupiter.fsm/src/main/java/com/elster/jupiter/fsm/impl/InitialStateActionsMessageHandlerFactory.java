/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.fsm.impl.InitialStateActionsMessageHandlerFactory",
        property = {"subscriber=" + InitialStateActionsMessageHandlerFactory.SUBSCRIBER_NAME,
                "destination=" + InitialStateActionsMessageHandlerFactory.DESTINATION_NAME,
                "name=" + InitialStateActionsMessageHandlerFactory.COMPONENT_NAME},
        service = {MessageHandlerFactory.class}, immediate = true)
public class InitialStateActionsMessageHandlerFactory implements MessageHandlerFactory {
    static final String COMPONENT_NAME = FiniteStateMachineService.COMPONENT_NAME;
    static final String DESTINATION_NAME = "InitialStateActions";
    static final String SUBSCRIBER_NAME = "InitialStateActions";

    private volatile UpgradeService upgradeService;
    private volatile EventService eventService;
    private volatile UserService userService;
    private volatile JsonService jsonService;

    @Inject
    public InitialStateActionsMessageHandlerFactory() {
    }

    @Inject
    public InitialStateActionsMessageHandlerFactory(UserService userService, EventService eventService, JsonService jsonService) {
        this.eventService = eventService;
        this.userService = userService;
        this.jsonService = jsonService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new InitialStateActionsMessageHandler(jsonService, eventService);
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(JsonService.class).toInstance(jsonService);
            }
        });
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setJsonService(JsonService jsonService){
        this.jsonService = jsonService;
    }
}