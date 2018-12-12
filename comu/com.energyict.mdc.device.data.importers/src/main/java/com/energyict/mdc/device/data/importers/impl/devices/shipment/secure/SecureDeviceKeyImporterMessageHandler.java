/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceKeyImporterMessageHandler",
        service = MessageHandlerFactory.class,
        property = {
                "name=" + SecureDeviceKeyImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + SecureDeviceKeyImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + SecureDeviceKeyImporterMessageHandler.DESTINATION_NAME },
        immediate = true)
public class SecureDeviceKeyImporterMessageHandler implements MessageHandlerFactory {

    static final String DESTINATION_NAME = "SecureDeviceKeyImport";
    public static final String SUBSCRIBER_NAME = "SecureDeviceKeyImport";
    static final String COMPONENT_NAME = "SSK";

    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile OrmService ormService;

    // OSGi constructor
    public SecureDeviceKeyImporterMessageHandler() {

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
                InstallIdentifier.identifier("MultiSense", SecureDeviceKeyImporterMessageHandler.COMPONENT_NAME),
                dataModel,
                SskInstaller.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4_SSK.class
                ));

    }

    @Inject // for test purposes
    public SecureDeviceKeyImporterMessageHandler(FileImportService fileImportService, UpgradeService upgradeService, MessageService messageService, OrmService ormService) {
        this();
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        setOrmService(ormService);
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

}