/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

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
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceDataImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + DeviceDataImporterMessageHandler.COMPONENT,
                    "subscriber=" + DeviceDataImporterMessageHandler.SUBSCRIBER_NAME,
                    "destination=" + DeviceDataImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class DeviceDataImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider, MessageSeedProvider {

    static final String COMPONENT = "DDI";

    static final String DESTINATION_NAME = "DataImport";
    static final String SUBSCRIBER_NAME = "DataImport";

    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile OrmService ormService;

    public DeviceDataImporterMessageHandler() {}

    @Inject
    public DeviceDataImporterMessageHandler(MessageService messageService, FileImportService fileImportService, UpgradeService upgradeService, OrmService ormService) {
        this();
        setMessageService(messageService);
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
        setOrmService(ormService);
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
                InstallIdentifier.identifier("MultiSense", DeviceDataImporterMessageHandler.COMPONENT),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
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

    @Reference(target = "(com.elster.jupiter.checklist=MultiSense)")
    public void setCheckList(UpgradeCheckList upgradeCheckList) {
        // just explicitly depend
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }
}
