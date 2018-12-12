/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;


@Component(name = "com.elster.jupiter.metering.imports.impl.UsagePointReadingMessageHandlerFactory", property = {"subscriber=" + UsagePointReadingMessageHandlerFactory.SUBSCRIBER_NAME, "destination=" + UsagePointReadingMessageHandlerFactory.DESTINATION_NAME}, service = MessageHandlerFactory.class, immediate = true)
public class UsagePointReadingMessageHandlerFactory implements MessageHandlerFactory, TranslationKeyProvider, MessageSeedProvider {

    static final String COMPONENT_NAME = "RUI";
    static final String DESTINATION_NAME = "UsgPointReadingImp";
    static final String SUBSCRIBER_NAME = "UsgPointReadingImp";

    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile OrmService ormService;

    @Inject
    public UsagePointReadingMessageHandlerFactory() {
    }

    @Inject
    public UsagePointReadingMessageHandlerFactory(MessageService messageService, FileImportService fileImportService, UpgradeService upgradeService, OrmService ormService) {
        this.messageService = messageService;
        this.fileImportService = fileImportService;
        this.upgradeService = upgradeService;
        this.ormService = ormService;
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(OrmService.class).toInstance(ormService);
            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", UsagePointReadingMessageHandlerFactory.COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(version(10, 5), UpgraderV10_5.class
                ));
    }

    @Deactivate
    public void deactivate() {
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

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return null;
    }

    @Override
    public String getComponentName() {
        return null;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return null;
    }
}
