/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.eventpropagator.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.mdm.eventpropagator.handler", service = {MessageHandlerFactory.class, TranslationKeyProvider.class}, property = {"subscriber=" + MeteringMessageHandlerFactory.SUBSCRIBER_NAME, "destination=JupiterEvents"}, immediate = true)
public class MeteringMessageHandlerFactory implements MessageHandlerFactory, TranslationKeyProvider {

    public static final String SUBSCRIBER_NAME = "MdmBpmEventSubscriber";
    public static final TranslationKey SUBSCRIBER_DISPLAYNAME = new SimpleTranslationKey(SUBSCRIBER_NAME, "Handle events to propagate into BPM");
    static final String COMPONENT_NAME = "BEP";

    private volatile BpmService bpmService;
    private volatile MeteringService meteringService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile UpgradeService upgradeService;
    private volatile EventService eventService;
    private volatile Clock clock;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(EventService.class).toInstance(eventService);
            }
        });

        upgradeService.register(InstallIdentifier.identifier("Insight", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new MeteringMessageHandler(jsonService, bpmService, meteringService, clock);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }


    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(SUBSCRIBER_DISPLAYNAME);
    }
}
