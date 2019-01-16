/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.hsm.HsmEnergyService;
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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;


@Component(name = "com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureHSMDeviceShipmentImporterMessageHandler",
        service = MessageHandlerFactory.class,
        property = {
                "name=" + SecureHSMDeviceShipmentImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + SecureHSMDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + SecureHSMDeviceShipmentImporterMessageHandler.DESTINATION_NAME },
        immediate = true)
public class SecureHSMDeviceShipmentImporterMessageHandler implements MessageHandlerFactory {

    static final String DESTINATION_NAME = "SecHSMShipmntImport";
    public static final String SUBSCRIBER_NAME = "SecHSMShipmntImport";
    static final String COMPONENT_NAME = "SHI";

    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile OrmService ormService;
    private volatile HsmEnergyService hsmEnergyService;

    public SecureHSMDeviceShipmentImporterMessageHandler() {

    }

    @Inject // for test purposes
    public SecureHSMDeviceShipmentImporterMessageHandler(FileImportService fileImportService, UpgradeService upgradeService, MessageService messageService, OrmService ormService) {
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        setOrmService(ormService);
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(OrmService.class).toInstance(ormService);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
            }
        });

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", SecureHSMDeviceShipmentImporterMessageHandler.COMPONENT_NAME),
                dataModel,
                HsmInstaller.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4_HSM.class
                ));

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

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    public void unsetHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = null;
    }

}