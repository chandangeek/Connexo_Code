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

@Component(name = "com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceShipmentImporterMessageHandler",
        service = MessageHandlerFactory.class,
        property = {
                "name=" + SecureDeviceShipmentImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + SecureDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + SecureDeviceShipmentImporterMessageHandler.DESTINATION_NAME },
        immediate = true)
public class SecureDeviceShipmentImporterMessageHandler implements MessageHandlerFactory {

    static final String DESTINATION_NAME = "SecureShipmentImport";
    static final String SUBSCRIBER_NAME = "SecureShipmentImport";
    static final String COMPONENT_NAME = "SSI";

    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile OrmService ormService;

    // OSGi constructor
    public SecureDeviceShipmentImporterMessageHandler() {

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
                InstallIdentifier.identifier("MultiSense", SecureDeviceShipmentImporterMessageHandler.COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4.class
                ));

    }

    @Inject // for test purposes
    public SecureDeviceShipmentImporterMessageHandler(FileImportService fileImportService) {
        this();
        setFileImportService(fileImportService);
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