/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.calendar.importers.CalendarImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {
                "name=" + CalendarImporterMessageHandler.COMPONENT,
                "subscriber=" + CalendarImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + CalendarImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class CalendarImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider, MessageSeedProvider {

    static final String COMPONENT = "CLI";

    static final String DESTINATION_NAME = "CalendarImport";
    public static final String SUBSCRIBER_NAME = "CalendarImport";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle calendar import";

    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;

    public CalendarImporterMessageHandler() {}

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Pulse", CalendarImporterMessageHandler.COMPONENT), dataModel, Installer.class, Collections.emptyMap());
    }

    @Inject
    public CalendarImporterMessageHandler(MessageService messageService, FileImportService fileImportService, UpgradeService upgradeService) {
        this();
        setMessageService(messageService);
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
    }

    @Override
    public String getComponentName() {
        return COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }
}


